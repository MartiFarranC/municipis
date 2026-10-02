package cat.descobreix.joc.dades

import cat.descobreix.joc.geo.LimitMunicipi
import java.io.ByteArrayInputStream
import java.io.DataInputStream

/**
 * Límits municipals a resolució completa, per saber en quin municipi és l'usuari.
 * Només es llegeix l'índex en obrir el fitxer; cada municipi es descodifica quan cal.
 *
 * Format de `limits.bin` (generat per `scripts/generar-dades.js`):
 * ```
 * "DCL1"  int versió  int n
 * n × (int offset  int minLon  int minLat  int maxLon  int maxLat)     (microgaus)
 * blocs: varint polígons × (varint anells × (varint punts × (zigzag dLon  zigzag dLat)))
 * ```
 * Les coordenades són en milionèsimes de grau, codificades com a diferències respecte del punt anterior.
 */
class IndexLimits(
    val offsets: IntArray,
    /** Per a cada municipi: minLon, minLat, maxLon, maxLat en microgaus. */
    val requadres: Array<IntArray>,
    val midaFitxer: Int,
) {
    val total: Int get() = offsets.size

    fun midaBloc(i: Int): Int = (if (i + 1 < offsets.size) offsets[i + 1] else midaFitxer) - offsets[i]
}

object FormatLimits {
    const val MIDA_CAPCALERA_FIXA = 12
    const val MIDA_ENTRADA_INDEX = 20

    /** Mida en bytes de la capçalera completa per a `n` municipis. */
    fun midaCapcalera(n: Int): Int = MIDA_CAPCALERA_FIXA + n * MIDA_ENTRADA_INDEX

    /** Llegeix el nombre de municipis dels primers 12 bytes. */
    fun llegeixTotal(capcaleraFixa: ByteArray): Int {
        val d = DataInputStream(ByteArrayInputStream(capcaleraFixa))
        val magic = ByteArray(4).also { d.readFully(it) }.decodeToString()
        require(magic == "DCL1") { "limits.bin: format desconegut ($magic)" }
        val versio = d.readInt()
        require(versio == 1) { "limits.bin: versió no suportada ($versio)" }
        return d.readInt()
    }

    fun llegeixIndex(capcalera: ByteArray, midaFitxer: Int): IndexLimits {
        val n = llegeixTotal(capcalera.copyOfRange(0, MIDA_CAPCALERA_FIXA))
        val d = DataInputStream(ByteArrayInputStream(capcalera, MIDA_CAPCALERA_FIXA, n * MIDA_ENTRADA_INDEX))
        val offsets = IntArray(n)
        val requadres = Array(n) { IntArray(4) }
        for (i in 0 until n) {
            offsets[i] = d.readInt()
            for (j in 0 until 4) requadres[i][j] = d.readInt()
        }
        return IndexLimits(offsets, requadres, midaFitxer)
    }

    fun llegeixBloc(bloc: ByteArray): LimitMunicipi {
        val l = LectorVarint(bloc)
        val poligons = List(l.varint()) {
            List(l.varint()) {
                val punts = l.varint()
                val coords = DoubleArray(punts * 2)
                var lon = 0
                var lat = 0
                for (p in 0 until punts) {
                    lon += l.zigzag()
                    lat += l.zigzag()
                    coords[p * 2] = lon / 1e6
                    coords[p * 2 + 1] = lat / 1e6
                }
                coords
            }
        }
        return LimitMunicipi(poligons)
    }

    private class LectorVarint(private val b: ByteArray) {
        private var pos = 0

        fun varint(): Int {
            var resultat = 0L
            var desplacament = 0
            while (true) {
                val byte = b[pos++].toInt() and 0xff
                resultat = resultat or ((byte and 0x7f).toLong() shl desplacament)
                if (byte < 0x80) return resultat.toInt()
                desplacament += 7
            }
        }

        fun zigzag(): Int {
            val v = varint()
            return (v ushr 1) xor -(v and 1)
        }
    }
}
