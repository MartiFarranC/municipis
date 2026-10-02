package cat.descobreix.joc

import cat.descobreix.joc.cerca.Cercador
import kotlin.test.Test
import kotlin.test.assertEquals

class CercadorTest {
    private val cercador = Cercador(Repositori.geografia.municipis)

    private fun primer(text: String) = cercador.cerca(text).first().nom

    @Test
    fun `ignora accents i majúscules`() {
        assertEquals("Lleida", primer("LLEIDA"))
        assertEquals("Puigcerdà", primer("puigcerda"))
        assertEquals("Llívia", primer("llivia"))
    }

    @Test
    fun `ignora els articles`() {
        assertEquals("L'Hospitalet de Llobregat", primer("hospitalet"))
        assertEquals("La Seu d'Urgell", primer("seu d'urgell"))
        assertEquals("El Pont de Suert", primer("pont de suert"))
        assertEquals("Les Borges Blanques", primer("borges blanques"))
    }

    @Test
    fun `primer els que comencen pel text`() {
        assertEquals("Vic", primer("vic"))
    }

    @Test
    fun `normalitza`() {
        assertEquals("hospitalet llobregat", Cercador.normalitza("L'Hospitalet de Llobregat").replace(" de ", " "))
        assertEquals("vielha e mijaran", Cercador.normalitza("Vielha e Mijaran"))
        assertEquals(emptyList(), cercador.cerca("   "))
    }
}
