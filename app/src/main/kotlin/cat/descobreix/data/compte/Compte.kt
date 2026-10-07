package cat.descobreix.data.compte

import android.content.Intent
import kotlinx.coroutines.flow.StateFlow

// Comptes d'usuari (docs/requisits.md, secció 9.1). El compte és obligatori: l'app només
// deixa jugar quan l'estat és [EstatCompte.Llest].

/** Explorador (juga) o Espectador (segueix altra gent). Es tria en crear el compte i és per sempre. */
enum class TipusPerfil { EXPLORADOR, ESPECTADOR }

/** @param public si el compte és públic (qualsevol el pot seguir directament) o privat (cal acceptar-ho). */
data class Perfil(
    val usuariId: String,
    val nomUsuari: String,
    val tipus: TipusPerfil = TipusPerfil.EXPLORADOR,
    val public: Boolean = false,
)

sealed interface EstatCompte {
    /** Encara es llegeix la sessió desada. */
    data object Carregant : EstatCompte

    /** No hi ha cap sessió iniciada. */
    data object SenseSessio : EstatCompte

    /** Hi ha sessió, però l'usuari encara no té perfil en aquesta app (ha de triar el nom d'usuari). */
    data object CalPerfil : EstatCompte

    /** Hi ha sessió, però cal connexió per saber si l'usuari té perfil. */
    data object SenseConnexio : EstatCompte

    /** L'usuari ha obert l'enllaç per recuperar la contrasenya i n'ha de triar una de nova. */
    data object CalNovaContrasenya : EstatCompte

    /** El perfil és d'abans dels comptes públics i privats: cal triar-ho (no hi ha cap opció per defecte). */
    data class CalVisibilitat(val nomUsuari: String) : EstatCompte

    data class Llest(val perfil: Perfil) : EstatCompte
}

enum class ErrorCompte {
    CREDENCIALS_INCORRECTES,
    CORREU_NO_CONFIRMAT,
    CORREU_JA_REGISTRAT,
    CORREU_INVALID,
    CONTRASENYA_FEBLE,
    MATEIXA_CONTRASENYA,
    MASSA_INTENTS,
    NOM_USUARI_AGAFAT,
    NOM_USUARI_INVALID,
    SENSE_CONNEXIO,
    DESCONEGUT,
}

class ExcepcioCompte(val error: ErrorCompte, causa: Throwable? = null) : Exception(error.name, causa)

enum class ResultatRegistre {
    /** El compte s'ha creat i ja hi ha sessió. */
    FET,

    /** Cal confirmar el correu amb l'enllaç que s'hi ha enviat. */
    CAL_CONFIRMAR_CORREU,
}

/** Totes les operacions llancen [ExcepcioCompte] si fallen. */
interface ServeiCompte {
    val estat: StateFlow<EstatCompte>

    suspend fun entra(correu: String, contrasenya: String)

    suspend fun registra(correu: String, contrasenya: String): ResultatRegistre

    /** Envia un enllaç per entrar sense contrasenya. */
    suspend fun enviaEnllac(correu: String)

    /** Envia un enllaç per triar una contrasenya nova. */
    suspend fun recuperaContrasenya(correu: String)

    suspend fun canviaContrasenya(nova: String)

    suspend fun creaPerfil(nomUsuari: String, tipus: TipusPerfil, public: Boolean)

    /** Canvia si el compte és públic o privat. */
    suspend fun triaVisibilitat(public: Boolean)

    /** Torna a provar de llegir el perfil (per exemple, quan torna la connexió). */
    suspend fun reintenta()

    suspend fun surt()

    /**
     * Esborra totes les dades d'aquesta app, al servidor i al mòbil. No esborra el compte, que és
     * compartit amb altres apps (secció 9.0). Cal connexió.
     */
    suspend fun esborraDades()

    /** Gestiona els enllaços dels correus (confirmació, enllaç màgic i recuperació de contrasenya). */
    fun gestionaEnllac(intent: Intent)
}

/** Normes del nom d'usuari. La comprovació que mana és la de la base de dades (taula descobreix.perfils). */
object NomUsuari {
    const val MIN = 3
    const val MAX = 20
    private val patro = Regex("^[a-z0-9_]{$MIN,$MAX}$")

    /** Passa a minúscules i treu els espais dels extrems. */
    fun normalitza(nom: String): String = nom.trim().lowercase()

    fun esValid(nom: String): Boolean = patro.matches(nom)
}

/** Una contrasenya ha de tenir com a mínim aquests caràcters (el mínim per defecte de Supabase). */
const val CONTRASENYA_MINIM = 6
