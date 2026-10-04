package cat.descobreix

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cat.descobreix.data.db.BaseDades
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** El flux principal amb l'app real: triar el municipi, veure el mapa, les missions, la fitxa i el perfil. */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class FluxPrincipalTest {
    @get:Rule
    val regla = createEmptyComposeRule()

    private var escenari: ActivityScenario<MainActivity>? = null

    @Before
    fun abans() {
        // Comença sempre sense partida.
        InstrumentationRegistry.getInstrumentation().targetContext.deleteDatabase(BaseDades.NOM)
        escenari = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun despres() {
        escenari?.close()
    }

    private fun espera(text: String, temps: Long = 20_000) {
        regla.waitUntilAtLeastOneExists(hasText(text, substring = true), temps)
    }

    @Test
    fun triarMunicipiIJugar() {
        espera("D'on ets?")
        regla.onNode(hasSetTextAction()).performTextInput("Vic")
        espera("Osona")
        // El primer resultat és Vic (Osona).
        regla.onAllNodesWithText("Osona").onFirst().performClick()
        regla.onNodeWithText("Comença l'aventura").performClick()

        // Mapa amb el comptador sobre 947.
        espera("de 947 municipis descoberts", 30_000)

        regla.onNodeWithText("Missions").performClick()
        espera("Vic")
        regla.onAllNodesWithText("Vic").onFirst().performClick()

        // Fitxa de Vic amb les missions genèriques.
        espera("Osona · El teu municipi d'inici")
        regla.onNode(hasScrollAction()).performScrollToNode(hasText("Fes check-in al municipi"))
        regla.onNode(hasScrollAction()).performScrollToNode(hasText("Municipis veïns"))

        escenari?.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        regla.onNodeWithText("Perfil").performClick()
        espera("Nivell 1")
        regla.onNode(hasScrollAction()).performScrollToNode(hasText("Assoliments"))
    }
}
