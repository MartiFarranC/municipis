package cat.descobreix

import android.content.Intent
import cat.descobreix.data.compte.EstatCompte
import cat.descobreix.data.compte.Perfil
import cat.descobreix.data.compte.ResultatRegistre
import cat.descobreix.data.compte.ServeiCompte
import kotlinx.coroutines.flow.MutableStateFlow

/** Un compte sense servidor: entrar sempre funciona i el perfil es crea a l'instant. */
class CompteFals(inicial: EstatCompte = EstatCompte.Llest(PERFIL)) : ServeiCompte {
    override val estat = MutableStateFlow(inicial)

    override suspend fun entra(correu: String, contrasenya: String) {
        estat.value = EstatCompte.CalPerfil
    }

    override suspend fun registra(correu: String, contrasenya: String): ResultatRegistre {
        estat.value = EstatCompte.CalPerfil
        return ResultatRegistre.FET
    }

    override suspend fun enviaEnllac(correu: String) = Unit

    override suspend fun recuperaContrasenya(correu: String) = Unit

    override suspend fun canviaContrasenya(nova: String) {
        estat.value = EstatCompte.Llest(PERFIL)
    }

    override suspend fun creaPerfil(nomUsuari: String) {
        estat.value = EstatCompte.Llest(Perfil(PERFIL.usuariId, nomUsuari))
    }

    override suspend fun reintenta() = Unit

    override suspend fun surt() {
        estat.value = EstatCompte.SenseSessio
    }

    override suspend fun esborraDades() {
        estat.value = EstatCompte.CalPerfil
    }

    override fun gestionaEnllac(intent: Intent) = Unit

    companion object {
        val PERFIL = Perfil("00000000-0000-0000-0000-00000000000a", "anna")
    }
}
