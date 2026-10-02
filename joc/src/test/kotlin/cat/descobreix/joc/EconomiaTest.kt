package cat.descobreix.joc

import cat.descobreix.joc.model.TipusMissio
import cat.descobreix.joc.regles.EstatJoc
import cat.descobreix.joc.regles.ResultatDesbloqueig
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.test.fail

/** Regla obligatòria (secció 3.3): el joc no pot quedar mai encallat. */
class EconomiaTest {
    private val regles = Repositori.regles
    private val config = Repositori.config
    private val total = Repositori.geografia.total

    @Test
    fun `cost de desbloquejar segons la fórmula dels requisits`() {
        assertEquals(60, regles.cost(1))
        assertEquals(65, regles.cost(2))
        assertEquals(105, regles.cost(10))
        assertEquals(200, regles.cost(29))
        assertEquals(200, regles.cost(30))
        assertEquals(200, regles.cost(total))
    }

    @Test
    fun `cada municipi garanteix prou punts per pagar el desbloqueig següent`() {
        for (n in 1 until total) {
            assertTrue(config.punts.minimGarantitPerMunicipi >= regles.cost(n), "Amb $n municipis")
        }
    }

    @Test
    fun `amb només els punts garantits sempre es pot pagar el desbloqueig següent`() {
        // Després de tenir n municipis descoberts, s'han guanyat com a mínim 200·n punts
        // i s'han gastat cost(1) + ... + cost(n-1).
        var gastat = 0
        for (n in 1 until total) {
            val guanyatMinim = config.punts.minimGarantitPerMunicipi * n
            assertTrue(guanyatMinim - gastat >= regles.cost(n), "Encallat amb $n municipis")
            gastat += regles.cost(n)
        }
    }

    @Test
    fun `tots els municipis tenen missions genèriques que garanteixen el mínim`() {
        for (m in Repositori.geografia.municipis) {
            val generiques = Repositori.missions.de(m.codi).filter { it.tipus == TipusMissio.GENERICA }
            assertTrue(generiques.sumOf { it.punts } >= config.punts.minimGarantitPerMunicipi, m.nom)
        }
    }

    @Test
    fun `cap municipi supera el màxim de punts`() {
        for (m in Repositori.geografia.municipis) {
            assertTrue(Repositori.missions.de(m.codi).sumOf { it.punts } <= config.punts.maximMissionsPerMunicipi, m.nom)
        }
    }

    @Test
    fun `simulació fent només les missions genèriques es descobreix tot Catalunya`() {
        val inicis = listOf(Repositori.codi("Llívia"), Repositori.codi("Barcelona"), Repositori.codi("Vielha e Mijaran")) +
            Repositori.geografia.municipis.shuffled(Random(7)).take(5).map { it.codi }
        for (inici in inicis) simula(inici, Random(inici.hashCode()))
    }

    private fun simula(inici: String, random: Random) {
        var estat = EstatJoc(setOf(inici), 0, 0, emptySet())
        val pendents = ArrayDeque(listOf(inici))
        while (estat.descoberts.size < total) {
            // L'usuari fa les missions genèriques del municipi que acaba de descobrir.
            while (pendents.isNotEmpty()) {
                val codi = pendents.removeFirst()
                val generiques = Repositori.missions.de(codi).filter { it.tipus == TipusMissio.GENERICA }
                var completades = estat.missionsCompletades
                var guanyats = estat.puntsGuanyats
                for (m in generiques) {
                    guanyats += regles.puntsPerCompletar(m, completades).total
                    completades = completades + m.id
                }
                estat = estat.copy(puntsGuanyats = guanyats, missionsCompletades = completades)
            }
            val disponibles = regles.graf.disponibles(estat.descoberts)
            if (disponibles.isEmpty()) fail("Sense municipis disponibles amb ${estat.descoberts.size} descoberts")
            val tria = disponibles.random(random)
            val resultat = regles.avaluaDesbloqueig(tria, estat)
            if (resultat !is ResultatDesbloqueig.Permes) fail("Encallat amb ${estat.descoberts.size} municipis: $resultat")
            estat = estat.copy(descoberts = estat.descoberts + tria, puntsGastats = estat.puntsGastats + resultat.cost)
            pendents.add(tria)
        }
        assertIs<ResultatDesbloqueig.JaDescobert>(regles.avaluaDesbloqueig(inici, estat))
        assertTrue(estat.saldo >= 0)
    }
}
