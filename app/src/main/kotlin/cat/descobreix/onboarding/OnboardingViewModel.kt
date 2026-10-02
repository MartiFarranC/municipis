package cat.descobreix.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.descobreix.data.ubicacio.ServeiUbicacio
import cat.descobreix.domain.Joc
import cat.descobreix.joc.geo.Localitzacio
import cat.descobreix.joc.model.CodiIne
import cat.descobreix.ui.Missatge
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class OpcioMunicipi(val codi: CodiIne, val nom: String, val comarca: String)

data class OnboardingEstat(
    val text: String = "",
    val resultats: List<OpcioMunicipi> = emptyList(),
    val seleccionat: OpcioMunicipi? = null,
    val buscantUbicacio: Boolean = false,
    val candidats: List<OpcioMunicipi>? = null,
    val missatge: Missatge? = null,
    val comencant: Boolean = false,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val joc: Joc,
    private val ubicacio: ServeiUbicacio,
) : ViewModel() {
    private val _estat = MutableStateFlow(OnboardingEstat())
    val estat: StateFlow<OnboardingEstat> = _estat.asStateFlow()

    fun canviaText(text: String) {
        _estat.update { it.copy(text = text) }
        viewModelScope.launch {
            val dades = joc.dades()
            val resultats = dades.cercador.cerca(text, LIMIT).map { opcio(it.codi) }
            _estat.update { if (it.text == text) it.copy(resultats = resultats) else it }
        }
    }

    fun tria(opcio: OpcioMunicipi) = _estat.update { it.copy(seleccionat = opcio, candidats = null) }

    fun tancaCandidats() = _estat.update { it.copy(candidats = null) }

    fun tancaMissatge() = _estat.update { it.copy(missatge = null) }

    fun sensePermis() = _estat.update { it.copy(missatge = Missatge.SensePermisUbicacio) }

    fun faServirUbicacio() {
        if (_estat.value.buscantUbicacio) return
        _estat.update { it.copy(buscantUbicacio = true) }
        viewModelScope.launch {
            val u = ubicacio.ubicacioActual()
            if (u == null) {
                _estat.update { it.copy(buscantUbicacio = false, missatge = Missatge.SenseUbicacio) }
                return@launch
            }
            val dades = joc.dades()
            val loc = withContext(Dispatchers.Default) { dades.localitzador.localitza(u.lat, u.lon, u.precisioMetres) }
            _estat.update {
                when (loc) {
                    is Localitzacio.Dins -> it.copy(buscantUbicacio = false, seleccionat = opcio(loc.codi))
                    is Localitzacio.Dubte -> it.copy(buscantUbicacio = false, candidats = loc.candidats.map { c -> opcio(c) })
                    Localitzacio.Fora -> it.copy(buscantUbicacio = false, missatge = Missatge.ForaDeCatalunya)
                }
            }
        }
    }

    fun comenca(onComencat: (CodiIne) -> Unit) {
        val opcio = _estat.value.seleccionat ?: return
        if (_estat.value.comencant) return
        _estat.update { it.copy(comencant = true) }
        viewModelScope.launch {
            try {
                joc.iniciaPartida(opcio.codi)
                onComencat(opcio.codi)
            } catch (e: IllegalStateException) {
                // La partida ja estava iniciada (per exemple, amb un doble toc): anem al mapa igualment.
                onComencat(opcio.codi)
            }
        }
    }

    private suspend fun opcio(codi: CodiIne): OpcioMunicipi {
        val g = joc.dades().geografia
        val m = g.municipi(codi)
        return OpcioMunicipi(m.codi, m.nom, g.comarca(m.comarca).nom)
    }

    private companion object {
        const val LIMIT = 30
    }
}
