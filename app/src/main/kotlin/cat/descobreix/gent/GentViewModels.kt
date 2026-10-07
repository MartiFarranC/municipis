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
import cat.descobreix.data.social.ServeiSocial
import cat.descobreix.domain.Joc
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
)

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
    }

    fun actualitza() {
        viewModelScope.launch {
            val sollicituds = remot { social.sollicituds() }
            val mur = remot { social.mur(null) }
            _estat.update {
                it.copy(
                    carregant = false,
                    senseConnexio = sollicituds == null || mur == null,
                    sollicituds = sollicituds ?: it.sollicituds,
                    mur = mur ?: it.mur,
                    hiHaMes = (mur?.size ?: 0) >= PAGINA,
                )
            }
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

    private companion object {
        const val PAGINA = 50
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
                seguits = seguits, treballant = false,
            )
        }
    }

    fun segueix() = accio { social.segueix(id) }

    fun deixaDeSeguir() = accio { social.deixaDeSeguir(id) }

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
