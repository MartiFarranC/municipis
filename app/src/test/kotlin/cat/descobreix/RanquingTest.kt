package cat.descobreix

import cat.descobreix.data.ranquing.CriteriRanquing
import cat.descobreix.data.ranquing.FilaRanquing
import cat.descobreix.data.ranquing.ServeiRanquing
import cat.descobreix.profile.RanquingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Un rànquing en memòria que apunta les crides. */
class RanquingEnMemoria : ServeiRanquing {
    val crides = mutableListOf<String>()
    var falla = false
    var fallaPujada = false
    var files = listOf(
        FilaRanquing(1, "b", "biel", null, 300, 4, socJo = false),
        FilaRanquing(2, "a", "anna", "a/1.jpg", 200, 6, socJo = true),
    )

    override suspend fun classificacio(criteri: CriteriRanquing, nomesSeguits: Boolean): List<FilaRanquing> {
        crides += "classificacio $criteri ${if (nomesSeguits) "seguits" else "general"}"
        if (falla) throw IOException("sense connexió")
        return files
    }

    override suspend fun pujaProgres() {
        crides += "puja"
        if (fallaPujada) throw IOException("sense connexió")
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class RanquingTest {
    private val ranquing = RanquingEnMemoria()

    @Before
    fun abans() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun despres() = Dispatchers.resetMain()

    @Test
    fun `en obrir-lo, puja el progrés abans de carregar la classificació general per punts`() = runTest {
        val vm = RanquingViewModel(ranquing)
        assertEquals(listOf("puja", "classificacio PUNTS general"), ranquing.crides)
        val e = vm.estat.value
        assertFalse(e.carregant)
        assertEquals(2, e.files.size)
        assertEquals("anna", e.jo?.nomUsuari)
    }

    @Test
    fun `canviar el criteri o l'àmbit torna a carregar`() = runTest {
        val vm = RanquingViewModel(ranquing)
        vm.canviaCriteri(CriteriRanquing.MUNICIPIS)
        vm.canviaAmbit(nomesSeguits = true)
        vm.canviaAmbit(nomesSeguits = true)
        assertEquals(
            listOf("classificacio PUNTS general", "classificacio MUNICIPIS general", "classificacio MUNICIPIS seguits"),
            ranquing.crides.filter { it != "puja" },
        )
    }

    @Test
    fun `si no es pot pujar el progrés, la classificació es carrega igualment`() = runTest {
        ranquing.fallaPujada = true
        val vm = RanquingViewModel(ranquing)
        assertFalse(vm.estat.value.error)
        assertEquals(2, vm.estat.value.files.size)
    }

    @Test
    fun `sense connexió ho diu i es pot tornar a provar`() = runTest {
        ranquing.falla = true
        val vm = RanquingViewModel(ranquing)
        assertTrue(vm.estat.value.error)
        assertTrue(vm.estat.value.files.isEmpty())

        ranquing.falla = false
        vm.actualitza()
        assertFalse(vm.estat.value.error)
        assertEquals(2, vm.estat.value.files.size)
    }

    @Test
    fun `si no segueix ningú, la classificació de seguits només té l'usuari`() = runTest {
        ranquing.files = listOf(FilaRanquing(1, "a", "anna", null, 0, 1, socJo = true))
        val vm = RanquingViewModel(ranquing)
        assertFalse(vm.estat.value.senseSeguits, "a la general no és cap avís")
        vm.canviaAmbit(nomesSeguits = true)
        assertTrue(vm.estat.value.senseSeguits)
    }
}
