package cat.descobreix

import cat.descobreix.domain.Joc
import cat.descobreix.domain.MedallesNoves
import cat.descobreix.joc.model.TipusMissio
import cat.descobreix.joc.progressio.Medalles
import cat.descobreix.joc.progressio.TipusMedalla
import cat.descobreix.joc.regles.ResultatDesbloqueig
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class JocTest {
    private val repositori = ProgresEnMemoria()
    private val joc = Joc(DadesDeProva, repositori)
    private val vic = DadesDeProva.codi("Vic")
    private val gurb = DadesDeProva.codi("Gurb")

    @Test
    fun `una partida sencera en un municipi`() = runTest {
        joc.iniciaPartida(vic)
        assertEquals(vic, repositori.progresAra().inici)
        assertEquals(ResultatDesbloqueig.PuntsInsuficients(60, 60), joc.desbloqueja(gurb))

        // Les missions genèriques sempre donen els punts garantits.
        val generiques = DadesDeProva.dades.missions.de(vic).filter { it.tipus == TipusMissio.GENERICA }
        val punts = generiques.map { joc.completaMissio(it, null, null) }
        assertEquals(listOf(100, 50, 50), punts.map { it.missio })
        // La foto del cartell també dona la medalla del primer cartell.
        val medalla = DadesDeProva.dades.config.medalles.punts.fitaCartells
        assertEquals(200 + medalla, repositori.progresAra().saldo)

        // Tornar a completar una missió no dona punts.
        assertEquals(0, joc.completaMissio(generiques.first(), null, null).total)

        assertEquals(ResultatDesbloqueig.Permes(60), joc.desbloqueja(gurb))
        val p = repositori.progresAra()
        assertEquals(listOf(vic, gurb), p.descoberts)
        assertEquals(140 + medalla, p.saldo)
        assertEquals(200 + medalla, p.puntsGuanyats)
    }

    @Test
    fun `el bonus arriba amb l'última missió del municipi`() = runTest {
        joc.iniciaPartida(vic)
        val missions = DadesDeProva.dades.missions.de(vic)
        val punts = missions.map { joc.completaMissio(it, null, null) }
        assertEquals(0, punts.dropLast(1).sumOf { it.bonus })
        assertEquals(DadesDeProva.dades.config.punts.bonusTotesLesMissions, punts.last().bonus)
        val medalla = DadesDeProva.dades.config.medalles.punts.fitaCartells
        assertEquals(DadesDeProva.dades.regles.puntsPossibles(vic) + medalla, repositori.progresAra().saldo)
    }

    @Test
    fun `una medalla només dona punts una vegada i s'anuncia`() = runTest {
        joc.iniciaPartida(vic)
        val anunciades = mutableListOf<MedallesNoves>()
        val escolta = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { joc.medallesNoves.toList(anunciades) }
        val cartell = DadesDeProva.dades.missions.de(vic).single { it.clau == Medalles.CLAU_CARTELL }
        joc.completaMissio(cartell, null, null)
        val p = DadesDeProva.dades.config.medalles.punts
        assertEquals(cartell.punts + p.fitaCartells, repositori.progresAra().saldo)
        assertEquals(1, anunciades.size)
        assertEquals(TipusMedalla.CARTELLS, anunciades.single().medalles.single().tipus)
        assertEquals(p.fitaCartells, anunciades.single().punts)

        // Una altra missió no torna a donar la medalla.
        val checkin = DadesDeProva.dades.missions.de(vic).first { it.clau != Medalles.CLAU_CARTELL }
        joc.completaMissio(checkin, null, null)
        assertEquals(cartell.punts + checkin.punts + p.fitaCartells, repositori.progresAra().saldo)
        assertEquals(1, anunciades.size)
        escolta.cancel()
    }

    @Test(expected = IllegalStateException::class)
    fun `no es poden fer missions d'un municipi bloquejat`() = runTest {
        joc.iniciaPartida(vic)
        joc.completaMissio(DadesDeProva.dades.missions.de(gurb).first(), null, null)
    }
}
