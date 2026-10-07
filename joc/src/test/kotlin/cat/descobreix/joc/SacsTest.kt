package cat.descobreix.joc

import cat.descobreix.joc.config.ConfiguracioJoc.Objecte
import cat.descobreix.joc.config.ConfiguracioJoc.Raresa
import cat.descobreix.joc.config.ConfiguracioJoc.TipusObjecte
import cat.descobreix.joc.progressio.ContingutSac
import cat.descobreix.joc.progressio.DadesSacs
import cat.descobreix.joc.progressio.MedallaGuanyada
import cat.descobreix.joc.progressio.Medalles
import cat.descobreix.joc.progressio.Sacs
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SacsTest {
    private val config = Repositori.config.sacs
    private val sacs = Sacs(config)

    @Test
    fun `els sacs tenen el contingut decidit`() {
        fun compta(t: TipusObjecte) = sacs.delsSacs.count { it.tipus == t }
        assertEquals(100, compta(TipusObjecte.EMOJI))
        assertEquals(10, compta(TipusObjecte.PORTADA))
        assertEquals(10, compta(TipusObjecte.ANIMACIO))
        assertEquals(9, compta(TipusObjecte.COLOR))
        assertEquals(mapOf(Raresa.COMUNA to 70, Raresa.RARA to 25, Raresa.LLEGENDARIA to 5), config.probabilitats)
        assertEquals(100, config.probabilitats.values.sum())
        assertTrue(sacs.inicials.isNotEmpty())
    }

    @Test
    fun `cap objecte no surt dues vegades`() {
        assertEquals(config.objectes.size, config.objectes.map { it.tipus to it.id }.toSet().size)
    }

    @Test
    fun `hi ha tants sacs de medalles de comarca com coses als sacs`() {
        val medalles = Medalles(Repositori.config.medalles, Repositori.geografia, Repositori.missions)
        val tots = Repositori.geografia.municipis.map { it.codi }.toSet()
        val completades = Repositori.geografia.municipis.flatMap { Repositori.missions.de(it.codi) }.map { it.id }.toSet()
        val guanyades = medalles.guanyades(medalles.calcula(medalles.dades(tots, completades)))
        val deMedalles = sacs.guanyats(DadesSacs(0, 0, 0, guanyades))
        assertEquals(43 * 3, deMedalles.size)
        assertEquals(sacs.delsSacs.size, deMedalles.size)
    }

    @Test
    fun `els sacs de les primeres vegades`() {
        assertEquals(emptyList(), sacs.guanyats(DadesSacs(1, 0, 0, emptyList())))
        assertEquals(listOf(Sacs.PRIMER_MUNICIPI), sacs.guanyats(DadesSacs(2, 0, 0, emptyList())))
        assertEquals(
            listOf(Sacs.PRIMER_MUNICIPI, Sacs.PRIMERA_MISSIO, Sacs.PRIMERA_FOTO),
            sacs.guanyats(DadesSacs(5, 3, 1, emptyList())),
        )
        val medalles = listOf(MedallaGuanyada("comarca_01_bronze", 25), MedallaGuanyada("municipis_10", 20))
        assertEquals(listOf("medalla_comarca_01_bronze"), sacs.guanyats(DadesSacs(1, 0, 0, medalles)))
    }

    @Test
    fun `obrint tots els sacs surt tot una sola vegada i després punts`() {
        val atzar = Random(7)
        val tinc = mutableListOf<Objecte>()
        repeat(sacs.delsSacs.size) {
            val c = sacs.obre(tinc, atzar)
            assertIs<ContingutSac.Nou>(c)
            assertTrue(c.objecte !in tinc)
            assertTrue(c.objecte.raresa != Raresa.INICIAL)
            tinc += c.objecte
        }
        assertEquals(sacs.delsSacs.toSet(), tinc.toSet())
        assertEquals(ContingutSac.Punts(config.puntsSiJaTensTot), sacs.obre(tinc, atzar))
    }

    @Test
    fun `les rareses surten amb les probabilitats que toquen`() {
        val atzar = Random(42)
        val n = 20_000
        val compte = (1..n).map { (sacs.obre(emptyList(), atzar) as ContingutSac.Nou).objecte.raresa }.groupingBy { it }.eachCount()
        for ((r, p) in config.probabilitats) {
            val fraccio = (compte[r] ?: 0).toDouble() / n
            assertTrue(kotlin.math.abs(fraccio - p / 100.0) < 0.015, "$r: $fraccio")
        }
    }

    @Test
    fun `si s'acaba una raresa surt de les altres`() {
        val senseComunes = sacs.delsSacs.filter { it.raresa == Raresa.COMUNA }
        val atzar = Random(1)
        repeat(200) {
            val c = sacs.obre(senseComunes, atzar) as ContingutSac.Nou
            assertTrue(c.objecte.raresa != Raresa.COMUNA)
        }
    }
}
