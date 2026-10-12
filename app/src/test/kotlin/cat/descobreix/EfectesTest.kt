package cat.descobreix

import cat.descobreix.ui.Efectes
import org.junit.Assert.assertTrue
import org.junit.Test

class EfectesTest {
    @Test
    fun `cada so es fabrica, és curt i no satura`() {
        for (so in Efectes.So.entries) {
            val mostres = Efectes.fabrica(so)
            assertTrue("$so és buit", mostres.isNotEmpty())
            assertTrue("$so dura massa", mostres.size < 44100)
            assertTrue("$so no sona", mostres.any { it.toInt() != 0 })
        }
    }
}
