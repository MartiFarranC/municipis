package cat.descobreix.joc.geo

import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.tan

/** Projecció de Mercator amb els mateixos paràmetres que d3.geoMercator() de l'script de dades. */
data class Projeccio(val k: Double, val tx: Double, val ty: Double) {
    fun x(lon: Double): Double = tx + k * Math.toRadians(lon)

    fun y(lat: Double): Double = ty - k * ln(tan(PI / 4 + Math.toRadians(lat) / 2))

    fun lon(x: Double): Double = Math.toDegrees((x - tx) / k)

    fun lat(y: Double): Double = Math.toDegrees(2 * atan(exp((ty - y) / k)) - PI / 2)
}
