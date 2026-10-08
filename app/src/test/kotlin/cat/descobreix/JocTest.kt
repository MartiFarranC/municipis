package cat.descobreix

import cat.descobreix.domain.Joc
import cat.descobreix.domain.MedallesNoves
import cat.descobreix.joc.model.TipusMissio
import cat.descobreix.joc.model.TipusProva
import cat.descobreix.joc.progressio.ContingutSac
import cat.descobreix.joc.progressio.Medalles
import cat.descobreix.joc.progressio.Sacs
import cat.descobreix.joc.progressio.TipusMedalla
import cat.descobreix.joc.regles.MissioAjuntament
import cat.descobreix.joc.regles.ResultatDesbloqueig
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class JocTest {
    private val repositori = ProgresEnMemoria()
    private val fotos = FotosEnMemoria()
    private val sacs = SacsEnMemoria(repositori)
    private val joc = Joc(DadesDeProva, repositori, sacs, fotos)
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

    @Test
    fun `la primera missió dona un sac, que s'obre una sola vegada`() = runTest {
        joc.iniciaPartida(vic)
        val anunciats = mutableListOf<List<String>>()
        val escolta = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { joc.sacsNous.toList(anunciats) }
        val inicials = joc.colleccioAra()
        assertEquals(DadesDeProva.dades.sacs.inicials, inicials)

        joc.completaMissio(DadesDeProva.dades.missions.de(vic).first { it.clau != Medalles.CLAU_CARTELL }, null, null)
        assertEquals(listOf(listOf(Sacs.PRIMERA_MISSIO)), anunciats)

        val contingut = joc.obreSac(Sacs.PRIMERA_MISSIO)
        assertIs<ContingutSac.Nou>(contingut)
        assertEquals(inicials + contingut.objecte, joc.colleccioAra())
        assertFailsWith<IllegalStateException> { joc.obreSac(Sacs.PRIMERA_MISSIO) }

        // Tornar a revisar els sacs no en dona cap de repetit.
        joc.revisaSacs()
        assertEquals(1, anunciats.size)
        escolta.cancel()
    }

    @Test(expected = IllegalStateException::class)
    fun `no es poden fer missions d'un municipi bloquejat`() = runTest {
        joc.iniciaPartida(vic)
        joc.completaMissio(DadesDeProva.dades.missions.de(gurb).first(), null, null)
    }

    @Test
    fun `una missio oficial dona les barretines de la configuracio, sense el bonus`() = runTest {
        val c = DadesDeProva.dades.config.ajuntaments
        val m = MissioAjuntament("u1", vic, "Puja al campanar", null, TipusProva.FOTO, null, null, null, null, null, 0, false)
        val j = Joc(DadesDeProva, repositori, sacs, fotos, AjuntamentsEnMemoria(listOf(m)))
        j.iniciaPartida(vic)
        val oficial = checkNotNull(j.missio(m.idMissio))
        assertEquals(TipusMissio.OFICIAL, oficial.tipus)
        // Encara que ja s'hagin fet totes les automàtiques, no torna a donar el bonus.
        DadesDeProva.dades.missions.de(vic).forEach { j.completaMissio(it, null, null) }
        val punts = j.completaMissio(oficial, null, null)
        assertEquals(c.puntsMissio, punts.missio)
        assertEquals(0, punts.bonus)
    }

    @Test
    fun `una festa que no es avui no dona barretines`() = runTest {
        val dema = Joc.avui().plusDays(1)
        val m = MissioAjuntament("u2", vic, "Fira", null, TipusProva.FOTO, null, null, null, dema, dema, 0, false)
        val j = Joc(DadesDeProva, repositori, sacs, fotos, AjuntamentsEnMemoria(listOf(m)))
        j.iniciaPartida(vic)
        assertEquals(0, j.completaMissio(checkNotNull(j.missio(m.idMissio)), null, null).total)
    }
}
