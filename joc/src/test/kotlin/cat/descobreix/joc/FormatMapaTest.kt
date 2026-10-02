package cat.descobreix.joc

import cat.descobreix.joc.geo.Geometria
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FormatMapaTest {
    private val mapa = Repositori.mapa

    @Test
    fun `té tots els municipis amb geometria`() {
        assertEquals(Repositori.geografia.total, mapa.municipis.size)
        for ((i, m) in mapa.municipis.withIndex()) {
            assertTrue(m.general.isNotEmpty() && m.detall.isNotEmpty(), Repositori.geografia.municipis[i].nom)
            assertTrue(m.requadre.maxX <= mapa.amplada && m.requadre.maxY <= mapa.alcada)
        }
    }

    @Test
    fun `l'etiqueta és dins del municipi`() {
        for ((i, m) in mapa.municipis.withIndex()) {
            val dins = m.detall.any { Geometria.dinsPoligon(m.etiquetaX.toDouble(), m.etiquetaY.toDouble(), it.anells) }
            assertTrue(dins, Repositori.geografia.municipis[i].nom)
        }
    }

    @Test
    fun `la projecció situa Barcelona dins de Barcelona`() {
        val p = mapa.projeccio
        val x = p.x(2.1700)
        val y = p.y(41.3870)
        val bcn = mapa.municipis[Repositori.geografia.index(Repositori.codi("Barcelona"))]
        assertTrue(bcn.detall.any { Geometria.dinsPoligon(x, y, it.anells) })
        assertTrue(abs(p.lon(x) - 2.1700) < 1e-9 && abs(p.lat(y) - 41.3870) < 1e-9)
    }
}
