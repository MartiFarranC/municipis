package cat.descobreix.data.ranquing

// Rànquing (docs/requisits.md, secció 9.3). Es calcula al servidor, amb la funció
// descobreix.ranquing, a partir del progrés que l'app hi ha pujat.

/** Posicions que es mostren. La de l'usuari s'hi afegeix si no hi és. */
const val LIMIT_RANQUING = 50

enum class CriteriRanquing { PUNTS, MUNICIPIS }

data class FilaRanquing(
    /** Els empats comparteixen posició. */
    val posicio: Int,
    val usuariId: String,
    val nomUsuari: String,
    /** Ruta de la foto de perfil al bucket descobreix-avatars, o null si no en té. */
    val foto: String?,
    val punts: Long,
    val municipis: Long,
    val socJo: Boolean,
)

/** Totes les operacions llancen una excepció si fallen (per exemple, sense connexió). */
interface ServeiRanquing {
    /**
     * Les primeres posicions de la classificació i, si no hi és, també la de l'usuari.
     * Amb [nomesAmics], només l'usuari i els seus amics.
     */
    suspend fun classificacio(criteri: CriteriRanquing, nomesAmics: Boolean): List<FilaRanquing>

    /** Puja el progrés que encara no és al servidor, perquè el rànquing el tingui en compte. */
    suspend fun pujaProgres()
}
