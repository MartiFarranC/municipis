package cat.descobreix.joc.progressio

import cat.descobreix.joc.config.ConfiguracioJoc

data class Nivell(
    val numero: Int,
    /** Punts totals a partir dels quals s'és en aquest nivell. */
    val puntsInici: Int,
    /** Punts totals que calen per al nivell següent. */
    val puntsSeguent: Int,
    val puntsActuals: Int,
) {
    val faltenPerAlSeguent: Int get() = puntsSeguent - puntsActuals

    /** Progrés dins del nivell, de 0 a 1. */
    val progres: Float get() = (puntsActuals - puntsInici).toFloat() / (puntsSeguent - puntsInici)
}

/**
 * Nivells a partir dels punts guanyats en total (els gastats també compten).
 * Per passar del nivell n al n+1 calen `puntsPrimerNivell + increment × (n − 1)` punts.
 */
class Nivells(private val config: ConfiguracioJoc.Nivells) {
    fun llindar(numero: Int): Int {
        val n = (numero - 1).toLong()
        return (config.puntsPrimerNivell * n + config.increment * n * (n - 1) / 2).toInt()
    }

    fun nivell(puntsTotals: Int): Nivell {
        val punts = maxOf(puntsTotals, 0)
        var numero = 1
        while (llindar(numero + 1) <= punts) numero++
        return Nivell(numero, llindar(numero), llindar(numero + 1), punts)
    }
}
