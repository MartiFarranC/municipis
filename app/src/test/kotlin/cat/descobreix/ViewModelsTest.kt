package cat.descobreix

import androidx.lifecycle.SavedStateHandle
import cat.descobreix.domain.Joc
import cat.descobreix.joc.model.TipusProva
import cat.descobreix.joc.regles.Ubicacio
import cat.descobreix.municipality.MunicipiViewModel
import cat.descobreix.onboarding.OnboardingViewModel
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

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelsTest {
    private val repositori = ProgresEnMemoria()
    private val joc = Joc(DadesDeProva, repositori)
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
        FotosEnMemoria(),
        ubicacio,
        SavedStateHandle(mapOf("codi" to codi)),
    )
}
