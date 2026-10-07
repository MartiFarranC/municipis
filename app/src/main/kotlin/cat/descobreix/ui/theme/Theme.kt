package cat.descobreix.ui.theme

import android.provider.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private fun esquemaFosc(secundari: Color) = darkColorScheme(
    primary = secundari,
    onPrimary = Colors.TintaAmbre,
    primaryContainer = lerp(Colors.Fons, secundari, .12f),
    onPrimaryContainer = secundari,
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

/** L'animació de càrrega que ha triat l'usuari (l'id d'una dels sacs), o null per a la sardana. */
val LocalAnimacioCarrega = compositionLocalOf<String?> { null }

/**
 * @param color el color secundari (null per a l'ambre).
 * @param animacioCarrega l'animació de càrrega (null per a la sardana).
 */
@Composable
fun DescobreixTheme(color: Color? = null, animacioCarrega: String? = null, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val reduir = remember(context) {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
    SideEffect { ColorSecundari.actual = color ?: ColorSecundari.Ambre }
    val secundari = color ?: ColorSecundari.Ambre
    val esquema = remember(secundari) { esquemaFosc(secundari) }
    CompositionLocalProvider(LocalReduirAnimacions provides reduir, LocalAnimacioCarrega provides animacioCarrega) {
        MaterialTheme(colorScheme = esquema, typography = Tipografia, shapes = formes, content = content)
    }
}
