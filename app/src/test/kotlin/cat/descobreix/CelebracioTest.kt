package cat.descobreix

import cat.descobreix.ui.Missatge
import cat.descobreix.ui.esCelebracio
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CelebracioTest {
    @Test
    fun `es celebren els punts i els desbloquejos`() {
        assertTrue(Missatge.PuntsGuanyats(100, 0).esCelebracio)
        assertTrue(Missatge.FotoDesada(50, 0).esCelebracio)
        assertTrue(Missatge.Desbloquejat("Gurb").esCelebracio)
    }

    @Test
    fun `no es celebra el que no dona punts ni els errors`() {
        assertFalse(Missatge.FotoDesada(0, 0).esCelebracio)
        assertFalse(Missatge.PuntsGuanyats(0, 0).esCelebracio)
        assertFalse(Missatge.Error.esCelebracio)
        assertFalse(Missatge.SenseUbicacio.esCelebracio)
    }
}
