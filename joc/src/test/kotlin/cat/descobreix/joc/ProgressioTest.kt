package cat.descobreix.joc

import cat.descobreix.joc.progressio.DadesMedalles
import cat.descobreix.joc.progressio.Medalles
import cat.descobreix.joc.progressio.NivellMedalla
import cat.descobreix.joc.progressio.Nivells
import cat.descobreix.joc.progressio.Passaport
import cat.descobreix.joc.progressio.TipusMedalla
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProgressioTest {
    private val nivells = Nivells(Repositori.config.nivells)
    private val medalles = Medalles(Repositori.config.medalles, Repositori.geografia, Repositori.missions)
    private val moianes = Repositori.geografia.comarques.single { it.nom == "Moianès" }
    private val municipisMoianes = Repositori.geografia.municipisDeComarca(moianes.codi).map { it.codi }.toSet()

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
    fun `la vitrina té totes les medalles`() {
        val m = medalles.calcula(DadesMedalles(emptySet(), emptySet(), emptySet()))
        assertEquals(43, m.count { it.tipus == TipusMedalla.COMARCA })
        assertEquals(6, m.count { it.tipus == TipusMedalla.MUNICIPIS })
        assertEquals(1, m.count { it.tipus == TipusMedalla.CAPITALS })
        assertEquals(5, m.count { it.tipus == TipusMedalla.CARTELLS })
        assertTrue(m.none { it.aconseguida })
        assertTrue(medalles.guanyades(m).isEmpty())
    }

    @Test
    fun `els tres nivells d'una comarca`() {
        fun nivell(d: DadesMedalles) = medalles.calcula(d).single { it.comarca == moianes.codi }
        val tots = municipisMoianes
        val unMenys = tots - tots.first()

        val res = nivell(DadesMedalles(unMenys, emptySet(), emptySet()))
        assertNull(res.nivell)
        assertEquals(tots.size - 1, res.actual)
        assertEquals(tots.size, res.necessari)

        assertEquals(NivellMedalla.BRONZE, nivell(DadesMedalles(tots, unMenys, tots)).nivell)
        assertEquals(NivellMedalla.PLATA, nivell(DadesMedalles(tots, tots, unMenys)).nivell)
        assertEquals(NivellMedalla.OR, nivell(DadesMedalles(tots, tots, tots)).nivell)
        // L'or demana la plata: amb tots els cartells però sense totes les missions es queda en bronze.
        assertEquals(NivellMedalla.BRONZE, nivell(DadesMedalles(tots, unMenys, tots)).nivell)
    }

    @Test
    fun `punts de les medalles guanyades`() {
        val p = Repositori.config.medalles.punts
        val or = medalles.calcula(DadesMedalles(municipisMoianes, municipisMoianes, municipisMoianes))
        val guanyades = medalles.guanyades(or)
        val deComarca = guanyades.filter { it.id.startsWith("comarca_${moianes.codi}_") }
        assertEquals(listOf("bronze", "plata", "or"), deComarca.map { it.id.substringAfterLast('_') })
        assertEquals(p.comarcaBronze + p.comarcaPlata + p.comarcaOr, deComarca.sumOf { it.punts })
        // El Moianès té 10 municipis: fita de 10 municipis i fites d'1 cartell.
        assertTrue(guanyades.any { it.id == "municipis_10" && it.punts == p.fitaMunicipis })
        assertTrue(guanyades.any { it.id == "cartells_1" && it.punts == p.fitaCartells })
        assertTrue(guanyades.none { it.id == "capitals" })
        assertEquals(guanyades.size, guanyades.map { it.id }.distinct().size)
    }

    @Test
    fun `dades a partir de les missions completades`() {
        val codi = municipisMoianes.first()
        val missions = Repositori.missions.de(codi)
        val cartell = missions.single { it.clau == Medalles.CLAU_CARTELL }
        val nomesCartell = medalles.dades(setOf(codi), setOf(cartell.id))
        assertEquals(setOf(codi), nomesCartell.cartells)
        assertTrue(nomesCartell.complets.isEmpty())
        val totes = medalles.dades(setOf(codi), missions.map { it.id }.toSet())
        assertEquals(setOf(codi), totes.complets)
    }

    @Test
    fun `progrés per comarques`() {
        val p = medalles.progresComarques(emptySet())
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

    @Test
    fun `pàgines del passaport de 20 segells`() {
        val p = Passaport(Repositori.config.passaport)
        assertEquals(0, p.paginaNova(0))
        assertEquals(0, p.paginaNova(19))
        assertEquals(1, p.paginaNova(20))
        assertEquals(1, p.pagines(0))
        assertEquals(1, p.pagines(20))
        // L'Alt Empordà, amb 68 municipis, té 4 pàgines.
        val altEmporda = Repositori.geografia.comarques.single { it.nom == "Alt Empordà" }
        assertEquals(4, p.pagines(Repositori.geografia.municipisDeComarca(altEmporda.codi).size))
    }

    @Test
    fun `el segell sempre cap sencer dins la pàgina`() {
        val p = Passaport(Repositori.config.passaport)
        val (amplada, alcada) = .34f to .15f
        for ((x, y) in listOf(0f to 0f, 1f to 1f, .5f to .5f, -3f to 7f)) {
            val (lx, ly) = p.limita(x, y, amplada, alcada)
            assertTrue(lx - amplada / 2 >= 0f && lx + amplada / 2 <= 1f)
            assertTrue(ly - alcada / 2 >= Passaport.CAPCALERA && ly + alcada / 2 <= 1f)
        }
        assertEquals(.5f to .5f, p.limita(.5f, .5f, amplada, alcada))
    }

    @Test
    fun `cada segell té un gir petit i una tinta vàlida`() {
        val p = Passaport(Repositori.config.passaport)
        val atzar = Random(1)
        repeat(200) {
            val (gir, tinta) = p.estil(atzar)
            assertTrue(gir in -Passaport.GIR_MAXIM..Passaport.GIR_MAXIM)
            assertTrue(tinta in 0 until Passaport.TINTES)
        }
    }
}
