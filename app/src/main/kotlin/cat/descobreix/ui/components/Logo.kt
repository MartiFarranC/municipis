package cat.descobreix.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import cat.descobreix.R
import cat.descobreix.ui.theme.Colors
import cat.descobreix.ui.theme.LocalReduirAnimacions

private val VidreApagat = Color(0xFF3A4558)

/** El fanal només s'encén la primera vegada que es mostra mentre l'app és oberta. */
private var jaEnces = false

/**
 * El logo: el fanal de paret fent llum sobre Catalunya.
 * Si [encen] és cert, el fanal s'encén fent pampallugues, com un fanal de poble quan es fa fosc.
 */
@Composable
fun Logo(descripcio: String?, modifier: Modifier = Modifier, encen: Boolean = false) {
    val reduir = LocalReduirAnimacions.current
    val llum = remember { Animatable(if (encen && !reduir && !jaEnces) 0f else 1f) }
    LaunchedEffect(Unit) {
        if (llum.value < 1f) {
            llum.animateTo(
                1f,
                keyframes {
                    durationMillis = 1400
                    0f at 0
                    1f at 420
                    0.1f at 500
                    1f at 670
                    0.3f at 760
                    1f at 980
                },
            )
            jaEnces = true
        }
    }
    val semantica = if (descripcio != null) Modifier.semantics { contentDescription = descripcio } else Modifier
    Box(modifier.aspectRatio(1f).then(semantica)) {
        Image(painterResource(R.drawable.logo_catalunya), null, Modifier.fillMaxSize())
        Canvas(Modifier.fillMaxSize()) {
            // El mateix halo que la icona: centrat al vidre del fanal.
            val centre = Offset(size.width * .6555f, size.height * .603f)
            drawCircle(
                Brush.radialGradient(listOf(Colors.Ambre.copy(alpha = .5f * llum.value), Color.Transparent), centre, size.width * .2692f),
                size.width * .2692f,
                centre,
            )
        }
        Image(painterResource(R.drawable.logo_fanal), null, Modifier.fillMaxSize())
        Image(
            painterResource(R.drawable.logo_vidre),
            null,
            Modifier.fillMaxSize(),
            colorFilter = ColorFilter.tint(lerp(VidreApagat, Colors.Ambre, llum.value)),
        )
    }
}
