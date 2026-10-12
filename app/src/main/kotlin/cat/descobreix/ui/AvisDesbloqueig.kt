package cat.descobreix.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.descobreix.R
import cat.descobreix.ui.theme.Caveat
import cat.descobreix.ui.theme.Colors
import cat.descobreix.ui.theme.LocalReduirAnimacions
import kotlinx.coroutines.delay

/**
 * Quan es desbloqueja un municipi: una frase feta catalana a l'atzar, escrita a mà, a dalt de la pantalla, mentre els
 * núvols del mapa s'obren (docs/disseny.md). No tapa res ni atura els tocs, i se'n va sola al cap de tres segons.
 */
@Composable
fun AvisDesbloqueig(missatge: Missatge.Desbloquejat, onTanca: () -> Unit) {
    val reduir = LocalReduirAnimacions.current
    val frases = stringArrayResource(R.array.frases_fetes)
    val frase = remember(missatge) { frases.random() }
    val visible = remember(missatge) { Animatable(if (reduir) 1f else 0f) }
    LaunchedEffect(missatge) {
        visible.animateTo(1f, tween(if (reduir) 0 else 350))
        delay(2600)
        visible.animateTo(0f, tween(if (reduir) 0 else 300))
        onTanca()
    }
    Box(Modifier.fillMaxSize().statusBarsPadding().padding(top = 72.dp, start = 24.dp, end = 24.dp), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .semantics { liveRegion = LiveRegionMode.Polite }
                .graphicsLayer {
                    alpha = visible.value
                    translationY = (1f - visible.value) * 24f
                    rotationZ = -3f
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                frase,
                fontFamily = Caveat,
                fontSize = 34.sp,
                lineHeight = 36.sp,
                color = Colors.Text,
                textAlign = TextAlign.Center,
                modifier = Modifier.background(Colors.Fons).padding(horizontal = 10.dp, vertical = 2.dp),
            )
            Text(
                stringResource(R.string.missatge_desbloquejat, missatge.nom),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.background(Colors.Ambre).padding(horizontal = 10.dp, vertical = 4.dp),
                color = Colors.TintaAmbre,
            )
        }
    }
}
