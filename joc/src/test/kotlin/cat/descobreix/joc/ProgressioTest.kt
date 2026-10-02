package cat.descobreix.joc

import cat.descobreix.joc.progressio.Assoliments
import cat.descobreix.joc.progressio.DadesAssoliments
import cat.descobreix.joc.progressio.Nivells
import cat.descobreix.joc.progressio.TipusAssoliment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProgressioTest {
    private val nivells = Nivells(Repositori.config.nivells)
    private val assoliments = Assoliments(Repositori.config.assoliments, Repositori.geografia)

    @Test
    fun `nivells`() {
        assertEquals(1, nivells.nivell(0).numero)
        assertEquals(1, nivells.nivell(149).numero)
        assertEquals(2, nivells.nivell(150).numero)
        assertEquals(3, nivells.nivell(450).numero)
        val n = nivells.nivell(300)
        assertEquals(150, n.puntsInici)
        assertEquals(450, n.puntsSeguent)
        assertEquals(0.5f, n.progres)
        assertEquals(150, n.faltenPerAlSeguent)
    }

    @Test
    fun `els nivells creixen`() {
        var anterior = -1
        for (p in 0..300_000 step 1000) {
            val n = nivells.nivell(p).numero
            assertTrue(n >= anterior)
            anterior = n
        }
    }

    @Test
    fun `assoliments`() {
        val vic = Repositori.codi("Vic")
        val inicial = assoliments.calcula(DadesAssoliments(setOf(vic), 0, 0, 0))
        assertTrue(inicial.none { it.aconseguit })

        val moianes = Repositori.geografia.comarques.single { it.nom == "Moianès" }
        val tots = Repositori.geografia.municipisDeComarca(moianes.codi).map { it.codi }.toSet()
        val despres = assoliments.calcula(DadesAssoliments(tots, 3, 1, 1)).filter { it.aconseguit }.map { it.tipus }
        assertTrue(TipusAssoliment.PRIMER_MUNICIPI in despres)
        assertTrue(TipusAssoliment.COMARCA_COMPLETA in despres)
        assertTrue(TipusAssoliment.PRIMERA_MISSIO in despres)
        assertTrue(TipusAssoliment.FOTOS in despres)
        assertFalse(TipusAssoliment.CAPITALS in despres)
    }

    @Test
    fun `progrés per comarques`() {
        val p = assoliments.progresComarques(emptySet())
        assertEquals(43, p.size)
        assertEquals(947, p.sumOf { it.total })
    }

    @Test
    fun `capitals de comarca`() {
        val capitals = Repositori.geografia.capitals
        // 43 comarques, i el Vallès Occidental en té dues (Sabadell i Terrassa).
        assertEquals(44, capitals.size)
        assertTrue(Repositori.codi("Vielha e Mijaran") in capitals)
    }
}
