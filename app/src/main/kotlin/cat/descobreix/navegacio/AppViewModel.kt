package cat.descobreix.navegacio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.descobreix.domain.Joc
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(joc: Joc) : ViewModel() {
    /** null mentre es carrega; després, si hi ha una partida començada. */
    val partidaIniciada: StateFlow<Boolean?> = joc.progres
        .map { it.partidaIniciada }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}
