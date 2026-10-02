package cat.descobreix.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.descobreix.data.exportacio.GestioDades
import cat.descobreix.data.repositori.FotosRepositori
import cat.descobreix.domain.Foto
import cat.descobreix.domain.Joc
import cat.descobreix.joc.progressio.Assoliment
import cat.descobreix.joc.progressio.DadesAssoliments
import cat.descobreix.joc.progressio.Nivell
import cat.descobreix.joc.progressio.ProgresComarca
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    val assoliments: List<Assoliment> = emptyList(),
    val album: List<GrupFotos> = emptyList(),
    val treballant: Boolean = false,
    /** Resultat de l'última exportació: true si ha anat bé. */
    val exportat: Boolean? = null,
)

@HiltViewModel
class PerfilViewModel @Inject constructor(
    private val joc: Joc,
    fotos: FotosRepositori,
    private val gestio: GestioDades,
) : ViewModel() {
    private val _estat = MutableStateFlow(PerfilEstat())
    val estat: StateFlow<PerfilEstat> = _estat.asStateFlow()

    init {
        viewModelScope.launch {
            val d = joc.dades()
            combine(joc.progres, fotos.totes) { p, f -> p to f }.collect { (p, f) ->
                val municipisComplets = p.descoberts.count { d.regles.municipiComplet(it, p.completades.keys) }
                val comarques = d.assoliments.progresComarques(p.conjuntDescoberts)
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
                        assoliments = d.assoliments.calcula(DadesAssoliments(p.conjuntDescoberts, p.completades.size, municipisComplets, f.size)),
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

    fun esborraTot() {
        _estat.update { it.copy(treballant = true) }
        viewModelScope.launch {
            gestio.esborraTot()
            _estat.update { it.copy(treballant = false) }
        }
    }
}
