package cat.descobreix.joc.regles

import cat.descobreix.joc.config.ConfiguracioJoc
import cat.descobreix.joc.dades.Missions
import cat.descobreix.joc.model.CodiIne
import cat.descobreix.joc.model.EstatMunicipi
import cat.descobreix.joc.model.Missio
import cat.descobreix.joc.model.TipusMissio

/** Estat del progrés de l'usuari necessari per aplicar les regles. */
data class EstatJoc(
    val descoberts: Set<CodiIne>,
    /** Punts guanyats en total (missions i bonus). Els gastats també hi compten. */
    val puntsGuanyats: Int,
    val puntsGastats: Int,
    /** Identificadors de les missions automàtiques completades. */
    val missionsCompletades: Set<String>,
) {
    val saldo: Int get() = puntsGuanyats - puntsGastats

    companion object {
        val BUIT = EstatJoc(emptySet(), 0, 0, emptySet())
    }
}

sealed interface ResultatDesbloqueig {
    data class Permes(val cost: Int) : ResultatDesbloqueig

    data object JaDescobert : ResultatDesbloqueig

    /** No fa frontera amb cap municipi descobert. */
    data class NoDisponible(val distancia: Int?) : ResultatDesbloqueig

    data class PuntsInsuficients(val cost: Int, val falten: Int) : ResultatDesbloqueig
}

/** Punts que dona completar una missió, i si amb ella es completen totes les del municipi. */
data class PuntsMissio(val missio: Int, val bonus: Int) {
    val total: Int get() = missio + bonus
}

/** Regles del joc (secció 3 de docs/requisits.md). */
class ReglesJoc(
    val config: ConfiguracioJoc,
    val graf: GrafVeins,
    val missions: Missions,
) {
    /** Cost de desbloquejar un municipi quan ja se'n tenen [descoberts]. */
    fun cost(descoberts: Int): Int {
        val d = config.desbloqueig
        return minOf(d.costBase + d.costIncrementPerMunicipi * (maxOf(descoberts, 1) - 1), d.costMaxim)
    }

    fun estat(codi: CodiIne, descoberts: Set<CodiIne>): EstatMunicipi = when {
        codi in descoberts -> EstatMunicipi.DESCOBERT
        graf.veins(codi).any { it in descoberts } -> EstatMunicipi.DISPONIBLE
        else -> EstatMunicipi.BOIRA
    }

    fun avaluaDesbloqueig(codi: CodiIne, estat: EstatJoc): ResultatDesbloqueig = when (estat(codi, estat.descoberts)) {
        EstatMunicipi.DESCOBERT -> ResultatDesbloqueig.JaDescobert
        EstatMunicipi.BOIRA -> ResultatDesbloqueig.NoDisponible(graf.distancies(estat.descoberts)[codi])
        EstatMunicipi.DISPONIBLE -> {
            val cost = cost(estat.descoberts.size)
            if (estat.saldo >= cost) {
                ResultatDesbloqueig.Permes(cost)
            } else {
                ResultatDesbloqueig.PuntsInsuficients(cost, cost - estat.saldo)
            }
        }
    }

    /** Punts que es poden guanyar en un municipi: totes les missions més el bonus. */
    fun puntsPossibles(codi: CodiIne): Int {
        val ms = missions.de(codi)
        return ms.sumOf { it.punts } + if (ms.isEmpty()) 0 else config.punts.bonusTotesLesMissions
    }

    fun puntsGuanyatsA(codi: CodiIne, completades: Set<String>): Int {
        val ms = missions.de(codi)
        val fetes = ms.filter { it.id in completades }
        val bonus = if (ms.isNotEmpty() && fetes.size == ms.size) config.punts.bonusTotesLesMissions else 0
        return fetes.sumOf { it.punts } + bonus
    }

    /** Punts que dona completar [missio], tenint en compte les que ja estan [completades]. */
    fun puntsPerCompletar(missio: Missio, completades: Set<String>): PuntsMissio {
        if (missio.id in completades) return PuntsMissio(0, 0)
        // Les oficials van a part: no donen el bonus de completar-les totes (secció 9.6).
        if (missio.tipus == TipusMissio.OFICIAL) return PuntsMissio(missio.punts, 0)
        val ms = missions.de(missio.municipi)
        val totesFetes = ms.all { it.id == missio.id || it.id in completades }
        return PuntsMissio(missio.punts, if (totesFetes) config.punts.bonusTotesLesMissions else 0)
    }

    fun municipiComplet(codi: CodiIne, completades: Set<String>): Boolean {
        val ms = missions.de(codi)
        return ms.isNotEmpty() && ms.all { it.id in completades }
    }
}
