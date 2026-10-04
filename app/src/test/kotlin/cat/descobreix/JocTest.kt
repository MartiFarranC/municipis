package cat.descobreix

import cat.descobreix.domain.Joc
import cat.descobreix.joc.model.TipusMissio
import cat.descobreix.joc.regles.ResultatDesbloqueig
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
        assertEquals(200, repositori.progresAra().saldo)

        // Tornar a completar una missió no dona punts.
        assertEquals(0, joc.completaMissio(generiques.first(), null, null).total)

        assertEquals(ResultatDesbloqueig.Permes(60), joc.desbloqueja(gurb))
        val p = repositori.progresAra()
        assertEquals(listOf(vic, gurb), p.descoberts)
        assertEquals(140, p.saldo)
        assertEquals(200, p.puntsGuanyats)
    }

    @Test
    fun `el bonus arriba amb l'última missió del municipi`() = runTest {
        joc.iniciaPartida(vic)
        val missions = DadesDeProva.dades.missions.de(vic)
        val punts = missions.map { joc.completaMissio(it, null, null) }
        assertEquals(0, punts.dropLast(1).sumOf { it.bonus })
        assertEquals(DadesDeProva.dades.config.punts.bonusTotesLesMissions, punts.last().bonus)
        assertEquals(DadesDeProva.dades.regles.puntsPossibles(vic), repositori.progresAra().saldo)
    }

    @Test(expected = IllegalStateException::class)
    fun `no es poden fer missions d'un municipi bloquejat`() = runTest {
        joc.iniciaPartida(vic)
        joc.completaMissio(DadesDeProva.dades.missions.de(gurb).first(), null, null)
    }
}
