package cat.descobreix.compte

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.descobreix.data.compte.CONTRASENYA_MINIM
import cat.descobreix.data.compte.ErrorCompte
import cat.descobreix.data.compte.ExcepcioCompte
import cat.descobreix.data.compte.NomUsuari
import cat.descobreix.data.compte.ResultatRegistre
import cat.descobreix.data.compte.ServeiCompte
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ModeCompte {
    /** Entrar amb correu i contrasenya. */
    ENTRA,

    /** Crear un compte amb correu i contrasenya. */
    REGISTRA,

    /** Rebre un enllaç per entrar sense contrasenya. */
    ENLLAC,

    /** Rebre un enllaç per triar una contrasenya nova. */
    RECUPERA,
}

/** Avisos quan una operació ha anat bé però l'usuari ha de mirar el correu. */
enum class AvisCompte { CONFIRMA_CORREU, ENLLAC_ENVIAT, RECUPERACIO_ENVIADA }

data class CompteEstat(
    val mode: ModeCompte = ModeCompte.ENTRA,
    val correu: String = "",
    val contrasenya: String = "",
    val nomUsuari: String = "",
    val treballant: Boolean = false,
    val error: ErrorCompte? = null,
    val avis: AvisCompte? = null,
) {
    val correuValid: Boolean get() = esCorreu(correu)

    val potEnviar: Boolean
        get() = !treballant && correuValid && when (mode) {
            ModeCompte.ENTRA -> contrasenya.isNotEmpty()
            ModeCompte.REGISTRA -> contrasenya.length >= CONTRASENYA_MINIM
            ModeCompte.ENLLAC, ModeCompte.RECUPERA -> true
        }

    val nomUsuariValid: Boolean get() = NomUsuari.esValid(NomUsuari.normalitza(nomUsuari))

    val contrasenyaNovaValida: Boolean get() = contrasenya.length >= CONTRASENYA_MINIM

    companion object {
        private val patroCorreu = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

        fun esCorreu(text: String): Boolean = patroCorreu.matches(text.trim())
    }
}

@HiltViewModel
class CompteViewModel @Inject constructor(private val compte: ServeiCompte) : ViewModel() {
    private val _estat = MutableStateFlow(CompteEstat())
    val estat: StateFlow<CompteEstat> = _estat.asStateFlow()

    fun canviaMode(mode: ModeCompte) = _estat.update { it.copy(mode = mode, contrasenya = "", error = null) }

    fun canviaCorreu(text: String) = _estat.update { it.copy(correu = text, error = null) }

    fun canviaContrasenya(text: String) = _estat.update { it.copy(contrasenya = text, error = null) }

    fun canviaNomUsuari(text: String) = _estat.update { it.copy(nomUsuari = text, error = null) }

    fun tancaError() = _estat.update { it.copy(error = null) }

    fun tancaAvis() = _estat.update { it.copy(avis = null) }

    /** Fa l'acció del mode actual: entrar, registrar-se o enviar un enllaç. */
    fun envia() {
        val e = _estat.value
        if (!e.potEnviar) return
        executa {
            when (e.mode) {
                ModeCompte.ENTRA -> {
                    compte.entra(e.correu, e.contrasenya)
                    null
                }
                ModeCompte.REGISTRA -> when (compte.registra(e.correu, e.contrasenya)) {
                    ResultatRegistre.FET -> null
                    ResultatRegistre.CAL_CONFIRMAR_CORREU -> AvisCompte.CONFIRMA_CORREU
                }
                ModeCompte.ENLLAC -> {
                    compte.enviaEnllac(e.correu)
                    AvisCompte.ENLLAC_ENVIAT
                }
                ModeCompte.RECUPERA -> {
                    compte.recuperaContrasenya(e.correu)
                    AvisCompte.RECUPERACIO_ENVIADA
                }
            }
        }
    }

    fun creaPerfil() {
        val e = _estat.value
        if (e.treballant) return
        if (!e.nomUsuariValid) {
            _estat.update { it.copy(error = ErrorCompte.NOM_USUARI_INVALID) }
            return
        }
        executa {
            compte.creaPerfil(e.nomUsuari)
            null
        }
    }

    fun desaContrasenyaNova() {
        val e = _estat.value
        if (e.treballant || !e.contrasenyaNovaValida) return
        executa {
            compte.canviaContrasenya(e.contrasenya)
            null
        }
    }

    fun reintenta() = executa {
        compte.reintenta()
        null
    }

    fun surt() = executa {
        compte.surt()
        null
    }

    private fun executa(accio: suspend () -> AvisCompte?) {
        _estat.update { it.copy(treballant = true, error = null) }
        viewModelScope.launch {
            try {
                val avis = accio()
                _estat.update { it.copy(treballant = false, avis = avis) }
            } catch (e: ExcepcioCompte) {
                _estat.update { it.copy(treballant = false, error = e.error) }
            }
        }
    }
}
