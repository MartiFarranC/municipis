package cat.descobreix.joc

import cat.descobreix.joc.cartell.Cartell
import cat.descobreix.joc.cartell.Rectangle
import cat.descobreix.joc.cartell.Requadre
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CartellTest {
    private val LLETRES = Repositori.config.fotos.lletresPerErrorCartell

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
        assertTrue(Cartell.diuElNom("VIC\nOsona", "Vic", LLETRES))
        assertTrue(Cartell.diuElNom("Benvinguts a\nSANT JOAN DE LES ABADESSES", "Sant Joan de les Abadesses", LLETRES))
        assertTrue(Cartell.diuElNom("SANT JOAN DE LES ABADESSE5", "Sant Joan de les Abadesses", LLETRES))
        assertTrue(Cartell.diuElNom("L AMETLLA DE MAR", "L'Ametlla de Mar", LLETRES))
        assertTrue(Cartell.diuElNom("castellolí", "Castellolí", LLETRES))
    }

    @Test
    fun `el nom passa encara que hi hagi més text o lletres que no es veuen`() {
        assertTrue(Cartell.diuElNom("SOM REPÚBLICA\nSANT FRUITÓS\nDE BAGES", "Sant Fruitós de Bages", LLETRES))
        assertTrue(Cartell.diuElNom("SOM\nREPUBLICA TORELLO", "Torelló", LLETRES))
        // Lletres tapades o confoses amb altres.
        assertTrue(Cartell.diuElNom("SANT FR TOS DE BAG S", "Sant Fruitós de Bages", LLETRES))
        assertTrue(Cartell.diuElNom("T0RELL0", "Torelló", LLETRES))
        assertTrue(Cartell.diuElNom("C4STELLOLI", "Castellolí", LLETRES))
        assertTrue(Cartell.diuElNom("8ANYOLES", "Banyoles", LLETRES))
    }

    @Test
    fun `un altre nom no passa`() {
        assertFalse(Cartell.diuElNom("GURB", "Vic", LLETRES))
        assertFalse(Cartell.diuElNom("", "Vic", LLETRES))
        assertFalse(Cartell.diuElNom("SANT JOAN DESPI", "Sant Joan de les Abadesses", LLETRES))
        assertFalse(Cartell.diuElNom("TONA", "Moià", LLETRES))
        assertFalse(Cartell.diuElNom("SANT FELIU DE GUÍXOLS", "Sant Feliu de Llobregat", LLETRES))
        assertFalse(Cartell.diuElNom("SANT CUGAT DEL VALLÈS", "Sant Quirze del Vallès", LLETRES))
    }
}
