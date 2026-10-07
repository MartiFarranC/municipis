package cat.descobreix.joc.progressio

import cat.descobreix.joc.config.ConfiguracioJoc
import cat.descobreix.joc.dades.Geografia
import cat.descobreix.joc.dades.Missions
import cat.descobreix.joc.model.CodiIne

enum class NivellMedalla { BRONZE, PLATA, OR }

enum class TipusMedalla {
    /** Una per comarca, amb tres nivells. */
    COMARCA,

    /** Fites de municipis descoberts. */
    MUNICIPIS,

    /** Totes les capitals de comarca descobertes. */
    CAPITALS,

    /** Fites de fotos del cartell d'entrada. */
    CARTELLS,
}

/**
 * Una medalla de la vitrina. Les de comarca tenen tres nivells; les altres, quan s'aconsegueixen, són d'or.
 * [actual] i [necessari] són el progrés cap al nivell següent (o cap a la medalla, si encara no se'n té cap).
 * Els textos són a l'app (strings.xml), segons el tipus.
 */
data class Medalla(
    val tipus: TipusMedalla,
    /** Codi de la comarca, només per a les de comarca. */
    val comarca: String?,
    /** La fita (municipis o cartells), o el total de capitals o de municipis de la comarca. */
    val objectiu: Int,
    val nivell: NivellMedalla?,
    val actual: Int,
    val necessari: Int,
) {
    val aconseguida: Boolean get() = nivell != null
    val completa: Boolean get() = nivell == NivellMedalla.OR
}

/** Una medalla (o un nivell d'una de comarca) guanyada, amb els punts que dona. L'id no canvia mai. */
data class MedallaGuanyada(val id: String, val punts: Int)

data class ProgresComarca(val codi: String, val nom: String, val descoberts: Int, val total: Int) {
    val completa: Boolean get() = descoberts == total
}

/** El que cal saber del progrés de l'usuari per calcular les medalles. */
data class DadesMedalles(
    val descoberts: Set<CodiIne>,
    /** Municipis amb totes les missions automàtiques fetes. */
    val complets: Set<CodiIne>,
    /** Municipis amb la foto del cartell d'entrada. */
    val cartells: Set<CodiIne>,
)

class Medalles(
    private val config: ConfiguracioJoc.Medalles,
    private val geografia: Geografia,
    private val missions: Missions,
) {
    /** Calcula les dades a partir dels municipis descoberts i les missions completades. */
    fun dades(descoberts: Set<CodiIne>, completades: Set<String>): DadesMedalles {
        val complets = descoberts.filterTo(mutableSetOf()) { codi ->
            val ms = missions.de(codi)
            ms.isNotEmpty() && ms.all { it.id in completades }
        }
        val cartells = descoberts.filterTo(mutableSetOf()) { codi ->
            missions.de(codi).any { it.clau == CLAU_CARTELL && it.id in completades }
        }
        return DadesMedalles(descoberts, complets, cartells)
    }

    fun progresComarques(descoberts: Set<CodiIne>): List<ProgresComarca> {
        val perComarca = geografia.municipis.groupBy { it.comarca }
        return geografia.comarques.map { c ->
            val ms = perComarca[c.codi].orEmpty()
            ProgresComarca(c.codi, c.nom, ms.count { it.codi in descoberts }, ms.size)
        }
    }

    /** Totes les medalles, aconseguides o no, en l'ordre de la vitrina. */
    fun calcula(d: DadesMedalles): List<Medalla> = buildList {
        val perComarca = geografia.municipis.groupBy { it.comarca }
        for (c in geografia.comarques) {
            val codis = perComarca[c.codi].orEmpty().map { it.codi }
            add(medallaComarca(c.codi, codis, d))
        }
        for (n in config.municipis) add(fita(TipusMedalla.MUNICIPIS, n, d.descoberts.size))
        val capitals = geografia.capitals
        add(fita(TipusMedalla.CAPITALS, capitals.size, capitals.count { it in d.descoberts }))
        for (n in config.cartells) add(fita(TipusMedalla.CARTELLS, n, d.cartells.size))
    }

    /** Les medalles (i els nivells de les de comarca) guanyades, amb els punts de cadascuna. */
    fun guanyades(medalles: List<Medalla>): List<MedallaGuanyada> = medalles.flatMap { guanyades(it) }

    /** El que ha guanyat una medalla: res, si encara no s'ha aconseguit; a les de comarca, un per nivell. */
    fun guanyades(m: Medalla): List<MedallaGuanyada> {
        val p = config.punts
        val nivell = m.nivell ?: return emptyList()
        return when (m.tipus) {
            TipusMedalla.COMARCA -> NivellMedalla.entries.filter { it <= nivell }.map {
                val punts = when (it) {
                    NivellMedalla.BRONZE -> p.comarcaBronze
                    NivellMedalla.PLATA -> p.comarcaPlata
                    NivellMedalla.OR -> p.comarcaOr
                }
                MedallaGuanyada("comarca_${m.comarca}_${it.name.lowercase()}", punts)
            }
            TipusMedalla.MUNICIPIS -> listOf(MedallaGuanyada("municipis_${m.objectiu}", p.fitaMunicipis))
            TipusMedalla.CAPITALS -> listOf(MedallaGuanyada("capitals", p.capitals))
            TipusMedalla.CARTELLS -> listOf(MedallaGuanyada("cartells_${m.objectiu}", p.fitaCartells))
        }
    }

    private fun medallaComarca(codi: String, codis: List<CodiIne>, d: DadesMedalles): Medalla {
        val total = codis.size
        // Cada nivell demana l'anterior: bronze (tots descoberts), plata (a més, totes les missions), or (a més, tots els cartells).
        val requisits = listOf(
            NivellMedalla.BRONZE to codis.count { it in d.descoberts },
            NivellMedalla.PLATA to codis.count { it in d.complets },
            NivellMedalla.OR to codis.count { it in d.cartells },
        )
        var nivell: NivellMedalla? = null
        var actual = requisits.first().second
        for ((n, fets) in requisits) {
            if (fets < total) {
                actual = fets
                break
            }
            nivell = n
            actual = fets
        }
        return Medalla(TipusMedalla.COMARCA, codi, total, nivell, actual, total)
    }

    private fun fita(tipus: TipusMedalla, objectiu: Int, actual: Int) =
        Medalla(tipus, null, objectiu, if (actual >= objectiu) NivellMedalla.OR else null, minOf(actual, objectiu), objectiu)

    companion object {
        /** Clau de la missió genèrica de la foto del cartell d'entrada. */
        const val CLAU_CARTELL = "cartell"
    }
}
