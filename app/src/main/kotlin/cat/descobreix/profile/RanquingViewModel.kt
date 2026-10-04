package cat.descobreix.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.descobreix.data.ranquing.CriteriRanquing
import cat.descobreix.data.ranquing.FilaRanquing
import cat.descobreix.data.ranquing.ServeiRanquing
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RanquingEstat(
    val criteri: CriteriRanquing = CriteriRanquing.PUNTS,
    val nomesAmics: Boolean = false,
    val carregant: Boolean = true,
    val files: List<FilaRanquing> = emptyList(),
    /** No s'ha pogut carregar (normalment, sense connexió). */
    val error: Boolean = false,
) {
    /** La fila de l'usuari, que es mostra sempre a sota encara que no sigui entre les primeres. */
    val jo: FilaRanquing? get() = files.firstOrNull { it.socJo }

    /** Si a la classificació d'amics només hi ha l'usuari. */
    val senseAmics: Boolean get() = nomesAmics && !carregant && !error && files.all { it.socJo }
}

@HiltViewModel
class RanquingViewModel @Inject constructor(private val ranquing: ServeiRanquing) : ViewModel() {
    private val _estat = MutableStateFlow(RanquingEstat())
    val estat: StateFlow<RanquingEstat> = _estat.asStateFlow()
    private var carrega: Job? = null

    init {
        actualitza()
    }

    fun canviaCriteri(criteri: CriteriRanquing) {
        if (criteri == _estat.value.criteri) return
        _estat.update { it.copy(criteri = criteri) }
        actualitza()
    }

    fun canviaAmbit(nomesAmics: Boolean) {
        if (nomesAmics == _estat.value.nomesAmics) return
        _estat.update { it.copy(nomesAmics = nomesAmics) }
        actualitza()
    }

    /** Puja el progrés pendent, perquè l'usuari s'hi vegi al dia, i torna a carregar la classificació. */
    fun actualitza() {
        carrega?.cancel()
        _estat.update { it.copy(carregant = true, error = false) }
        carrega = viewModelScope.launch {
            try {
                ranquing.pujaProgres()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "No s'ha pogut pujar el progrés", e)
            }
            val e = _estat.value
            try {
                val files = ranquing.classificacio(e.criteri, e.nomesAmics)
                _estat.update { it.copy(carregant = false, files = files) }
            } catch (ex: CancellationException) {
                throw ex
            } catch (ex: Exception) {
                Log.w(TAG, "No s'ha pogut carregar el rànquing", ex)
                _estat.update { it.copy(carregant = false, error = true, files = emptyList()) }
            }
        }
    }

    private companion object {
        const val TAG = "Ranquing"
    }
}
