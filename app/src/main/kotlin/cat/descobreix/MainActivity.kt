package cat.descobreix

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.toArgb
import cat.descobreix.data.compte.ServeiCompte
import cat.descobreix.navegacio.AppNavegacio
import cat.descobreix.ui.theme.Colors
import cat.descobreix.ui.theme.DescobreixTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var compte: ServeiCompte

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Colors.Fons.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(Colors.Fons.toArgb()),
        )
        super.onCreate(savedInstanceState)
        // Enllaços dels correus de Supabase (confirmació, enllaç màgic i recuperació de contrasenya).
        if (savedInstanceState == null) compte.gestionaEnllac(intent)
        setContent {
            DescobreixTheme {
                AppNavegacio()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        compte.gestionaEnllac(intent)
    }
}
