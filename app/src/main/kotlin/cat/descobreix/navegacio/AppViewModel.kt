package cat.descobreix.navegacio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.descobreix.data.compte.EstatCompte
import cat.descobreix.data.compte.ServeiCompte
import cat.descobreix.domain.Joc
import cat.descobreix.domain.MedallesNoves
import cat.descobreix.ui.components.Silueta
import cat.descobreix.ui.components.siluetesComarques
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Les medalles a celebrar, amb el que cal per dibuixar-les. */
data class CelebracioPendent(val noves: MedallesNoves, val nomsComarques: Map<String, String>, val siluetes: Map<String, Silueta>)

@HiltViewModel
class AppViewModel @Inject constructor(private val joc: Joc, compte: ServeiCompte) : ViewModel() {
    /** El compte és obligatori: només es juga quan és [EstatCompte.Llest]. */
    val compte: StateFlow<EstatCompte> = compte.estat

    /** null mentre es carrega; després, si hi ha una partida començada. */
    val partidaIniciada: StateFlow<Boolean?> = joc.progres
        .map { it.partidaIniciada }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val cua = MutableStateFlow<List<CelebracioPendent>>(emptyList())

    /** La celebració de medalles que toca mostrar ara, si n'hi ha cap. */
    val celebracio: StateFlow<CelebracioPendent?> = cua
        .map { it.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private var siluetes: Map<String, Silueta>? = null

    private val _sacsNous = MutableStateFlow(0)

    /** Quants sacs s'han guanyat des de l'últim avís (0 si no n'hi ha cap per anunciar). */
    val sacsNous: StateFlow<Int> = _sacsNous.asStateFlow()

    init {
        viewModelScope.launch {
            joc.sacsNous.collect { nous ->
                launch {
                    // Després de la celebració de la missió (la de les medalles, si n'hi ha, ja espera la seva).
                    delay(ESPERA_MS)
                    _sacsNous.update { it + nous.size }
                }
            }
        }
        // Els sacs que tocaven abans que n'hi hagués (o que no es van poder desar) es donen en obrir l'app.
        viewModelScope.launch { joc.revisaSacs() }
        viewModelScope.launch {
            joc.medallesNoves.collect { noves ->
                launch {
                    val d = joc.dades()
                    val s = siluetes ?: withContext(Dispatchers.Default) { siluetesComarques(joc.mapa(), d.geografia) }.also { siluetes = it }
                    // Primer es veu la celebració de la missió o del desbloqueig; després, la de la medalla.
                    delay(ESPERA_MS)
                    cua.update { it + CelebracioPendent(noves, d.geografia.comarques.associate { c -> c.codi to c.nom }, s) }
                }
            }
        }
    }

    fun tancaCelebracio() = cua.update { it.drop(1) }

    fun tancaAvisSacs() {
        _sacsNous.value = 0
    }

    private companion object {
        const val ESPERA_MS = 3500L
    }
}
