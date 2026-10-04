package cat.descobreix

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import cat.descobreix.ui.theme.ColorSecundari
import cat.descobreix.ui.theme.Colors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ColorSecundariTest {
    private fun contrast(a: Color, b: Color): Float {
        val la = a.luminance()
        val lb = b.luminance()
        return (max(la, lb) + 0.05f) / (min(la, lb) + 0.05f)
    }

    private fun to(c: Color): Float {
        val r = c.red
        val g = c.green
        val b = c.blue
        val mx = maxOf(r, g, b)
        val d = mx - minOf(r, g, b)
        if (d == 0f) return 0f
        val h = when (mx) {
            r -> ((g - b) / d).mod(6f)
            g -> (b - r) / d + 2
            else -> (r - g) / d + 4
        }
        return h * 60
    }

    private fun distanciaTo(a: Color, b: Color): Float {
        val d = abs(to(a) - to(b))
        return min(d, 360 - d)
    }

    @Test
    fun `tots els colors es llegeixen sobre els fons de l'app`() {
        for (c in ColorSecundari.entries) {
            for (fons in listOf(Colors.Fons, Colors.Superficie, Colors.Superficie2, Colors.Disponible1)) {
                assertTrue(contrast(c.color, fons) >= 4.5f, "$c sobre $fons: ${contrast(c.color, fons)}")
            }
        }
    }

    @Test
    fun `cap color es confon amb l'ambre ni amb el d'error`() {
        for (c in ColorSecundari.entries) {
            assertTrue(distanciaTo(c.color, Colors.Ambre) >= 30f, "$c massa semblant a l'ambre")
            assertTrue(distanciaTo(c.color, Colors.Error) >= 30f, "$c massa semblant al color d'error")
        }
    }

    @Test
    fun `un valor desconegut torna el color per defecte`() {
        assertEquals(ColorSecundari.BLAU, ColorSecundari.perNom(null))
        assertEquals(ColorSecundari.BLAU, ColorSecundari.perNom("TARONJA"))
        assertEquals(ColorSecundari.LILA, ColorSecundari.perNom("LILA"))
    }
}
