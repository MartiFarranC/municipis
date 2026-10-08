package cat.descobreix.joc

import cat.descobreix.joc.model.TipusMissio
import cat.descobreix.joc.model.TipusProva
import cat.descobreix.joc.regles.DisponibilitatOficial
import cat.descobreix.joc.regles.MissioAjuntament
import cat.descobreix.joc.regles.ReglesAjuntaments
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AjuntamentsTest {
    private val config = Repositori.config
    private val regles = ReglesAjuntaments(config)
    private val vic = Repositori.codi("Vic")
    private val avui = LocalDate.of(2026, 4, 1)

    private fun missio(id: String, ordre: Int = 0, inici: LocalDate? = null, fi: LocalDate? = null, retirada: Boolean = false) =
        MissioAjuntament(id, vic, "Missió $id", null, TipusProva.FOTO, null, null, null, inici, fi, ordre, retirada)

    @Test
    fun `una missió oficial dona les barretines de la configuració`() {
        assertEquals(config.ajuntaments.puntsMissio, regles.punts(missio("a")))
    }

    @Test
    fun `una festa dona el bonus de festa`() {
        val festa = missio("a", inici = avui, fi = avui.plusDays(1))
        assertEquals(config.ajuntaments.puntsMissio + config.ajuntaments.bonusFesta, regles.punts(festa))
    }

    @Test
    fun `una festa només es pot fer els seus dies, inclosos`() {
        val festa = missio("a", inici = LocalDate.of(2026, 4, 5), fi = LocalDate.of(2026, 4, 6))
        assertIs<DisponibilitatOficial.Abans>(regles.disponibilitat(festa, LocalDate.of(2026, 4, 4)))
        assertEquals(DisponibilitatOficial.Disponible, regles.disponibilitat(festa, LocalDate.of(2026, 4, 5)))
        assertEquals(DisponibilitatOficial.Disponible, regles.disponibilitat(festa, LocalDate.of(2026, 4, 6)))
        assertIs<DisponibilitatOficial.Passada>(regles.disponibilitat(festa, LocalDate.of(2026, 4, 7)))
    }

    @Test
    fun `només es fan servir les primeres missions de cada municipi, per ordre`() {
        val maxim = config.ajuntaments.maximMissionsPerMunicipi
        val totes = (0..maxim + 2).map { missio("m$it", ordre = maxim + 2 - it) }
        val fetes = regles.missionsDe(vic, totes, avui, emptySet())
        assertEquals(maxim, fetes.size)
        assertEquals("m${maxim + 2}", fetes.first().id)
    }

    @Test
    fun `les retirades no surten`() {
        val totes = listOf(missio("a"), missio("b", retirada = true))
        assertEquals(listOf("a"), regles.missionsDe(vic, totes, avui, emptySet()).map { it.id })
    }

    @Test
    fun `una festa passada només surt si s'havia fet`() {
        val passada = missio("a", inici = LocalDate.of(2026, 3, 1), fi = LocalDate.of(2026, 3, 2))
        assertTrue(regles.missionsDe(vic, listOf(passada), avui, emptySet()).isEmpty())
        assertEquals(1, regles.missionsDe(vic, listOf(passada), avui, setOf(passada.idMissio)).size)
    }

    @Test
    fun `el QR es comprova amb el resum, sense el codi`() {
        val text = "https://github.com/MartiFarranC/municipis?segell=abc123"
        val qr = missio("a").copy(prova = TipusProva.QR, resumQr = ReglesAjuntaments.resumQr(text))
        assertTrue(regles.qrValid(qr, text))
        assertTrue(regles.qrValid(qr, "  $text\n"))
        assertFalse(regles.qrValid(qr, "https://github.com/MartiFarranC/municipis?segell=abc124"))
        assertFalse(regles.qrValid(missio("b"), text))
    }

    @Test
    fun `el resum és el SHA-256 en hexadecimal`() {
        // El mateix que dona `encode(sha256('prova'), 'hex')` a Postgres.
        assertEquals("6258a5e0eb772911d4f92be5b5db0e14511edbe01d1d0ddd1d5a2cb9db9a56ba", ReglesAjuntaments.resumQr("prova"))
    }

    @Test
    fun `una missió oficial no dona el bonus de completar-les totes`() {
        val totes = Repositori.missions.de(vic).map { it.id }.toSet()
        val oficial = regles.comMissio(missio("a"))
        assertEquals(TipusMissio.OFICIAL, oficial.tipus)
        val punts = Repositori.regles.puntsPerCompletar(oficial, totes)
        assertEquals(config.ajuntaments.puntsMissio, punts.missio)
        assertEquals(0, punts.bonus)
    }

    @Test
    fun `una missió oficial no canvia els punts possibles del municipi`() {
        val abans = Repositori.regles.puntsPossibles(vic)
        regles.comMissio(missio("a"))
        assertEquals(abans, Repositori.regles.puntsPossibles(vic))
        assertFalse(Repositori.regles.municipiComplet(vic, setOf("aj-a")))
    }
}
