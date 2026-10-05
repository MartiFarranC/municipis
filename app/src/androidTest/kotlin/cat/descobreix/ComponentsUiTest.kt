package cat.descobreix

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import cat.descobreix.ui.Celebracio
import cat.descobreix.ui.DialegMissatge
import cat.descobreix.ui.DialegTriaMunicipi
import cat.descobreix.ui.Missatge
import cat.descobreix.ui.components.BotoPrincipal
import cat.descobreix.ui.theme.DescobreixTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComponentsUiTest {
    @get:Rule
    val regla = createComposeRule()

    @Test
    fun triarMunicipiALaFrontera() {
        var triat: String? = null
        regla.setContent {
            DescobreixTheme {
                DialegTriaMunicipi(listOf("08298" to "Vic", "08100" to "Gurb"), onTria = { triat = it }, onCancela = {})
            }
        }
        regla.onNodeWithText("On ets?").assertIsDisplayed()
        regla.onNodeWithText("Gurb").performClick()
        assertEquals("08100", triat)
    }

    @Test
    fun missatgeDeMunicipiBloquejat() {
        regla.setContent {
            DescobreixTheme {
                DialegMissatge(Missatge.MunicipiBloquejat("Gurb", 25, null), onTanca = {})
            }
        }
        regla.onNodeWithText("Municipi bloquejat").assertIsDisplayed()
        regla.onNodeWithText("Et falten 25 punts", substring = true).assertIsDisplayed()
    }

    @Test
    fun botoDesactivat() {
        regla.setContent {
            DescobreixTheme { BotoPrincipal("Desbloqueja · 60 pts", onClick = {}, enabled = false) }
        }
        regla.onNodeWithText("Desbloqueja · 60 pts").assertIsNotEnabled()
    }

    @Test
    fun celebracioDeMissioCompletada() {
        var tancada = false
        regla.setContent {
            DescobreixTheme {
                Celebracio(Missatge.PuntsGuanyats(100, 0)) { tancada = true }
            }
        }
        regla.onNodeWithText("Missió completada!").assertIsDisplayed()
        // Després de la confirmació arriba el premi, amb els punts i el botó per continuar.
        regla.mainClock.advanceTimeBy(3000)
        regla.onNodeWithText("+100").assertIsDisplayed()
        regla.onNodeWithText("Continua").performClick()
        assertTrue(tancada)
    }
}
