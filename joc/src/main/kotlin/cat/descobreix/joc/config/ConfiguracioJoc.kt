package cat.descobreix.joc.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Totes les regles i els valors ajustables del joc. Es llegeixen de `configuracio_joc.json`
 * (a `dades/` del repositori, i copiat als assets de l'app per l'script de dades), que és
 * l'únic lloc on es defineixen. Els tests de `ConfiguracioJocTest` en comproven la coherència.
 */
@Serializable
data class ConfiguracioJoc(
    val versio: Int,
    val desbloqueig: Desbloqueig,
    val punts: Punts,
    val missionsGeneriques: List<MissioGenerica>,
    val categoriesLloc: Map<String, CategoriaLloc>,
    val gps: Gps,
    val fotos: Fotos,
    val nivells: Nivells,
    val medalles: Medalles,
    val passaport: Passaport,
) {
    @Serializable
    data class Desbloqueig(
        val costBase: Int,
        val costIncrementPerMunicipi: Int,
        val costMaxim: Int,
    )

    @Serializable
    data class Punts(
        val minimGarantitPerMunicipi: Int,
        val maximMissionsPerMunicipi: Int,
        val bonusTotesLesMissions: Int,
        val minimPerMissioLloc: Int,
        val maximMissionsLlocPerMunicipi: Int,
    )

    @Serializable
    data class MissioGenerica(
        val clau: String,
        val titol: String,
        val prova: String,
        val punts: Int,
    )

    @Serializable
    data class CategoriaLloc(
        val punts: Int,
        val prova: String,
        val prioritat: Int,
    )

    @Serializable
    data class Gps(
        val radiMissioMetres: Double,
        val precisioMaximaMetres: Double,
        val margeFronteraMetres: Double,
    )

    @Serializable
    data class Fotos(
        val costatLlargMaxim: Int,
        val costatMiniatura: Int,
        val qualitatJpeg: Int,
    )

    @Serializable
    data class Nivells(
        val puntsPrimerNivell: Int,
        val increment: Int,
    )

    @Serializable
    data class Medalles(
        /** Fites de municipis descoberts. */
        val municipis: List<Int>,
        /** Fites de fotos del cartell d'entrada. */
        val cartells: List<Int>,
        val punts: PuntsMedalles,
    )

    /** Punts que dona cada medalla (o cada nivell de les de comarca). */
    @Serializable
    data class PuntsMedalles(
        val comarcaBronze: Int,
        val comarcaPlata: Int,
        val comarcaOr: Int,
        val fitaMunicipis: Int,
        val fitaCartells: Int,
        val capitals: Int,
    )

    @Serializable
    data class Passaport(
        /** Quan una pàgina d'una comarca té aquests segells, se'n comença una altra. */
        val segellsPerPagina: Int,
    )

    /** Punts que donen les missions genèriques, que es poden fer a qualsevol municipi. */
    val puntsGenericsPerMunicipi: Int get() = missionsGeneriques.sumOf { it.punts }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun llegeix(text: String): ConfiguracioJoc = json.decodeFromString(serializer(), text)
    }
}
