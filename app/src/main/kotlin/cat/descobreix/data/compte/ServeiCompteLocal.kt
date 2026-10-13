package cat.descobreix.data.compte

import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * El compte quan l'app no té servidor configurat (sense `supabase.url` a local.properties): per provar-la des de
 * l'Android Studio sense res més (docs/android-studio.md). Ja hi ha sessió des del principi, amb un perfil local;
 * no es sincronitza res ni hi ha la part social. Totes les dades del joc es desen al mòbil igualment.
 */
class ServeiCompteLocal : ServeiCompte {
    private val _estat = MutableStateFlow<EstatCompte>(EstatCompte.Llest(PERFIL))
    override val estat: StateFlow<EstatCompte> = _estat.asStateFlow()

    override suspend fun entra(correu: String, contrasenya: String) = Unit

    override suspend fun registra(correu: String, contrasenya: String): ResultatRegistre = ResultatRegistre.FET

    override suspend fun enviaEnllac(correu: String) = Unit

    override suspend fun recuperaContrasenya(correu: String) = Unit

    override suspend fun canviaContrasenya(nova: String) = Unit

    override suspend fun creaPerfil(nomUsuari: String, tipus: TipusPerfil, public: Boolean) {
        _estat.value = EstatCompte.Llest(Perfil(PERFIL.usuariId, nomUsuari, tipus, public))
    }

    override suspend fun triaVisibilitat(public: Boolean) {
        val actual = (_estat.value as? EstatCompte.Llest)?.perfil ?: PERFIL
        _estat.value = EstatCompte.Llest(actual.copy(public = public))
    }

    override suspend fun reintenta() = Unit

    // Sense servidor no hi ha cap altre compte on anar: la sessió es queda.
    override suspend fun surt() = Unit

    override suspend fun esborraDades() = Unit

    override fun gestionaEnllac(intent: Intent) = Unit

    companion object {
        val PERFIL = Perfil("00000000-0000-0000-0000-000000000000", "prova")
    }
}
