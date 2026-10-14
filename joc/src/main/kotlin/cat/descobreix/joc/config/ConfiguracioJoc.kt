package cat.descobreix.joc.config

import kotlinx.serialization.SerialName
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
    val sacs: Sacs,
    val ajuntaments: Ajuntaments,
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

    /** Les missions oficials dels ajuntaments (secció 9.6 de docs/requisits.md). */
    @Serializable
    data class Ajuntaments(
        /** Barretines que dona cada missió oficial, la posi qui la posi. */
        val puntsMissio: Int,
        /** Barretines de més per fer una missió de festa o fira durant els seus dies. */
        val bonusFesta: Int,
        /** Si un ajuntament en té més, només es fan servir les primeres. */
        val maximMissionsPerMunicipi: Int,
    )

    @Serializable
    data class Passaport(
        /** Quan una pàgina d'una comarca té aquests segells, se'n comença una altra. */
        val segellsPerPagina: Int,
    )

    /** Els sacs: què hi ha a dins i amb quina probabilitat surt cada raresa. */
    @Serializable
    data class Sacs(
        /** Percentatge de cada raresa (sumen 100). */
        val probabilitats: Map<Raresa, Int>,
        /** Punts que dona un sac quan ja no queda res per sortir. */
        val puntsSiJaTensTot: Int,
        /** Tot el que es pot tenir: el que es té des del principi i el que surt dels sacs. */
        val objectes: List<Objecte>,
    )

    @Serializable
    data class Objecte(val tipus: TipusObjecte, val id: String, val raresa: Raresa)

    @Serializable
    enum class TipusObjecte {
        @SerialName("emoji")
        EMOJI,

        /** Tapa del passaport. */
        @SerialName("portada")
        PORTADA,

        /** Animació de càrrega. */
        @SerialName("animacio")
        ANIMACIO,

        /** Color secundari de l'app. */
        @SerialName("color")
        COLOR,
    }

    @Serializable
    enum class Raresa {
        /** No surt dels sacs: la té tothom des del principi. */
        @SerialName("inicial")
        INICIAL,

        @SerialName("comuna")
        COMUNA,

        @SerialName("rara")
        RARA,

        @SerialName("llegendaria")
        LLEGENDARIA,
    }

    /** Punts que donen les missions genèriques, que es poden fer a qualsevol municipi. */
    val puntsGenericsPerMunicipi: Int get() = missionsGeneriques.sumOf { it.punts }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun llegeix(text: String): ConfiguracioJoc = json.decodeFromString(serializer(), text)
    }
}
