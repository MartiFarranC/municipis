package cat.descobreix.joc

import cat.descobreix.joc.geo.Localitzacio
import cat.descobreix.joc.model.FontMissio
import cat.descobreix.joc.model.Missio
import cat.descobreix.joc.model.TipusMissio
import cat.descobreix.joc.model.TipusProva
import cat.descobreix.joc.regles.ResultatProva
import cat.descobreix.joc.regles.Ubicacio
import cat.descobreix.joc.regles.ValidadorProves
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ValidadorProvesTest {
    private val v = ValidadorProves(Repositori.config)
    private val vic = "08298"
    private val gurb = "08100"
    private val calldetenes = "08037"

    private val lloc = Missio(
        "prova", vic, "Lloc de prova", TipusMissio.LLOC, null, "monument",
        lat = 41.93, lon = 2.25, prova = TipusProva.GPS, punts = 20, font = FontMissio("osm", "node/1"),
    )
    private val checkin = lloc.copy(id = "checkin", tipus = TipusMissio.GENERICA, lat = null, lon = null, clau = "checkin")

    @Test
    fun `GPS a menys de 75 metres i amb bona precisió`() {
        val r = v.validaGps(lloc, Ubicacio(41.9303, 2.25, 10.0), Localitzacio.Dins(vic), setOf(vic))
        assertEquals(ResultatProva.Valida, r)
    }

    @Test
    fun `GPS massa lluny`() {
        val r = v.validaGps(lloc, Ubicacio(41.932, 2.25, 10.0), Localitzacio.Dins(vic), setOf(vic))
        assertIs<ResultatProva.MassaLluny>(r)
    }

    @Test
    fun `GPS amb precisió pitjor de 50 metres`() {
        val r = v.validaGps(lloc, Ubicacio(41.93, 2.25, 60.0), Localitzacio.Dins(vic), setOf(vic))
        assertIs<ResultatProva.PrecisioInsuficient>(r)
    }

    @Test
    fun `check-in dins del municipi`() {
        assertEquals(ResultatProva.Valida, v.validaGps(checkin, Ubicacio(41.9, 2.2, 10.0), Localitzacio.Dins(vic), setOf(vic)))
    }

    @Test
    fun `a dins d'un municipi bloquejat no es pot fer res`() {
        val r = v.validaGps(checkin, Ubicacio(41.9, 2.2, 10.0), Localitzacio.Dins(gurb), setOf(vic))
        assertEquals(ResultatProva.MunicipiBloquejat(gurb), r)
        assertEquals(ResultatProva.MunicipiBloquejat(gurb), v.validaFoto(vic, Localitzacio.Dins(gurb), setOf(vic)))
        assertEquals(ResultatProva.MunicipiBloquejat(gurb), v.validaFoto(gurb, Localitzacio.Dins(gurb), setOf(vic)))
    }

    @Test
    fun `en un altre municipi descobert`() {
        assertEquals(ResultatProva.AltreMunicipi(gurb), v.validaFoto(vic, Localitzacio.Dins(gurb), setOf(vic, gurb)))
    }

    @Test
    fun `a la frontera cal triar`() {
        val dubte = Localitzacio.Dubte(listOf(vic, calldetenes))
        assertEquals(ResultatProva.CalTriarMunicipi(listOf(vic, calldetenes)), v.validaFoto(vic, dubte, setOf(vic)))
        assertEquals(ResultatProva.Valida, v.validaFoto(vic, dubte, setOf(vic), municipiTriat = vic))
        assertEquals(ResultatProva.MunicipiBloquejat(calldetenes), v.validaFoto(vic, dubte, setOf(vic), municipiTriat = calldetenes))
    }

    @Test
    fun `fora de Catalunya`() {
        assertEquals(ResultatProva.ForaDeCatalunya, v.validaFoto(vic, Localitzacio.Fora, setOf(vic)))
    }
}
