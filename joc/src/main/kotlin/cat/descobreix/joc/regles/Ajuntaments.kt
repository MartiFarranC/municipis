package cat.descobreix.joc.regles

import cat.descobreix.joc.config.ConfiguracioJoc
import cat.descobreix.joc.model.CodiIne
import cat.descobreix.joc.model.FontMissio
import cat.descobreix.joc.model.Missio
import cat.descobreix.joc.model.TipusMissio
import cat.descobreix.joc.model.TipusProva
import java.security.MessageDigest
import java.time.LocalDate

/** Una missió oficial tal com la posa un ajuntament al servidor (secció 9.6 de docs/requisits.md). */
data class MissioAjuntament(
    /** L'UUID del servidor. */
    val id: String,
    val municipi: CodiIne,
    val titol: String,
    val descripcio: String?,
    val prova: TipusProva,
    val lat: Double?,
    val lon: Double?,
    /** SHA-256 (hexadecimal) del text del codi QR, per a les de QR. */
    val resumQr: String?,
    /** Els dies d'una festa o fira (inclosos), si en té. */
    val dataInici: LocalDate?,
    val dataFi: LocalDate?,
    val ordre: Int,
    val retirada: Boolean,
) {
    val esFesta: Boolean get() = dataInici != null && dataFi != null

    /** L'identificador amb què es desa com a completada, perquè no es barregi amb les automàtiques. */
    val idMissio: String get() = PREFIX + id

    companion object {
        const val PREFIX = "aj-"
    }
}

/** Si una missió oficial es pot fer avui. */
sealed interface DisponibilitatOficial {
    data object Disponible : DisponibilitatOficial

    /** Una festa que encara no ha començat. */
    data class Abans(val inici: LocalDate, val fi: LocalDate) : DisponibilitatOficial

    /** Una festa que ja ha passat. */
    data class Passada(val inici: LocalDate, val fi: LocalDate) : DisponibilitatOficial
}

/** Regles de les missions oficials dels ajuntaments. */
class ReglesAjuntaments(private val config: ConfiguracioJoc) {
    private val c get() = config.ajuntaments

    /**
     * Les missions d'un municipi que es fan servir: les no retirades, per ordre, com a molt les que diu la
     * configuració. Les festes que ja han passat només hi són si s'havien fet.
     */
    fun missionsDe(codi: CodiIne, totes: List<MissioAjuntament>, avui: LocalDate, completades: Set<String>): List<MissioAjuntament> =
        totes.asSequence()
            .filter { it.municipi == codi && !it.retirada }
            .sortedWith(compareBy({ it.ordre }, { it.id }))
            .take(c.maximMissionsPerMunicipi)
            .filter { disponibilitat(it, avui) !is DisponibilitatOficial.Passada || it.idMissio in completades }
            .toList()

    fun disponibilitat(m: MissioAjuntament, avui: LocalDate): DisponibilitatOficial {
        val inici = m.dataInici
        val fi = m.dataFi
        return when {
            inici == null || fi == null -> DisponibilitatOficial.Disponible
            avui < inici -> DisponibilitatOficial.Abans(inici, fi)
            avui > fi -> DisponibilitatOficial.Passada(inici, fi)
            else -> DisponibilitatOficial.Disponible
        }
    }

    /** Barretines que dona: les de la configuració, més el bonus si és una festa. */
    fun punts(m: MissioAjuntament): Int = c.puntsMissio + if (m.esFesta) c.bonusFesta else 0

    /**
     * La missió com a [Missio], per fer-la servir com les altres (validar-la i completar-la).
     * No compta per al bonus de completar-les totes: les regles generals només miren les missions automàtiques.
     */
    fun comMissio(m: MissioAjuntament): Missio = Missio(
        id = m.idMissio,
        municipi = m.municipi,
        titol = m.titol,
        tipus = TipusMissio.OFICIAL,
        clau = null,
        categoria = null,
        lat = m.lat,
        lon = m.lon,
        prova = m.prova,
        punts = punts(m),
        font = FontMissio("ajuntament", m.id),
    )

    /** Si el text llegit d'un codi QR és el del punt de segellat de [m]. */
    fun qrValid(m: MissioAjuntament, text: String): Boolean {
        val resum = m.resumQr ?: return false
        return resum.equals(resumQr(text), ignoreCase = true)
    }

    companion object {
        /** SHA-256 en hexadecimal del text del QR, sense espais al principi ni al final. */
        fun resumQr(text: String): String =
            MessageDigest.getInstance("SHA-256").digest(text.trim().toByteArray(Charsets.UTF_8))
                .joinToString("") { "%02x".format(it) }
    }
}
