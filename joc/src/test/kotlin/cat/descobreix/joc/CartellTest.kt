package cat.descobreix.joc

import cat.descobreix.joc.cartell.Cartell
import cat.descobreix.joc.cartell.Rectangle
import cat.descobreix.joc.cartell.Requadre
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CartellTest {
    @Test
    fun `el requadre de tota la vista quan la foto té la seva proporció`() {
        assertEquals(Rectangle(0, 0, 3000, 4000), Cartell.retall(Requadre(0f, 0f, 1f, 1f), 300, 400, 3000, 4000))
        assertEquals(Rectangle(750, 1000, 2250, 3000), Cartell.retall(Requadre(.25f, .25f, .75f, .75f), 300, 400, 3000, 4000))
    }

    @Test
    fun `si la vista és més allargada, els costats de la foto no es veuen`() {
        // Foto 3:4 en una vista 1:2: la foto s'escala a l'alçada i se'n veu la franja del mig.
        val r = Cartell.retall(Requadre(0f, 0f, 1f, 1f), 200, 400, 3000, 4000)
        assertEquals(Rectangle(500, 0, 2500, 4000), r)
    }

    @Test
    fun `el retall no surt de la foto i accepta el requadre girat`() {
        val r = Cartell.retall(Requadre(1.2f, 1.1f, -.1f, -.3f), 300, 400, 3000, 4000)
        assertEquals(Rectangle(0, 0, 3000, 4000), r)
    }

    @Test
    fun `normalitza treu accents, apòstrofs i la ela geminada`() {
        assertEquals("LAMETLLADEMAR", Cartell.normalitza("L'Ametlla de Mar"))
        assertEquals("SANTJOANDELESABADESSES", Cartell.normalitza("Sant Joan de les Abadesses"))
        assertEquals("CASTELLOLI", Cartell.normalitza("Castellolí"))
        assertEquals("SANTFELIUDELLOBREGAT", Cartell.normalitza("Sant Feliu de Llobregat"))
        assertEquals("SANTPEREDERIUDEBITLLES", Cartell.normalitza("Sant Pere de Riudebitlles"))
    }

    @Test
    fun `el nom es troba dins del text del cartell, amb errors de lectura`() {
        assertTrue(Cartell.diuElNom("VIC\nOsona", "Vic"))
        assertTrue(Cartell.diuElNom("Benvinguts a\nSANT JOAN DE LES ABADESSES", "Sant Joan de les Abadesses"))
        assertTrue(Cartell.diuElNom("SANT JOAN DE LES ABADESSE5", "Sant Joan de les Abadesses"))
        assertTrue(Cartell.diuElNom("L AMETLLA DE MAR", "L'Ametlla de Mar"))
        assertTrue(Cartell.diuElNom("castellolí", "Castellolí"))
    }

    @Test
    fun `un altre nom no passa`() {
        assertFalse(Cartell.diuElNom("GURB", "Vic"))
        assertFalse(Cartell.diuElNom("", "Vic"))
        assertFalse(Cartell.diuElNom("SANT JOAN DESPI", "Sant Joan de les Abadesses"))
        assertFalse(Cartell.diuElNom("TONA", "Moià"))
    }
}
