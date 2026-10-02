package cat.descobreix.photos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.descobreix.data.repositori.FotosRepositori
import cat.descobreix.domain.Foto
import cat.descobreix.domain.Joc
import cat.descobreix.joc.model.Visibilitat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FotoEstat(
    val carregant: Boolean = true,
    val foto: Foto? = null,
    val nomMunicipi: String = "",
    val esborrada: Boolean = false,
)

@HiltViewModel
class FotoViewModel @Inject constructor(
    private val fotos: FotosRepositori,
    private val joc: Joc,
    estatDesat: SavedStateHandle,
) : ViewModel() {
    private val id: String = checkNotNull(estatDesat["id"])
    private val _estat = MutableStateFlow(FotoEstat())
    val estat: StateFlow<FotoEstat> = _estat.asStateFlow()

    init {
        viewModelScope.launch {
            val geografia = joc.dades().geografia
            fotos.foto(id).collect { f ->
                _estat.update {
                    it.copy(carregant = false, foto = f, nomMunicipi = f?.let { x -> geografia.municipi(x.codiIne).nom } ?: "")
                }
            }
        }
    }

    fun canviaVisibilitat(v: Visibilitat) {
        viewModelScope.launch { fotos.canviaVisibilitat(id, v) }
    }

    fun fesPortada() {
        viewModelScope.launch { fotos.fesPortada(id) }
    }

    fun esborra() {
        viewModelScope.launch {
            fotos.esborra(id)
            _estat.update { it.copy(esborrada = true) }
        }
    }
}
