package cat.descobreix

import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.printToString
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
import cat.descobreix.data.compte.ServeiCompte
import cat.descobreix.data.db.BaseDades
import cat.descobreix.di.ModulCompte
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * El flux principal amb l'app real: triar el municipi, veure el mapa, les missions, la fitxa i el perfil.
 * El compte és fals (ja hi ha sessió), perquè el test no depengui del servidor.
 */
@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
@UninstallModules(ModulCompte::class)
@RunWith(AndroidJUnit4::class)
class FluxPrincipalTest {
    @get:Rule(order = 0)
    val hilt = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val regla = createEmptyComposeRule()

    @BindValue
    @JvmField
    val compte: ServeiCompte = CompteFals()

    private var escenari: ActivityScenario<MainActivity>? = null

    @Before
    fun abans() {
        hilt.inject()
        // Comença sempre sense partida.
        InstrumentationRegistry.getInstrumentation().targetContext.deleteDatabase(BaseDades.NOM)
        escenari = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun despres() {
        escenari?.close()
    }

    private fun espera(text: String, temps: Long = 20_000) {
        try {
            regla.waitUntilAtLeastOneExists(hasText(text, substring = true), temps)
        } catch (e: ComposeTimeoutException) {
            // Mostra què hi ha a la pantalla, per entendre per què no hi és.
            val arbre = regla.onAllNodes(isRoot(), useUnmergedTree = true).printToString(maxDepth = 30)
            throw AssertionError("No apareix \"$text\". Pantalla:\n$arbre", e)
        }
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
        regla.onNode(hasScrollAction()).performScrollToNode(hasText("Medalles"))
    }
}
