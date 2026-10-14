package cat.descobreix.ui.theme

import android.provider.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private fun esquema(secundari: Color, fosc: Boolean): ColorScheme {
    val base = if (fosc) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = secundari,
        onPrimary = Colors.TintaAmbre,
        primaryContainer = lerp(Colors.Fons, secundari, .2f),
        onPrimaryContainer = Colors.Text,
        secondary = Colors.Text,
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
        outline = Colors.Text,
        outlineVariant = Colors.Linia,
        error = Colors.Error,
        onError = Colors.Fons,
    )
}

/** Cantonades rectes: res de píndoles (docs/disseny.md). */
private val formes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp),
)

/** Si l'usuari ha demanat al sistema que es redueixin les animacions. */
val LocalReduirAnimacions = staticCompositionLocalOf { false }

/** L'animació de càrrega que ha triat l'usuari (l'id d'una dels sacs), o null per a la sardana. */
val LocalAnimacioCarrega = compositionLocalOf<String?> { null }

/**
 * @param color el color fort (null per al groc).
 * @param animacioCarrega l'animació de càrrega (null per a la sardana).
 */
@Composable
fun DescobreixTheme(color: Color? = null, animacioCarrega: String? = null, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val reduir = remember(context) {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
    val fosc = isSystemInDarkTheme()
    val secundari = color ?: ColorSecundari.Groc
    // Els colors es llegeixen d'aquests estats mentre es pinta: s'han de posar abans de pintar res.
    Tema.fosc = fosc
    ColorSecundari.actual = secundari
    val esquema = remember(secundari, fosc) { esquema(secundari, fosc) }
    val tipografia = remember(fosc) { tipografia() }
    CompositionLocalProvider(LocalReduirAnimacions provides reduir, LocalAnimacioCarrega provides animacioCarrega) {
        MaterialTheme(colorScheme = esquema, typography = tipografia, shapes = formes, content = content)
    }
}
