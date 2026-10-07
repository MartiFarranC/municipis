package cat.descobreix

import androidx.lifecycle.SavedStateHandle
import cat.descobreix.domain.Joc
import cat.descobreix.joc.model.TipusProva
import cat.descobreix.joc.regles.Ubicacio
import cat.descobreix.municipality.MunicipiViewModel
import cat.descobreix.onboarding.OnboardingViewModel
import cat.descobreix.passaport.FaseSegellar
import cat.descobreix.passaport.SegellarViewModel
import cat.descobreix.ui.Missatge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelsTest {
    private val segells = SegellsEnMemoria()

    private val repositori = ProgresEnMemoria()
    private val fotos = FotosEnMemoria()
    private val sacs = SacsEnMemoria(repositori)
    private val joc = Joc(DadesDeProva, repositori, sacs, fotos)
    private val vic = DadesDeProva.codi("Vic")

    @Before
    fun abans() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun despres() = Dispatchers.resetMain()

    private fun ubicacioA(nom: String): Ubicacio {
        val (lat, lon) = DadesDeProva.puntDins(nom)
        return Ubicacio(lat, lon, 10.0)
    }

    @Test
    fun `onboarding cerca i comença la partida`() = runTest {
        val vm = OnboardingViewModel(joc, UbicacioFixa(null))
        vm.canviaText("hospitalet")
        val estat = vm.estat.first { it.resultats.isNotEmpty() }
        assertEquals("L'Hospitalet de Llobregat", estat.resultats.first().nom)

        vm.tria(estat.resultats.first())
        var comencat: String? = null
        vm.comenca { comencat = it }
        repositori.progres.first { it.partidaIniciada }
        assertEquals(estat.resultats.first().codi, comencat)
    }

    @Test
    fun `onboarding amb la ubicació tria el municipi on ets`() = runTest {
        val vm = OnboardingViewModel(joc, UbicacioFixa(ubicacioA("Vic")))
        vm.faServirUbicacio()
        val estat = vm.estat.first { !it.buscantUbicacio && it.seleccionat != null }
        assertEquals(vic, estat.seleccionat?.codi)
    }

    @Test
    fun `check-in amb GPS dins del municipi dona punts`() = runTest {
        joc.iniciaPartida(vic)
        val vm = municipi(vic, UbicacioFixa(ubicacioA("Vic")))
        vm.estat.first { !it.carregant }
        val checkin = DadesDeProva.dades.missions.de(vic).first { it.clau == "checkin" }
        assertEquals(TipusProva.GPS, checkin.prova)

        vm.provaGps(checkin)
        val estat = vm.estat.first { it.provant == null && it.avis != null }
        assertEquals(Missatge.PuntsGuanyats(100, 0), estat.avis)
        assertEquals(100, repositori.progresAra().saldo)
        // El check-in amb GPS dona el segell del passaport.
        assertEquals(vic, estat.segellPendent)
    }

    @Test
    fun `dins d'un municipi bloquejat no es pot fer res i diu quants punts falten`() = runTest {
        joc.iniciaPartida(vic)
        val vm = municipi(vic, UbicacioFixa(ubicacioA("Gurb")))
        vm.estat.first { !it.carregant }
        val checkin = DadesDeProva.dades.missions.de(vic).first { it.clau == "checkin" }

        vm.provaGps(checkin)
        val estat = vm.estat.first { it.provant == null && it.missatge != null }
        val m = assertIs<Missatge.MunicipiBloquejat>(estat.missatge)
        assertEquals("Gurb", m.nom)
        assertEquals(60, m.falten)
        assertEquals(0, repositori.progresAra().saldo)
    }

    @Test
    fun `precisió del GPS insuficient`() = runTest {
        joc.iniciaPartida(vic)
        val u = ubicacioA("Vic").copy(precisioMetres = 120.0)
        val vm = municipi(vic, UbicacioFixa(u))
        vm.estat.first { !it.carregant }
        vm.provaGps(DadesDeProva.dades.missions.de(vic).first { it.clau == "checkin" })
        val estat = vm.estat.first { it.provant == null && it.missatge != null }
        assertEquals(Missatge.PrecisioInsuficient(120, 50), estat.missatge)
    }

    @Test
    fun `desbloquejar des de la fitxa`() = runTest {
        joc.iniciaPartida(vic)
        DadesDeProva.dades.missions.de(vic).forEach { joc.completaMissio(it, null, null) }
        val gurb = DadesDeProva.codi("Gurb")
        val vm = municipi(gurb, UbicacioFixa(null))
        val abans = vm.estat.first { !it.carregant }
        assertEquals(true, abans.potDesbloquejar)
        vm.desbloqueja()
        val despres = vm.estat.first { it.avis != null }
        assertEquals(Missatge.Desbloquejat("Gurb"), despres.avis)
        assertEquals(listOf(vic, gurb), repositori.progresAra().descoberts)
    }

    private fun municipi(codi: String, ubicacio: UbicacioFixa) = MunicipiViewModel(
        joc,
        MissionsPropiesEnMemoria(),
        fotos,
        ubicacio,
        segells,
        SavedStateHandle(mapOf("codi" to codi)),
    )

    @Test
    fun `posar el segell al passaport`() = runTest {
        joc.iniciaPartida(vic)
        val vm = SegellarViewModel(joc, segells, PreferenciesEnMemoria(), SavedStateHandle(mapOf("codi" to vic)))
        vm.estat.first { !it.carregant }
        vm.obert()

        // A la cantonada, el segell es mou cap a dins perquè hi càpiga sencer.
        vm.toca(0f, 0f)
        val provisional = assertNotNull(vm.estat.value.provisional)
        assertTrue(provisional.x > 0f && provisional.y > 0f)
        assertEquals(FaseSegellar.DECIDINT, vm.estat.value.fase)

        vm.repeteix()
        assertNull(vm.estat.value.provisional)
        assertEquals(FaseSegellar.TRIANT, vm.estat.value.fase)

        vm.toca(.5f, .5f)
        vm.accepta()
        vm.estat.first { it.fase == FaseSegellar.FET }
        val desat = segells.segellsAra().single().posat
        assertEquals(vic, desat.codi)
        assertEquals(0, desat.pagina)
        assertEquals(.5f, desat.x)
    }
}

