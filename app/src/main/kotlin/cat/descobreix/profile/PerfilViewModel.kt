package cat.descobreix.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.descobreix.data.compte.EstatCompte
import cat.descobreix.data.compte.ExcepcioCompte
import cat.descobreix.data.compte.ServeiCompte
import cat.descobreix.data.exportacio.GestioDades
import cat.descobreix.data.repositori.FotosRepositori
import cat.descobreix.domain.Foto
import cat.descobreix.domain.Joc
import cat.descobreix.joc.progressio.Medalla
import cat.descobreix.joc.progressio.Nivell
import cat.descobreix.joc.progressio.ProgresComarca
import cat.descobreix.joc.progressio.TipusMedalla
import cat.descobreix.ui.components.Silueta
import cat.descobreix.ui.components.siluetesComarques
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class GrupFotos(val nom: String, val fotos: List<Foto>)

data class PerfilEstat(
    val carregant: Boolean = true,
    val nivell: Nivell? = null,
    val saldo: Int = 0,
    val puntsGuanyats: Int = 0,
    val descoberts: Int = 0,
    val total: Int = 0,
    val missionsFetes: Int = 0,
    val fotos: Int = 0,
    val comarquesCompletes: Int = 0,
    val comarques: List<ProgresComarca> = emptyList(),
    /** La vitrina: primer les comarques (les més avançades al davant) i després les fites. */
    val medalles: List<Medalla> = emptyList(),
    val nomsComarques: Map<String, String> = emptyMap(),
    /** Siluetes de les comarques per dibuixar les medalles. Buit fins que s'ha carregat el mapa. */
    val siluetes: Map<String, Silueta> = emptyMap(),
    val album: List<GrupFotos> = emptyList(),
    val treballant: Boolean = false,
    /** Resultat de l'última exportació: true si ha anat bé. */
    val exportat: Boolean? = null,
    val nomUsuari: String? = null,
    /** No s'han pogut esborrar les dades (per exemple, sense connexió). */
    val errorEsborrant: Boolean = false,
)

@HiltViewModel
class PerfilViewModel @Inject constructor(
    private val joc: Joc,
    fotos: FotosRepositori,
    private val gestio: GestioDades,
    private val compte: ServeiCompte,
) : ViewModel() {
    private val _estat = MutableStateFlow(PerfilEstat())
    val estat: StateFlow<PerfilEstat> = _estat.asStateFlow()

    init {
        viewModelScope.launch {
            compte.estat.collect { e ->
                _estat.update { it.copy(nomUsuari = (e as? EstatCompte.Llest)?.perfil?.nomUsuari) }
            }
        }
        viewModelScope.launch {
            val d = joc.dades()
            val mapa = joc.mapa()
            val siluetes = withContext(Dispatchers.Default) { siluetesComarques(mapa, d.geografia) }
            _estat.update { it.copy(siluetes = siluetes) }
        }
        viewModelScope.launch {
            val d = joc.dades()
            val noms = d.geografia.comarques.associate { it.codi to it.nom }
            combine(joc.progres, fotos.totes) { p, f -> p to f }.collect { (p, f) ->
                val comarques = d.medalles.progresComarques(p.conjuntDescoberts)
                val medalles = d.medalles.calcula(d.medalles.dades(p.conjuntDescoberts, p.completades.keys))
                _estat.update {
                    it.copy(
                        carregant = false,
                        nivell = d.nivells.nivell(p.puntsGuanyats),
                        saldo = p.saldo,
                        puntsGuanyats = p.puntsGuanyats,
                        descoberts = p.descoberts.size,
                        total = d.geografia.total,
                        missionsFetes = p.completades.size,
                        fotos = f.size,
                        comarquesCompletes = comarques.count { c -> c.completa },
                        comarques = comarques.sortedWith(compareByDescending<ProgresComarca> { c -> c.descoberts.toFloat() / c.total }.thenBy { c -> c.nom }),
                        medalles = ordenaVitrina(medalles, noms),
                        nomsComarques = noms,
                        album = f.groupBy { x -> x.codiIne }.map { (codi, l) -> GrupFotos(d.geografia.municipi(codi).nom, l) },
                    )
                }
            }
        }
    }

    fun exporta(desti: Uri) {
        _estat.update { it.copy(treballant = true) }
        viewModelScope.launch {
            val ok = runCatching { gestio.exporta(desti) }.isSuccess
            _estat.update { it.copy(treballant = false, exportat = ok) }
        }
    }

    fun tancaExportacio() = _estat.update { it.copy(exportat = null) }

    /** Esborra les dades del joc al servidor i al mòbil (cal connexió). */
    fun esborraTot() {
        _estat.update { it.copy(treballant = true) }
        viewModelScope.launch {
            val ok = try {
                compte.esborraDades()
                true
            } catch (e: ExcepcioCompte) {
                false
            }
            _estat.update { it.copy(treballant = false, errorEsborrant = !ok) }
        }
    }

    fun tancaErrorEsborrant() = _estat.update { it.copy(errorEsborrant = false) }

    fun surt() {
        _estat.update { it.copy(treballant = true) }
        viewModelScope.launch {
            compte.surt()
            _estat.update { it.copy(treballant = false) }
        }
    }
}

/** Les comarques primer, de més a menys avançades (i per nom), i després les fites en l'ordre de sempre. */
internal fun ordenaVitrina(medalles: List<Medalla>, noms: Map<String, String>): List<Medalla> {
    val (comarques, fites) = medalles.partition { it.tipus == TipusMedalla.COMARCA }
    val ordenades = comarques.sortedWith(
        compareByDescending<Medalla> { it.nivell?.ordinal ?: -1 }
            .thenByDescending { it.actual.toFloat() / it.necessari }
            .thenBy { noms[it.comarca].orEmpty() },
    )
    return ordenades + fites
}
