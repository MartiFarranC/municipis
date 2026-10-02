package cat.descobreix.joc.dades

import cat.descobreix.joc.model.CodiIne
import cat.descobreix.joc.model.Comarca
import cat.descobreix.joc.model.Municipi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Municipis i comarques, llegits de `municipis.json` (generat per `scripts/generar-dades.js`).
 * L'ordre dels municipis és el mateix que el de `mapa.bin` i `limits.bin`.
 */
class Geografia(
    val municipis: List<Municipi>,
    val comarques: List<Comarca>,
) {
    private val perCodi: Map<CodiIne, Municipi> = municipis.associateBy { it.codi }
    private val indexPerCodi: Map<CodiIne, Int> = municipis.withIndex().associate { it.value.codi to it.index }
    private val comarcaPerCodi: Map<String, Comarca> = comarques.associateBy { it.codi }

    val total: Int get() = municipis.size

    fun municipi(codi: CodiIne): Municipi = perCodi[codi] ?: error("Municipi desconegut: $codi")

    fun municipiONull(codi: CodiIne): Municipi? = perCodi[codi]

    fun index(codi: CodiIne): Int = indexPerCodi[codi] ?: error("Municipi desconegut: $codi")

    fun comarca(codi: String): Comarca = comarcaPerCodi[codi] ?: error("Comarca desconeguda: $codi")

    fun comarcaDe(codi: CodiIne): Comarca = comarca(municipi(codi).comarca)

    fun municipisDeComarca(codiComarca: String): List<Municipi> = municipis.filter { it.comarca == codiComarca }

    /** Codis de totes les capitals de comarca (una comarca en pot tenir més d'una). */
    val capitals: Set<CodiIne> by lazy { comarques.flatMap { it.capitals }.toSet() }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun llegeix(text: String): Geografia {
            val f = json.decodeFromString(Fitxer.serializer(), text)
            return Geografia(
                municipis = f.municipis.map { Municipi(it.codi, it.nom, it.comarca, it.veins) },
                comarques = f.comarques.map { Comarca(it.codi, it.nom, it.capitals) },
            )
        }
    }

    @Serializable
    private data class Fitxer(val comarques: List<ComarcaJson>, val municipis: List<MunicipiJson>)

    @Serializable
    private data class ComarcaJson(val codi: String, val nom: String, val capitals: List<String>)

    @Serializable
    private data class MunicipiJson(val codi: String, val nom: String, val comarca: String, val veins: List<String>)
}
