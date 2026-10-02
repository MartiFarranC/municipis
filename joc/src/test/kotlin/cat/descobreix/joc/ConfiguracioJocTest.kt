package cat.descobreix.joc

import cat.descobreix.joc.model.TipusProva
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConfiguracioJocTest {
    private val config = Repositori.config

    @Test
    fun `els valors inicials són els dels requisits`() {
        assertEquals(60, config.desbloqueig.costBase)
        assertEquals(5, config.desbloqueig.costIncrementPerMunicipi)
        assertEquals(200, config.desbloqueig.costMaxim)
        assertEquals(200, config.punts.minimGarantitPerMunicipi)
        assertEquals(400, config.punts.maximMissionsPerMunicipi)
        assertEquals(50, config.punts.bonusTotesLesMissions)
        assertEquals(75.0, config.gps.radiMissioMetres)
        assertEquals(50.0, config.gps.precisioMaximaMetres)
        assertEquals(50.0, config.gps.margeFronteraMetres)
        assertEquals(2048, config.fotos.costatLlargMaxim)
    }

    @Test
    fun `les missions genèriques garanteixen el mínim de punts`() {
        assertTrue(config.puntsGenericsPerMunicipi >= config.punts.minimGarantitPerMunicipi)
        assertTrue(config.puntsGenericsPerMunicipi <= config.punts.maximMissionsPerMunicipi)
    }

    @Test
    fun `el mínim garantit per municipi paga el desbloqueig més car`() {
        assertTrue(config.punts.minimGarantitPerMunicipi >= config.desbloqueig.costMaxim)
    }

    @Test
    fun `els tipus de prova són vàlids`() {
        val proves = config.missionsGeneriques.map { it.prova } + config.categoriesLloc.values.map { it.prova }
        proves.forEach { TipusProva.valueOf(it) }
    }

    @Test
    fun `la còpia dels assets és idèntica a la de dades`() {
        assertEquals(
            Repositori.fitxer("dades/configuracio_joc.json").readText(),
            File(Repositori.fitxer("app/src/main/assets/dades"), "configuracio_joc.json").readText(),
        )
    }
}
