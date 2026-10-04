package cat.descobreix

import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToString
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cat.descobreix.data.compte.EstatCompte
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

/** Sense sessió, l'app demana entrar i triar el nom d'usuari abans de deixar jugar. */
@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
@UninstallModules(ModulCompte::class)
@RunWith(AndroidJUnit4::class)
class CompteUiTest {
    @get:Rule(order = 0)
    val hilt = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val regla = createEmptyComposeRule()

    @BindValue
    @JvmField
    val compte: ServeiCompte = CompteFals(EstatCompte.SenseSessio)

    private var escenari: ActivityScenario<MainActivity>? = null

    @Before
    fun abans() {
        hilt.inject()
        InstrumentationRegistry.getInstrumentation().targetContext.deleteDatabase(BaseDades.NOM)
        escenari = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun despres() {
        escenari?.close()
    }

    private fun espera(text: String) {
        try {
            regla.waitUntilAtLeastOneExists(hasText(text, substring = true), 20_000)
        } catch (e: ComposeTimeoutException) {
            val arbre = regla.onAllNodes(isRoot(), useUnmergedTree = true).printToString(maxDepth = 30)
            throw AssertionError("No apareix \"$text\". Pantalla:\n$arbre", e)
        }
    }

    @Test
    fun entrarITriarElNomAbansDeJugar() {
        espera("Correu electrònic")
        // Sense correu vàlid no es pot entrar.
        regla.onNode(hasText("Entra") and hasClickAction()).assertIsNotEnabled()

        regla.onAllNodes(hasSetTextAction()).onFirst().performTextInput("anna@example.com")
        regla.onAllNodes(hasSetTextAction()).onLast().performTextInput("contrasenya")
        regla.onNode(hasText("Entra") and hasClickAction()).performClick()

        espera("Tria el teu nom")
        // Un nom amb caràcters no permesos no es pot desar.
        regla.onNode(hasSetTextAction()).performTextInput("Anna!")
        regla.onNodeWithText("Continua").assertIsNotEnabled()
        regla.onNode(hasSetTextAction()).performTextClearance()
        regla.onNode(hasSetTextAction()).performTextInput("anna_1")
        regla.onNodeWithText("Continua").performClick()

        espera("D'on ets?")
    }
}
