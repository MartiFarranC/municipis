package cat.descobreix

import cat.descobreix.data.sincronitzacio.Fusio
import kotlin.test.Test
import kotlin.test.assertEquals

class FusioTest {
    private data class Descobert(val id: String, val codi: String)

    private data class Sac(val id: String, val origen: String, val obert: String?, val modificatEl: Long)

    private fun afegibles(locals: List<Descobert>, remotes: List<Descobert>) = Fusio.afegibles(locals, remotes, { it.id }, { it.codi })

    private fun sacs(locals: List<Sac>, remotes: List<Sac>) = Fusio.modificables(
        locals, remotes, { it.id }, { it.origen }, { it.modificatEl },
        ajunta = { l, r -> if (r.obert == null && l.obert != null) r.copy(obert = l.obert, modificatEl = l.modificatEl) else r },
    )

    @Test
    fun `s'afegeix el que el mòbil no té`() {
        val r = afegibles(listOf(Descobert("a", "08298")), listOf(Descobert("a", "08298"), Descobert("b", "08019")))
        assertEquals(listOf(Descobert("b", "08019")), r.desa)
        assertEquals(emptyList(), r.treuLocals)
    }

    @Test
    fun `si dos mòbils han descobert el mateix municipi, val el del servidor`() {
        val r = afegibles(listOf(Descobert("local", "08298")), listOf(Descobert("remot", "08298")))
        assertEquals(listOf(Descobert("remot", "08298")), r.desa)
        assertEquals(listOf("local"), r.treuLocals)
    }

    @Test
    fun `guanya la modificació més recent`() {
        val local = Sac("s", "primera_foto", null, 5)
        assertEquals(emptyList(), sacs(listOf(local), listOf(local.copy(modificatEl = 3))).desa)
        val remot = local.copy(obert = "emoji", modificatEl = 9)
        assertEquals(listOf(remot), sacs(listOf(local), listOf(remot)).desa)
    }

    @Test
    fun `el mateix sac guanyat a dos mòbils es queda l'id del servidor i el que s'hi ha obert`() {
        val local = Sac("l", "primera_foto", "color:lila", 8)
        val remot = Sac("r", "primera_foto", null, 4)
        val r = sacs(listOf(local), listOf(remot))
        assertEquals(listOf(Sac("r", "primera_foto", "color:lila", 8)), r.desa)
        assertEquals(listOf("l"), r.treuLocals)
        assertEquals(listOf("r"), r.tornaAPujar)
    }

    @Test
    fun `si el del servidor ja estava obert, no cal tornar a pujar res`() {
        val r = sacs(listOf(Sac("l", "primera_foto", null, 2)), listOf(Sac("r", "primera_foto", "emoji:x", 3)))
        assertEquals(listOf(Sac("r", "primera_foto", "emoji:x", 3)), r.desa)
        assertEquals(emptyList(), r.tornaAPujar)
    }
}
