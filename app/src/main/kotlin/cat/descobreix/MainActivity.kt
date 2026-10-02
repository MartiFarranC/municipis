package cat.descobreix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.toArgb
import cat.descobreix.navegacio.AppNavegacio
import cat.descobreix.ui.theme.Colors
import cat.descobreix.ui.theme.DescobreixTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Colors.Fons.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(Colors.Fons.toArgb()),
        )
        super.onCreate(savedInstanceState)
        setContent {
            DescobreixTheme {
                AppNavegacio()
            }
        }
    }
}
