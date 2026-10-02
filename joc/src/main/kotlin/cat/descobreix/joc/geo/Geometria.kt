package cat.descobreix.joc.geo

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

/** Funcions de geometria plana. Els anells són arrays [x0, y0, x1, y1, ...] sense repetir el primer punt. */
object Geometria {
    fun dinsAnell(x: Double, y: Double, anell: IntArray): Boolean {
        var dins = false
        val n = anell.size / 2
        var j = n - 1
        for (i in 0 until n) {
            val xi = anell[i * 2].toDouble()
            val yi = anell[i * 2 + 1].toDouble()
            val xj = anell[j * 2].toDouble()
            val yj = anell[j * 2 + 1].toDouble()
            if ((yi > y) != (yj > y) && x < (xj - xi) * (y - yi) / (yj - yi) + xi) dins = !dins
            j = i
        }
        return dins
    }

    fun dinsAnell(x: Double, y: Double, anell: DoubleArray): Boolean {
        var dins = false
        val n = anell.size / 2
        var j = n - 1
        for (i in 0 until n) {
            val xi = anell[i * 2]
            val yi = anell[i * 2 + 1]
            val xj = anell[j * 2]
            val yj = anell[j * 2 + 1]
            if ((yi > y) != (yj > y) && x < (xj - xi) * (y - yi) / (yj - yi) + xi) dins = !dins
            j = i
        }
        return dins
    }

    /** Dins d'un polígon: dins de l'anell exterior (el primer) i fora dels forats. */
    fun dinsPoligon(x: Double, y: Double, anells: List<IntArray>): Boolean {
        if (anells.isEmpty() || !dinsAnell(x, y, anells[0])) return false
        for (r in 1 until anells.size) if (dinsAnell(x, y, anells[r])) return false
        return true
    }

    fun dinsPoligonD(x: Double, y: Double, anells: List<DoubleArray>): Boolean {
        if (anells.isEmpty() || !dinsAnell(x, y, anells[0])) return false
        for (r in 1 until anells.size) if (dinsAnell(x, y, anells[r])) return false
        return true
    }

    fun distanciaASegment(x: Double, y: Double, ax: Double, ay: Double, bx: Double, by: Double): Double {
        val dx = bx - ax
        val dy = by - ay
        val l = dx * dx + dy * dy
        val t = if (l == 0.0) 0.0 else (((x - ax) * dx + (y - ay) * dy) / l).coerceIn(0.0, 1.0)
        return hypot(x - (ax + t * dx), y - (ay + t * dy))
    }

    /** Distància en metres entre dos punts (fórmula de l'haversinus). */
    fun distanciaMetres(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val h = sin(dLat / 2).let { it * it } +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).let { it * it }
        return 2 * RADI_TERRA * asin(sqrt(h))
    }

    const val RADI_TERRA = 6_371_000.0
}
