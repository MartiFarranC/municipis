package cat.descobreix.data.compte

import android.content.Intent
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import cat.descobreix.data.exportacio.GestioDades
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.handleDeeplinks
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServeiCompteSupabase @Inject constructor(
    private val supabase: SupabaseClient,
    private val dataStore: DataStore<Preferences>,
    private val gestio: GestioDades,
) : ServeiCompte {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutex = Mutex()
    private val _estat = MutableStateFlow<EstatCompte>(EstatCompte.Carregant)
    override val estat: StateFlow<EstatCompte> = _estat.asStateFlow()

    /** L'usuari ha obert l'enllaç de recuperar la contrasenya i encara no n'ha triat una de nova. */
    @Volatile
    private var recuperant = false

    init {
        scope.launch {
            supabase.auth.sessionStatus.collect { avalua(it) }
        }
    }

    override suspend fun entra(correu: String, contrasenya: String) = ambErrors {
        supabase.auth.signInWith(Email) {
            email = correu.trim()
            password = contrasenya
        }
    }

    override suspend fun registra(correu: String, contrasenya: String): ResultatRegistre = ambErrors {
        supabase.auth.signUpWith(Email, redirectUrl = ENLLAC_APP) {
            email = correu.trim()
            password = contrasenya
        }
        if (supabase.auth.currentSessionOrNull() != null) ResultatRegistre.FET else ResultatRegistre.CAL_CONFIRMAR_CORREU
    }

    override suspend fun enviaEnllac(correu: String) = ambErrors {
        supabase.auth.signInWith(OTP, redirectUrl = ENLLAC_APP) {
            email = correu.trim()
            createUser = true
        }
    }

    override suspend fun recuperaContrasenya(correu: String) = ambErrors {
        supabase.auth.resetPasswordForEmail(correu.trim(), redirectUrl = ENLLAC_RECUPERACIO)
    }

    override suspend fun canviaContrasenya(nova: String) {
        ambErrors { supabase.auth.updateUser { password = nova } }
        recuperant = false
        reintenta()
    }

    override suspend fun creaPerfil(nomUsuari: String) {
        val nom = NomUsuari.normalitza(nomUsuari)
        if (!NomUsuari.esValid(nom)) throw ExcepcioCompte(ErrorCompte.NOM_USUARI_INVALID)
        val id = supabase.auth.currentUserOrNull()?.id ?: throw ExcepcioCompte(ErrorCompte.DESCONEGUT)
        ambErrors { supabase.postgrest.from(PERFILS).insert(PerfilRemot(id, nom)) }
        reintenta()
    }

    override suspend fun reintenta() = avalua(supabase.auth.sessionStatus.value)

    override suspend fun surt() {
        try {
            supabase.auth.signOut()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Sense connexió no es pot avisar el servidor, però la sessió es tanca igualment al mòbil.
            Log.w(TAG, "No s'ha pogut tancar la sessió al servidor", e)
            supabase.auth.clearSession()
        }
    }

    override suspend fun esborraDades() {
        ambErrors { supabase.postgrest.rpc("esborra_dades") }
        gestio.esborraTot()
        reintenta()
    }

    override fun gestionaEnllac(intent: Intent) {
        val dades = intent.data ?: return
        if (dades.scheme != ESQUEMA || dades.host != AMFITRIO) return
        if (dades.path == CAMI_RECUPERACIO) recuperant = true
        supabase.handleDeeplinks(intent, onError = { Log.w(TAG, "No s'ha pogut obrir l'enllaç", it) })
    }

    private suspend fun avalua(status: SessionStatus) = mutex.withLock {
        _estat.value = when (status) {
            SessionStatus.Initializing -> EstatCompte.Carregant
            is SessionStatus.NotAuthenticated -> EstatCompte.SenseSessio
            // La sessió ha caducat i no s'ha pogut renovar (normalment, sense connexió): es continua
            // jugant amb el perfil desat.
            is SessionStatus.RefreshFailure -> perfilDesat()?.let { EstatCompte.Llest(it) } ?: EstatCompte.SenseConnexio
            is SessionStatus.Authenticated -> {
                val id = status.session.user?.id
                when {
                    id == null -> EstatCompte.SenseConnexio
                    recuperant -> EstatCompte.CalNovaContrasenya
                    else -> estatAmbSessio(id)
                }
            }
        }
    }

    private suspend fun estatAmbSessio(id: String): EstatCompte {
        perfilDesat()?.takeIf { it.usuariId == id }?.let { return EstatCompte.Llest(it) }
        val remot = try {
            supabase.postgrest.from(PERFILS).select { filter { eq("id", id) } }.decodeSingleOrNull<PerfilRemot>()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "No s'ha pogut llegir el perfil", e)
            return EstatCompte.SenseConnexio
        }
        adoptaDades(id)
        if (remot == null) return EstatCompte.CalPerfil
        val perfil = Perfil(id, remot.nomUsuari)
        dataStore.edit {
            it[CLAU_USUARI] = perfil.usuariId
            it[CLAU_NOM] = perfil.nomUsuari
        }
        return EstatCompte.Llest(perfil)
    }

    /**
     * Les dades del mòbil són d'un sol usuari. Si no són de ningú (d'abans dels comptes), passen a ser
     * d'aquest usuari (secció 9.1). Si són d'un altre usuari, s'esborren.
     */
    private suspend fun adoptaDades(id: String) {
        val propietari = dataStore.data.first()[CLAU_PROPIETARI]
        if (propietari != null && propietari != id) gestio.esborraTot()
        dataStore.edit { it[CLAU_PROPIETARI] = id }
    }

    private suspend fun perfilDesat(): Perfil? {
        val p = dataStore.data.first()
        val id = p[CLAU_USUARI] ?: return null
        val nom = p[CLAU_NOM] ?: return null
        return Perfil(id, nom)
    }

    private inline fun <T> ambErrors(bloc: () -> T): T = try {
        bloc()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        throw ExcepcioCompte(errorDe(e), e)
    }

    @Serializable
    private data class PerfilRemot(val id: String, @SerialName("nom_usuari") val nomUsuari: String)

    companion object {
        private const val TAG = "Compte"
        private const val PERFILS = "perfils"

        // Ha de coincidir amb l'intent-filter de MainActivity i amb les URL de redirecció de Supabase.
        const val ESQUEMA = "cat.descobreix"
        const val AMFITRIO = "login"
        private const val CAMI_RECUPERACIO = "/recuperacio"
        // Supabase només hi redirigeix si són a Authentication → URL Configuration → Redirect URLs.
        // Si no, obre la Site URL del projecte compartit (localhost:3000).
        private const val ENLLAC_APP = "$ESQUEMA://$AMFITRIO"
        private const val ENLLAC_RECUPERACIO = "$ENLLAC_APP$CAMI_RECUPERACIO"

        private val CLAU_USUARI = stringPreferencesKey("compte_usuari_id")
        private val CLAU_NOM = stringPreferencesKey("compte_nom_usuari")
        private val CLAU_PROPIETARI = stringPreferencesKey("dades_propietari")

        internal fun errorDe(e: Exception): ErrorCompte = when (e) {
            is ExcepcioCompte -> e.error
            is AuthRestException -> when (e.errorCode) {
                AuthErrorCode.InvalidCredentials -> ErrorCompte.CREDENCIALS_INCORRECTES
                AuthErrorCode.EmailNotConfirmed -> ErrorCompte.CORREU_NO_CONFIRMAT
                AuthErrorCode.UserAlreadyExists, AuthErrorCode.EmailExists -> ErrorCompte.CORREU_JA_REGISTRAT
                AuthErrorCode.EmailAddressInvalid -> ErrorCompte.CORREU_INVALID
                AuthErrorCode.WeakPassword -> ErrorCompte.CONTRASENYA_FEBLE
                AuthErrorCode.SamePassword -> ErrorCompte.MATEIXA_CONTRASENYA
                AuthErrorCode.OverRequestRateLimit, AuthErrorCode.OverEmailSendRateLimit -> ErrorCompte.MASSA_INTENTS
                else -> ErrorCompte.DESCONEGUT
            }
            is PostgrestRestException -> when (e.code) {
                "23505" -> ErrorCompte.NOM_USUARI_AGAFAT
                "23514" -> ErrorCompte.NOM_USUARI_INVALID
                else -> ErrorCompte.DESCONEGUT
            }
            is IOException -> ErrorCompte.SENSE_CONNEXIO
            else -> ErrorCompte.DESCONEGUT
        }
    }
}
