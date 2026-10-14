package cat.descobreix.joc

import cat.descobreix.joc.model.EstatMunicipi
import cat.descobreix.joc.regles.EstatJoc
import cat.descobreix.joc.regles.ResultatDesbloqueig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ReglesJocTest {
    private val regles = Repositori.regles
    private val vic = Repositori.codi("Vic")
    private val gurb = Repositori.codi("Gurb")
    private val barcelona = Repositori.codi("Barcelona")

    @Test
    fun `estats dels municipis`() {
        val descoberts = setOf(vic)
        assertEquals(EstatMunicipi.DESCOBERT, regles.estat(vic, descoberts))
        assertEquals(EstatMunicipi.DISPONIBLE, regles.estat(gurb, descoberts))
        assertEquals(EstatMunicipi.BOIRA, regles.estat(barcelona, descoberts))
    }

    @Test
    fun `desbloquejar un veí amb prou punts`() {
        val estat = EstatJoc(setOf(vic), puntsGuanyats = 100, puntsGastats = 0, missionsCompletades = emptySet())
        assertEquals(ResultatDesbloqueig.Permes(60), regles.avaluaDesbloqueig(gurb, estat))
    }

    @Test
    fun `sense prou punts diu quants en falten`() {
        val estat = EstatJoc(setOf(vic), puntsGuanyats = 50, puntsGastats = 0, missionsCompletades = emptySet())
        assertEquals(ResultatDesbloqueig.PuntsInsuficients(60, 10), regles.avaluaDesbloqueig(gurb, estat))
    }

    @Test
    fun `si en ajuntar dos mòbils el saldo queda negatiu, cal guanyar punts abans de desbloquejar`() {
        // Regla de la sincronització (requisits.md, 9.2): no es desfà res; el saldo negatiu només impedeix gastar.
        val estat = EstatJoc(setOf(vic), puntsGuanyats = 100, puntsGastats = 160, missionsCompletades = emptySet())
        assertEquals(ResultatDesbloqueig.PuntsInsuficients(60, 120), regles.avaluaDesbloqueig(gurb, estat))
    }

    @Test
    fun `un municipi a la boira no es pot desbloquejar i se'n sap la distància`() {
        val estat = EstatJoc(setOf(vic), puntsGuanyats = 10_000, puntsGastats = 0, missionsCompletades = emptySet())
        val r = regles.avaluaDesbloqueig(barcelona, estat)
        assertIs<ResultatDesbloqueig.NoDisponible>(r)
        assertEquals(Repositori.graf.distancies(setOf(vic))[barcelona], r.distancia)
    }

    @Test
    fun `un municipi descobert no es torna a desbloquejar`() {
        assertEquals(ResultatDesbloqueig.JaDescobert, regles.avaluaDesbloqueig(vic, EstatJoc(setOf(vic), 0, 0, emptySet())))
    }

    @Test
    fun `el bonus es dona en completar l'última missió del municipi`() {
        val missions = Repositori.missions.de(vic)
        var completades = emptySet<String>()
        missions.dropLast(1).forEach {
            assertEquals(0, regles.puntsPerCompletar(it, completades).bonus)
            completades = completades + it.id
        }
        val darrera = regles.puntsPerCompletar(missions.last(), completades)
        assertEquals(Repositori.config.punts.bonusTotesLesMissions, darrera.bonus)
        completades = completades + missions.last().id
        assertEquals(regles.puntsPossibles(vic), regles.puntsGuanyatsA(vic, completades))
        assertEquals(0, regles.puntsPerCompletar(missions.last(), completades).total)
    }
}
