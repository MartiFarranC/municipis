package cat.descobreix.joc.cerca

import cat.descobreix.joc.model.Municipi
import java.text.Normalizer

/** Cerca de municipis pel nom, sense tenir en compte els accents ni els articles. */
class Cercador(private val municipis: List<Municipi>) {
    private val noms: List<Pair<Municipi, String>> = municipis.map { it to normalitza(it.nom) }

    fun cerca(text: String, limit: Int = 50): List<Municipi> {
        val q = normalitza(text)
        if (q.isEmpty()) return emptyList()
        val compacte = q.replace(" ", "")
        return noms.mapNotNull { (m, nom) ->
            val ordre = when {
                nom.startsWith(q) -> 0
                " $nom".contains(" $q") -> 1
                nom.replace(" ", "").contains(compacte) -> 2
                else -> return@mapNotNull null
            }
            Triple(m, ordre, nom)
        }
            .sortedWith(compareBy<Triple<Municipi, Int, String>> { it.second }.thenBy { it.third.length }.thenBy { it.third })
            .take(limit)
            .map { it.first }
    }

    companion object {
        // Articles en català i en aranès.
        private val ARTICLES = setOf("el", "la", "els", "les", "l", "lo", "los", "es", "eth", "era", "eths", "eras", "sa", "ses")

        fun normalitza(text: String): String {
            val senseAccents = Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD)
                .replace(Regex("\\p{M}+"), "")
            return senseAccents
                .replace("·", "")
                .replace(Regex("[^a-z0-9]+"), " ")
                .trim()
                .split(' ')
                .filter { it.isNotEmpty() && it !in ARTICLES }
                .joinToString(" ")
        }
    }
}
