package cat.descobreix.joc

import cat.descobreix.joc.geo.Localitzacio
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class LocalitzadorTest {
    private val loc = Repositori.localitzador

    private fun dins(nom: String, lat: Double, lon: Double) {
        assertEquals(Localitzacio.Dins(Repositori.codi(nom)), loc.localitza(lat, lon, 10.0), nom)
    }

    @Test
    fun `centres de ciutats`() {
        dins("Barcelona", 41.3870, 2.1700) // plaça de Catalunya
        dins("Girona", 41.9831, 2.8249)
        dins("Lleida", 41.6176, 0.6200)
        dins("Tarragona", 41.1189, 1.2445)
        dins("Llívia", 42.4636, 1.9800)
    }

    @Test
    fun `fora de Catalunya`() {
        assertEquals(Localitzacio.Fora, loc.localitza(41.30, 2.60, 10.0)) // mar
        assertEquals(Localitzacio.Fora, loc.localitza(43.20, 2.35, 10.0)) // França
    }

    @Test
    fun `a sobre d'una frontera pregunta`() {
        // Un vèrtex del límit de Vic és a la frontera amb un altre municipi.
        val vic = Repositori.codi("Vic")
        val anell = loc.limit(vic).poligons[0][0]
        val r = loc.localitza(anell[1], anell[0], 5.0)
        assertIs<Localitzacio.Dubte>(r)
        assertTrue(vic in r.candidats)
        assertTrue(r.candidats.size >= 2)
    }

    @Test
    fun `amb poca precisió pregunta`() {
        val r = loc.localitza(41.3870, 2.1700, 20_000.0)
        assertIs<Localitzacio.Dubte>(r)
        assertEquals(Repositori.codi("Barcelona"), r.candidats.first())
    }

    @Test
    fun `els límits són coherents amb l'índex`() {
        val vic = loc.limit(Repositori.codi("Vic"))
        assertTrue(vic.conte(41.9302, 2.2546))
        assertTrue(vic.distanciaAVoraMetres(41.9302, 2.2546) > 500)
    }
}
