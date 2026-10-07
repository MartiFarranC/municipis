package cat.descobreix.gent

import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.descobreix.data.compte.EstatCompte
import cat.descobreix.data.compte.ServeiCompte
import cat.descobreix.data.social.EstatSeguiment
import cat.descobreix.data.social.EventMur
import cat.descobreix.data.social.FotoPersona
import cat.descobreix.data.social.PerfilPersona
import cat.descobreix.data.social.Persona
import cat.descobreix.data.social.ReaccioRebuda
import cat.descobreix.data.social.ServeiSocial
import cat.descobreix.data.social.TipusEvent
import cat.descobreix.domain.Joc
import cat.descobreix.joc.config.ConfiguracioJoc.TipusObjecte
import cat.descobreix.joc.dades.GeometriaMapa
import cat.descobreix.joc.model.CodiIne
import cat.descobreix.map.CaminsMapa
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** Les imatges de les fotos dels altres, baixades del servidor i guardades en memòria mentre l'app és oberta. */
@Singleton
class ImatgesRemotes @Inject constructor(private val social: ServeiSocial) {
    private val cache = LruCache<String, ImageBitmap>(80)

    suspend fun carrega(ruta: String): ImageBitmap? {
        cache.get(ruta)?.let { return it }
        return try {
            val bytes = social.imatge(ruta)
            withContext(Dispatchers.Default) { BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap() }?.also { cache.put(ruta, it) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }
}

/** Executa una crida al servidor; si falla (normalment, sense connexió), torna null. */
private suspend fun <T> remot(bloc: suspend () -> T): T? = try {
    bloc()
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    null
}

data class GentEstat(
    val carregant: Boolean = true,
    val senseConnexio: Boolean = false,
    val cerca: String = "",
    val resultats: List<Persona> = emptyList(),
    val sollicituds: List<Persona> = emptyList(),
    val mur: List<EventMur> = emptyList(),
    val hiHaMes: Boolean = false,
    /** El nom de cada municipi, per escriure el mur. */
    val noms: Map<CodiIne, String> = emptyMap(),
    /** Les reaccions que t'han enviat. */
    val reaccions: List<ReaccioRebuda> = emptyList(),
    /** Els emojis que tens (els de tothom i els dels sacs): només aquests es poden enviar. */
    val emojis: List<String> = emptyList(),
    /** La cosa del mur a què s'està reaccionant (s'obre la finestra per animar). */
    val animant: EventMur? = null,
    /** Avís breu després d'una acció: s'ha enviat la reacció, s'ha bloquejat algú, la denúncia… */
    val avis: AvisGent? = null,
)

enum class AvisGent { ENVIAT, BLOQUEJAT, DENUNCIAT, ERROR }

/** La pestanya «Gent»: el mur de la gent que segueixes, buscar gent i les sol·licituds per seguir-te. */
@HiltViewModel
class GentViewModel @Inject constructor(
    private val social: ServeiSocial,
    private val joc: Joc,
    val imatges: ImatgesRemotes,
) : ViewModel() {
    private val _estat = MutableStateFlow(GentEstat())
    val estat: StateFlow<GentEstat> = _estat.asStateFlow()
    private var cercant: Job? = null

    init {
        viewModelScope.launch {
            val noms = joc.dades().geografia.municipis.associate { it.codi to it.nom }
            _estat.update { it.copy(noms = noms) }
            actualitza()
        }
        viewModelScope.launch {
            joc.colleccio.collect { c ->
                _estat.update { it.copy(emojis = c.filter { o -> o.tipus == TipusObjecte.EMOJI }.map { o -> o.id }) }
            }
        }
    }

    fun actualitza() {
        viewModelScope.launch {
            val sollicituds = remot { social.sollicituds() }
            val mur = remot { social.mur(null) }
            val reaccions = remot { social.reaccionsRebudes() }
            _estat.update {
                it.copy(
                    carregant = false,
                    senseConnexio = sollicituds == null || mur == null,
                    sollicituds = sollicituds ?: it.sollicituds,
                    mur = mur ?: it.mur,
                    reaccions = reaccions ?: it.reaccions,
                    hiHaMes = (mur?.size ?: 0) >= PAGINA,
                )
            }
        }
    }

    fun obreAnima(e: EventMur) = _estat.update { it.copy(animant = e) }

    fun tancaAnima() = _estat.update { it.copy(animant = null) }

    fun tancaAvis() = _estat.update { it.copy(avis = null) }

    /** Envia un emoji (si [emoji]) o un missatge a qui ha fet la cosa del mur que s'està animant. */
    fun anima(emoji: String?, missatge: String?) {
        val e = _estat.value.animant ?: return
        if (emoji != null && emoji !in _estat.value.emojis) return
        val text = missatge?.trim()?.take(MAX_MISSATGE)?.takeIf { it.isNotEmpty() }
        if ((emoji == null) == (text == null)) return
        _estat.update { it.copy(animant = null) }
        viewModelScope.launch {
            val objectiu = if (e.tipus == TipusEvent.FOTO) e.fotoId ?: return@launch else e.codiIne
            val fet = remot { social.anima(e.usuariId, e.tipus, objectiu, emoji, text) } != null
            _estat.update { it.copy(avis = if (fet) AvisGent.ENVIAT else AvisGent.ERROR) }
        }
    }

    fun treuReaccio(r: ReaccioRebuda) {
        viewModelScope.launch {
            if (remot { social.treuReaccio(r.id) } != null) _estat.update { it.copy(reaccions = it.reaccions - r) }
        }
    }

    /** Bloqueja qui t'ha enviat una reacció: deixa de seguir-te i se'n treuen les reaccions. */
    fun bloqueja(r: ReaccioRebuda) {
        viewModelScope.launch {
            val fet = remot { social.bloqueja(r.autorId) } != null
            if (fet) _estat.update { e -> e.copy(reaccions = e.reaccions.filterNot { it.autorId == r.autorId }, avis = AvisGent.BLOQUEJAT) }
            else _estat.update { it.copy(avis = AvisGent.ERROR) }
        }
    }

    fun denuncia(r: ReaccioRebuda, motiu: String?) {
        viewModelScope.launch {
            val fet = remot { social.denuncia(r.autorId, r, motiu) } != null
            _estat.update { it.copy(avis = if (fet) AvisGent.DENUNCIAT else AvisGent.ERROR) }
        }
    }

    fun mes() {
        val ultim = _estat.value.mur.lastOrNull() ?: return
        viewModelScope.launch {
            val mes = remot { social.mur(ultim.creatEl) } ?: return@launch
            _estat.update { it.copy(mur = it.mur + mes, hiHaMes = mes.size >= PAGINA) }
        }
    }

    fun cerca(text: String) {
        _estat.update { it.copy(cerca = text) }
        cercant?.cancel()
        cercant = viewModelScope.launch {
            delay(300)
            val r = if (text.isBlank()) emptyList() else remot { social.cerca(text) } ?: return@launch
            _estat.update { it.copy(resultats = r) }
        }
    }

    fun accepta(p: Persona) = canviaSollicitud(p) { social.accepta(p.id) }

    fun rebutja(p: Persona) = canviaSollicitud(p) { social.treu(p.id) }

    private fun canviaSollicitud(p: Persona, accio: suspend () -> Unit) {
        viewModelScope.launch {
            if (remot { accio() } != null) _estat.update { it.copy(sollicituds = it.sollicituds - p) }
        }
    }

    companion object {
        private const val PAGINA = 50
        const val MAX_MISSATGE = 80
    }
}

data class PersonaEstat(
    val carregant: Boolean = true,
    val senseConnexio: Boolean = false,
    val perfil: PerfilPersona? = null,
    val municipis: Set<CodiIne> = emptySet(),
    val fotos: List<FotoPersona> = emptyList(),
    val seguits: List<Persona> = emptyList(),
    val total: Int = 0,
    val geometria: GeometriaMapa? = null,
    val camins: CaminsMapa? = null,
    val index: Map<CodiIne, Int> = emptyMap(),
    val noms: Map<CodiIne, String> = emptyMap(),
    val treballant: Boolean = false,
    val bloquejat: Boolean = false,
    val avis: AvisGent? = null,
)

/** El perfil d'una altra persona: seguir-la i, si et deixa, veure'n el mapa, les fotos i a qui segueix. */
@HiltViewModel
class PersonaViewModel @Inject constructor(
    private val social: ServeiSocial,
    private val joc: Joc,
    val imatges: ImatgesRemotes,
    estatDesat: SavedStateHandle,
) : ViewModel() {
    val id: String = checkNotNull(estatDesat["id"])
    private val _estat = MutableStateFlow(PersonaEstat())
    val estat: StateFlow<PersonaEstat> = _estat.asStateFlow()

    init {
        viewModelScope.launch {
            val d = joc.dades()
            val mapa = joc.mapa()
            val camins = withContext(Dispatchers.Default) { CaminsMapa.construeix(mapa) }
            _estat.update {
                it.copy(
                    geometria = mapa,
                    camins = camins,
                    index = d.geografia.municipis.withIndex().associate { (i, m) -> m.codi to i },
                    noms = d.geografia.municipis.associate { m -> m.codi to m.nom },
                    total = d.geografia.total,
                )
            }
            carrega()
        }
    }

    private suspend fun carrega() {
        val p = remot { social.perfil(id) }
        val bloquejat = remot { social.hasBloquejat(id) } ?: false
        if (p == null) {
            _estat.update { it.copy(carregant = false, senseConnexio = true, treballant = false) }
            return
        }
        val municipis = if (p.visible) remot { social.municipisDe(id) } ?: emptySet() else emptySet()
        // Les fotos públiques d'un compte públic es veuen encara que no el segueixis.
        val fotos = if (p.visible || p.public) remot { social.fotosDe(id) } ?: emptyList() else emptyList()
        val seguits = if (p.visible || p.public) remot { social.seguitsDe(id) } ?: emptyList() else emptyList()
        _estat.update {
            it.copy(
                carregant = false, senseConnexio = false, perfil = p, municipis = municipis, fotos = fotos,
                seguits = seguits, treballant = false, bloquejat = bloquejat,
            )
        }
    }

    fun segueix() = accio { social.segueix(id) }

    fun deixaDeSeguir() = accio { social.deixaDeSeguir(id) }

    fun bloqueja() = accio { social.bloqueja(id) }

    fun desbloqueja() = accio { social.desbloqueja(id) }

    fun denuncia(motiu: String?) {
        viewModelScope.launch {
            val fet = remot { social.denuncia(id, null, motiu) } != null
            _estat.update { it.copy(avis = if (fet) AvisGent.DENUNCIAT else AvisGent.ERROR) }
        }
    }

    fun tancaAvis() = _estat.update { it.copy(avis = null) }

    private fun accio(bloc: suspend () -> Unit) {
        if (_estat.value.treballant) return
        _estat.update { it.copy(treballant = true) }
        viewModelScope.launch {
            remot { bloc() }
            carrega()
        }
    }
}

data class SeguitsEstat(
    val carregant: Boolean = true,
    val senseConnexio: Boolean = false,
    val seguits: List<Pair<Persona, EstatSeguiment>> = emptyList(),
    val nom: String = "",
    val public: Boolean = false,
    val canviant: Boolean = false,
    val errorCanviant: Boolean = false,
)

/** La gent que segueixes i el teu compte (públic o privat), per al perfil de l'Espectador i de l'Explorador. */
@HiltViewModel
class SeguitsViewModel @Inject constructor(private val social: ServeiSocial, private val compte: ServeiCompte) : ViewModel() {
    private val _estat = MutableStateFlow(SeguitsEstat())
    val estat: StateFlow<SeguitsEstat> = _estat.asStateFlow()

    init {
        viewModelScope.launch {
            compte.estat.collect { e ->
                if (e is EstatCompte.Llest) _estat.update { it.copy(nom = e.perfil.nomUsuari, public = e.perfil.public) }
            }
        }
        actualitza()
    }

    fun actualitza() {
        viewModelScope.launch {
            val s = remot { social.seguits() }
            _estat.update { it.copy(carregant = false, senseConnexio = s == null, seguits = s ?: it.seguits) }
        }
    }

    fun canviaVisibilitat(public: Boolean) {
        if (_estat.value.canviant) return
        _estat.update { it.copy(canviant = true, errorCanviant = false) }
        viewModelScope.launch {
            val fet = remot { compte.triaVisibilitat(public) } != null
            _estat.update { it.copy(canviant = false, errorCanviant = !fet) }
        }
    }
}
