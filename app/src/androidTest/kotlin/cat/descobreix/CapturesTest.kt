package cat.descobreix

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToString
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
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Captures de les pantalles principals, en clar i en fosc, per revisar l'estètica sense instal·lar l'app.
 * Es desen a la carpeta de l'app al mòbil (files/captures) i el CI les puja com a artefacte.
 */
@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
@UninstallModules(ModulCompte::class)
@RunWith(AndroidJUnit4::class)
class CapturesTest {
    @get:Rule(order = 0)
    val hilt = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val regla = createEmptyComposeRule()

    @BindValue
    @JvmField
    val compte: ServeiCompte = CompteFals()

    private var escenari: ActivityScenario<MainActivity>? = null
    private val instrumentacio = InstrumentationRegistry.getInstrumentation()
    private val carpeta: File by lazy {
        File(instrumentacio.targetContext.getExternalFilesDir(null), "captures").apply { mkdirs() }
    }

    @After
    fun despres() {
        escenari?.close()
        modeNit(false)
    }

    private fun modeNit(fosc: Boolean) {
        instrumentacio.uiAutomation.executeShellCommand("cmd uimode night ${if (fosc) "yes" else "no"}").close()
        Thread.sleep(1500)
    }

    private fun espera(text: String, temps: Long = 30_000) {
        try {
            regla.waitUntilAtLeastOneExists(hasText(text, substring = true), temps)
        } catch (e: ComposeTimeoutException) {
            val arbre = regla.onAllNodes(isRoot(), useUnmergedTree = true).printToString(maxDepth = 30)
            throw AssertionError("No apareix \"$text\". Pantalla:\n$arbre", e)
        }
    }

    /** Desa la pantalla en JPEG, reduïda a 540 píxels d'ample. */
    private fun captura(nom: String) {
        regla.waitForIdle()
        Thread.sleep(600)
        val imatge = regla.onRoot().captureToImage().asAndroidBitmap()
        val ample = 540
        val petita = Bitmap.createScaledBitmap(imatge, ample, imatge.height * ample / imatge.width, true)
        File(carpeta, "$nom.jpg").outputStream().use { petita.compress(Bitmap.CompressFormat.JPEG, 85, it) }
    }

    private fun recorregut(tema: String) {
        instrumentacio.targetContext.deleteDatabase(BaseDades.NOM)
        escenari = ActivityScenario.launch(MainActivity::class.java)
        espera("D'on ets?")
        captura("$tema-01-inici")
        regla.onNode(hasSetTextAction()).performTextInput("Vic")
        espera("Osona")
        regla.onAllNodesWithText("Osona").onFirst().performClick()
        captura("$tema-02-tria")
        regla.onNodeWithText("Comença l'aventura").performClick()
        espera("de 947 municipis descoberts")
        Thread.sleep(1500)
        captura("$tema-03-mapa")
        regla.onNodeWithText("Missions").performClick()
        espera("Vic")
        captura("$tema-04-missions")
        regla.onAllNodesWithText("Vic").onFirst().performClick()
        espera("Osona · El teu municipi d'inici")
        captura("$tema-05-fitxa")
        regla.onNode(hasScrollAction()).performScrollToNode(hasText("Fes check-in al municipi"))
        captura("$tema-06-fitxa-missions")
        escenari?.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        regla.onNodeWithText("Perfil").performClick()
        espera("Nivell 1")
        captura("$tema-07-perfil")
        regla.onNode(hasScrollAction()).performScrollToNode(hasText("So i vibració"))
        captura("$tema-08-perfil-opcions")
        regla.onNode(hasScrollAction()).performScrollToNode(hasText("Sacs i col·lecció"))
        regla.onNodeWithText("Sacs i col·lecció").performClick()
        Thread.sleep(1000)
        captura("$tema-09-sacs")
        escenari?.close()
        escenari = null
    }

    @Test
    fun capturesEnClarIEnFosc() {
        hilt.inject()
        modeNit(false)
        recorregut("clar")
        modeNit(true)
        recorregut("fosc")
    }
}
