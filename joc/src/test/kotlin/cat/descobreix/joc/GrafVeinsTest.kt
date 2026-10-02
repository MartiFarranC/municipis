package cat.descobreix.joc

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GrafVeinsTest {
    private val graf = Repositori.graf
    private val geografia = Repositori.geografia

    @Test
    fun `hi ha 947 municipis i 43 comarques`() {
        assertEquals(947, geografia.total)
        assertEquals(43, geografia.comarques.size)
        assertTrue(geografia.comarques.any { it.nom == "Lluçanès" })
    }

    @Test
    fun `Llívia i Puigcerdà estan connectades`() {
        val llivia = Repositori.codi("Llívia")
        val puigcerda = Repositori.codi("Puigcerdà")
        assertEquals(setOf(puigcerda), graf.veins(llivia))
        assertTrue(llivia in graf.veins(puigcerda))
    }

    @Test
    fun `des de qualsevol municipi es pot arribar a tots els altres`() {
        for (m in geografia.municipis) {
            assertEquals(geografia.total, graf.distancies(setOf(m.codi)).size, "Des de ${m.nom}")
        }
    }

    @Test
    fun `la relació de veïnatge és simètrica`() {
        for (m in geografia.municipis) for (v in m.veins) {
            assertTrue(m.codi in graf.veins(v), "${m.nom} -> $v")
        }
    }

    @Test
    fun `distàncies i disponibles`() {
        val vic = Repositori.codi("Vic")
        val dist = graf.distancies(setOf(vic))
        assertEquals(0, dist[vic])
        for (v in graf.veins(vic)) assertEquals(1, dist[v])
        assertEquals(graf.veins(vic), graf.disponibles(setOf(vic)))
    }
}
