package cat.descobreix.joc.progressio

import cat.descobreix.joc.config.ConfiguracioJoc
import cat.descobreix.joc.config.ConfiguracioJoc.Objecte
import cat.descobreix.joc.config.ConfiguracioJoc.Raresa
import kotlin.random.Random

/** El que surt d'un sac: una cosa nova o, si ja es té tot, punts. */
sealed interface ContingutSac {
    data class Nou(val objecte: Objecte) : ContingutSac

    data class Punts(val punts: Int) : ContingutSac
}

/** El que cal saber del progrés de l'usuari per saber quins sacs ha guanyat. */
data class DadesSacs(
    val municipisDescoberts: Int,
    val missionsFetes: Int,
    val fotos: Int,
    val medalles: List<MedallaGuanyada>,
)

/**
 * Els sacs (docs/decisions-pendents.md): es guanyen les primeres vegades i amb cada nivell de les medalles de
 * comarca, i en obrir-los donen a l'atzar una cosa que encara no es té. Mai no surten repetides.
 */
class Sacs(private val config: ConfiguracioJoc.Sacs) {
    /** El que té tothom des del principi. */
    val inicials: List<Objecte> = config.objectes.filter { it.raresa == Raresa.INICIAL }

    /** Tot el que pot sortir dels sacs. */
    val delsSacs: List<Objecte> = config.objectes.filter { it.raresa != Raresa.INICIAL }

    /**
     * Els sacs guanyats, cadascun amb un id que no canvia mai: les primeres vegades (el primer municipi desbloquejat,
     * a més del d'inici; la primera missió i la primera foto) i un per cada nivell de medalla de comarca.
     */
    fun guanyats(d: DadesSacs): List<String> = buildList {
        if (d.municipisDescoberts > 1) add(PRIMER_MUNICIPI)
        if (d.missionsFetes > 0) add(PRIMERA_MISSIO)
        if (d.fotos > 0) add(PRIMERA_FOTO)
        for (m in d.medalles) if (m.id.startsWith(PREFIX_COMARCA)) add(PREFIX_MEDALLA + m.id)
    }

    /**
     * Obre un sac. Primer es tria la raresa amb les probabilitats de la configuració (només entre les rareses de
     * què encara queda alguna cosa) i després una cosa d'aquesta raresa que encara no es tingui.
     * @param tinc les coses que ja es tenen (les inicials no cal passar-les).
     */
    fun obre(tinc: Collection<Objecte>, atzar: Random): ContingutSac {
        val tincSet = tinc.toSet()
        val queden = delsSacs.filter { it !in tincSet }.groupBy { it.raresa }
        if (queden.isEmpty()) return ContingutSac.Punts(config.puntsSiJaTensTot)
        val pesos = queden.keys.associateWith { config.probabilitats[it] ?: 0 }.filterValues { it > 0 }
        val raresa = if (pesos.isEmpty()) {
            queden.keys.first()
        } else {
            var x = atzar.nextInt(pesos.values.sum())
            pesos.entries.first { (_, p) -> (x - p).also { x = it } < 0 }.key
        }
        return ContingutSac.Nou(queden.getValue(raresa).random(atzar))
    }

    companion object {
        const val PRIMER_MUNICIPI = "primer_municipi"
        const val PRIMERA_MISSIO = "primera_missio"
        const val PRIMERA_FOTO = "primera_foto"
        const val PREFIX_MEDALLA = "medalla_"
        private const val PREFIX_COMARCA = "comarca_"
    }
}
