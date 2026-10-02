package cat.descobreix.ui.theme

import android.provider.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val esquemaFosc = darkColorScheme(
    primary = Colors.Ambre,
    onPrimary = Colors.TintaAmbre,
    primaryContainer = Colors.Disponible1,
    onPrimaryContainer = Colors.Ambre,
    secondary = Colors.Blau,
    onSecondary = Colors.Fons,
    background = Colors.Fons,
    onBackground = Colors.Text,
    surface = Colors.Fons,
    onSurface = Colors.Text,
    surfaceVariant = Colors.Superficie,
    onSurfaceVariant = Colors.TextSecundari,
    surfaceContainerLowest = Colors.Fons,
    surfaceContainerLow = Colors.Superficie,
    surfaceContainer = Colors.Superficie,
    surfaceContainerHigh = Colors.Superficie2,
    surfaceContainerHighest = Colors.Superficie2,
    outline = Colors.Linia,
    outlineVariant = Colors.Linia,
    error = Colors.Error,
    onError = Colors.Fons,
)

private val formes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

/** Si l'usuari ha demanat al sistema que es redueixin les animacions. */
val LocalReduirAnimacions = staticCompositionLocalOf { false }

@Composable
fun DescobreixTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val reduir = remember(context) {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
    CompositionLocalProvider(LocalReduirAnimacions provides reduir) {
        MaterialTheme(colorScheme = esquemaFosc, typography = Tipografia, shapes = formes, content = content)
    }
}
