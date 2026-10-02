package cat.descobreix.joc.dades

import cat.descobreix.joc.model.CodiIne
import cat.descobreix.joc.model.FontMissio
import cat.descobreix.joc.model.Missio
import cat.descobreix.joc.model.TipusMissio
import cat.descobreix.joc.model.TipusProva
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Missions automàtiques, llegides de `missions.json` (generat per `scripts/generar-missions.js`). */
class Missions(val totes: List<Missio>, val fonts: List<String>) {
    private val perMunicipi: Map<CodiIne, List<Missio>> = totes.groupBy { it.municipi }
    private val perId: Map<String, Missio> = totes.associateBy { it.id }

    fun de(codi: CodiIne): List<Missio> = perMunicipi[codi].orEmpty()

    fun missio(id: String): Missio? = perId[id]

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun llegeix(text: String): Missions {
            val f = json.decodeFromString(Fitxer.serializer(), text)
            return Missions(
                totes = f.missions.map {
                    Missio(
                        id = it.id,
                        municipi = it.municipi,
                        titol = it.titol,
                        tipus = TipusMissio.valueOf(it.tipus),
                        clau = it.clau,
                        categoria = it.categoria,
                        lat = it.lat,
                        lon = it.lon,
                        prova = TipusProva.valueOf(it.prova),
                        punts = it.punts,
                        font = FontMissio(it.font.tipus, it.font.id),
                    )
                },
                fonts = f.fonts,
            )
        }
    }

    @Serializable
    private data class Fitxer(val fonts: List<String> = emptyList(), val missions: List<MissioJson>)

    @Serializable
    private data class MissioJson(
        val id: String,
        val municipi: String,
        val titol: String,
        val tipus: String,
        val clau: String? = null,
        val categoria: String? = null,
        val lat: Double? = null,
        val lon: Double? = null,
        val prova: String,
        val punts: Int,
        val font: FontJson,
    )

    @Serializable
    private data class FontJson(val tipus: String, val id: String? = null)
}
