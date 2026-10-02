package cat.descobreix.joc.progressio

import cat.descobreix.joc.config.ConfiguracioJoc
import cat.descobreix.joc.dades.Geografia
import cat.descobreix.joc.model.CodiIne

enum class TipusAssoliment {
    PRIMER_MUNICIPI,
    PRIMERA_MISSIO,
    MUNICIPI_COMPLET,
    COMARCA_COMPLETA,
    MUNICIPIS,
    CAPITALS,
    FOTOS,
}

/** Un assoliment amb el seu progrés. Els textos són a l'app (strings.xml), segons el tipus. */
data class Assoliment(
    val tipus: TipusAssoliment,
    val objectiu: Int,
    val actual: Int,
) {
    val id: String get() = "${tipus.name}_$objectiu"
    val aconseguit: Boolean get() = actual >= objectiu
}

data class DadesAssoliments(
    val descoberts: Set<CodiIne>,
    val missionsCompletades: Int,
    val municipisComplets: Int,
    val fotos: Int,
)

data class ProgresComarca(val codi: String, val nom: String, val descoberts: Int, val total: Int) {
    val completa: Boolean get() = descoberts == total
}

class Assoliments(
    private val config: ConfiguracioJoc.Assoliments,
    private val geografia: Geografia,
) {
    fun progresComarques(descoberts: Set<CodiIne>): List<ProgresComarca> {
        val perComarca = geografia.municipis.groupBy { it.comarca }
        return geografia.comarques.map { c ->
            val ms = perComarca[c.codi].orEmpty()
            ProgresComarca(c.codi, c.nom, ms.count { it.codi in descoberts }, ms.size)
        }
    }

    fun calcula(d: DadesAssoliments): List<Assoliment> {
        val comarquesCompletes = progresComarques(d.descoberts).count { it.completa }
        val capitals = geografia.capitals
        return buildList {
            // El municipi d'inici no compta: el primer és el primer que es desbloqueja.
            add(Assoliment(TipusAssoliment.PRIMER_MUNICIPI, 1, minOf(maxOf(d.descoberts.size - 1, 0), 1)))
            add(Assoliment(TipusAssoliment.PRIMERA_MISSIO, 1, minOf(d.missionsCompletades, 1)))
            add(Assoliment(TipusAssoliment.MUNICIPI_COMPLET, 1, minOf(d.municipisComplets, 1)))
            add(Assoliment(TipusAssoliment.COMARCA_COMPLETA, 1, minOf(comarquesCompletes, 1)))
            for (n in config.municipis) add(Assoliment(TipusAssoliment.MUNICIPIS, n, minOf(d.descoberts.size, n)))
            add(Assoliment(TipusAssoliment.CAPITALS, capitals.size, capitals.count { it in d.descoberts }))
            for (n in config.fotos) add(Assoliment(TipusAssoliment.FOTOS, n, minOf(d.fotos, n)))
        }
    }
}
