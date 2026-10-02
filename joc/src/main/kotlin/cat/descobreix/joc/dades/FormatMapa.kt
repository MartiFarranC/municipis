package cat.descobreix.joc.dades

import cat.descobreix.joc.geo.Projeccio
import java.io.DataInputStream
import java.io.InputStream

/**
 * Geometria per dibuixar el mapa: projectada (Mercator) i simplificada, en dos nivells de detall.
 *
 * Format de `mapa.bin` (big-endian, generat per `scripts/generar-dades.js`):
 * ```
 * "DCM1"  int versió  int n  double k  double tx  double ty  int amplada  int alçada
 * n × (int etiquetaX  int etiquetaY  int minX  int minY  int maxX  int maxY)
 * 2 nivells (general, detall) × n × geometria
 * geometria = int polígons × (int anells × (int punts × (int x  int y)))
 * ```
 */
class GeometriaMapa(
    val projeccio: Projeccio,
    val amplada: Int,
    val alcada: Int,
    val municipis: List<FormaMunicipi>,
)

class FormaMunicipi(
    val etiquetaX: Int,
    val etiquetaY: Int,
    val requadre: Requadre,
    /** Geometria simplificada per veure tota Catalunya. */
    val general: List<Poligon>,
    /** Geometria amb més detall per quan es fa zoom. */
    val detall: List<Poligon>,
)

data class Requadre(val minX: Int, val minY: Int, val maxX: Int, val maxY: Int) {
    fun conte(x: Double, y: Double): Boolean = x >= minX && x <= maxX && y >= minY && y <= maxY

    fun talla(x0: Double, y0: Double, x1: Double, y1: Double): Boolean =
        maxX >= x0 && minX <= x1 && maxY >= y0 && minY <= y1
}

/** Un polígon: el primer anell és l'exterior i la resta són forats. Cada anell és [x0, y0, x1, y1, ...]. */
class Poligon(val anells: List<IntArray>)

object FormatMapa {
    private const val NIVELLS = 2

    fun llegeix(entrada: InputStream): GeometriaMapa {
        val d = DataInputStream(entrada.buffered())
        val magic = ByteArray(4).also { d.readFully(it) }.decodeToString()
        require(magic == "DCM1") { "mapa.bin: format desconegut ($magic)" }
        val versio = d.readInt()
        require(versio == 1) { "mapa.bin: versió no suportada ($versio)" }
        val n = d.readInt()
        val projeccio = Projeccio(k = d.readDouble(), tx = d.readDouble(), ty = d.readDouble())
        val amplada = d.readInt()
        val alcada = d.readInt()
        val capcaleres = List(n) {
            val ex = d.readInt()
            val ey = d.readInt()
            Triple(ex, ey, Requadre(d.readInt(), d.readInt(), d.readInt(), d.readInt()))
        }
        val nivells = List(NIVELLS) { List(n) { llegeixGeometria(d) } }
        val municipis = capcaleres.mapIndexed { i, (ex, ey, r) ->
            FormaMunicipi(ex, ey, r, general = nivells[0][i], detall = nivells[1][i])
        }
        return GeometriaMapa(projeccio, amplada, alcada, municipis)
    }

    private fun llegeixGeometria(d: DataInputStream): List<Poligon> = List(d.readInt()) {
        Poligon(
            List(d.readInt()) {
                val punts = d.readInt()
                IntArray(punts * 2) { d.readInt() }
            },
        )
    }
}
