package cat.descobreix

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cat.descobreix.data.compte.ServeiCompte
import cat.descobreix.data.repositori.PreferenciesRepositori
import cat.descobreix.navegacio.AppNavegacio
import cat.descobreix.sacs.colorSac
import cat.descobreix.ui.Efectes
import cat.descobreix.ui.LocalEfectes
import cat.descobreix.ui.theme.DescobreixTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var compte: ServeiCompte

    @Inject
    lateinit var preferencies: PreferenciesRepositori

    private val efectes by lazy { Efectes(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Les barres del sistema segueixen el tema clar o fosc del mòbil, com l'app.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // Enllaços dels correus de Supabase (confirmació, enllaç màgic i recuperació de contrasenya).
        if (savedInstanceState == null) compte.gestionaEnllac(intent)
        setContent {
            val color by preferencies.colorApp.collectAsStateWithLifecycle(null)
            val animacio by preferencies.animacioCarrega.collectAsStateWithLifecycle(null)
            val so by preferencies.soIVibracio.collectAsStateWithLifecycle(true)
            efectes.actiu = so
            DescobreixTheme(color?.let { colorSac(it) }, animacio) {
                CompositionLocalProvider(LocalEfectes provides efectes) {
                    AppNavegacio()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        compte.gestionaEnllac(intent)
    }
}
