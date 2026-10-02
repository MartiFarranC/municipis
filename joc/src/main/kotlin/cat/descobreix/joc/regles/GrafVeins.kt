package cat.descobreix.joc.regles

import cat.descobreix.joc.model.CodiIne
import cat.descobreix.joc.model.Municipi

/** Relació de veïnatge entre municipis (frontera + connexions especials, com Llívia–Puigcerdà). */
class GrafVeins(municipis: List<Municipi>) {
    private val veins: Map<CodiIne, Set<CodiIne>> = municipis.associate { it.codi to it.veins.toSet() }

    val codis: Set<CodiIne> get() = veins.keys

    fun veins(codi: CodiIne): Set<CodiIne> = veins[codi].orEmpty()

    /** Municipis no descoberts que fan frontera amb algun dels descoberts. */
    fun disponibles(descoberts: Set<CodiIne>): Set<CodiIne> =
        descoberts.flatMapTo(mutableSetOf()) { veins(it) } - descoberts

    /**
     * Distància (en salts de veí a veí) de cada municipi al territori descobert.
     * Els descoberts són a distància 0 i els disponibles a distància 1.
     */
    fun distancies(descoberts: Set<CodiIne>): Map<CodiIne, Int> {
        val dist = HashMap<CodiIne, Int>()
        val cua = ArrayDeque<CodiIne>()
        for (c in descoberts) {
            dist[c] = 0
            cua.add(c)
        }
        while (cua.isNotEmpty()) {
            val c = cua.removeFirst()
            val d = dist.getValue(c)
            for (v in veins(c)) if (v !in dist) {
                dist[v] = d + 1
                cua.add(v)
            }
        }
        return dist
    }

    /** Si des de qualsevol municipi es pot arribar a tots els altres. */
    fun esConnex(): Boolean {
        val primer = veins.keys.firstOrNull() ?: return true
        return distancies(setOf(primer)).size == veins.size
    }
}
