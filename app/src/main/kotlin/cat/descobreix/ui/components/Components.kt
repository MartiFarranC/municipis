package cat.descobreix.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.descobreix.R
import cat.descobreix.sacs.ANIMACIONS_CARREGA
import cat.descobreix.sacs.AnimacioCarrega
import cat.descobreix.ui.theme.Colors
import cat.descobreix.ui.theme.LocalAnimacioCarrega
import cat.descobreix.ui.theme.LocalReduirAnimacions

/**
 * La base dels botons (docs/disseny.md): vora de tinta de 2 dp i ombra dura de 4 dp. En prémer, el botó s'enfonsa fins
 * a tocar l'ombra. Si el text canvia (per exemple, «Fet!» després de desbloquejar), el canvi s'anima: el botó contesta.
 */
@Composable
private fun BotoAmbOmbra(
    onClick: () -> Unit,
    fons: Color,
    contingut: Color,
    modifier: Modifier,
    enabled: Boolean,
    ombra: Dp = 4.dp,
    alcadaMinima: Dp = 56.dp,
    content: @Composable RowScope.() -> Unit,
) {
    val interaccio = remember { MutableInteractionSource() }
    val premut by interaccio.collectIsPressedAsState()
    val reduir = LocalReduirAnimacions.current
    val enfonsat by animateDpAsState(if (premut && enabled) ombra else 0.dp, tween(if (reduir) 0 else 70), label = "enfonsat")
    val tinta = Colors.Text
    val vora = if (enabled) tinta else Colors.Linia
    Box(
        modifier
            .fillMaxWidth()
            .padding(end = ombra, bottom = ombra)
            .drawBehind {
                // L'ombra dura: un rectangle de tinta desplaçat, que s'amaga a mesura que el botó s'enfonsa.
                if (enabled) {
                    val d = ombra.toPx()
                    drawRect(tinta, topLeft = Offset(d, d), size = size)
                }
            },
    ) {
        Row(
            Modifier
                .offset(enfonsat, enfonsat)
                .fillMaxWidth()
                .heightIn(min = alcadaMinima)
                .background(if (enabled) fons else Colors.Superficie2)
                .border(2.dp, vora)
                .clickable(interactionSource = interaccio, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompositionLocalProvider(LocalContentColor provides if (enabled) contingut else Colors.TextSecundari) { content() }
        }
    }
}

/** El text d'un botó; si canvia, el vell surt per dalt i el nou entra per baix. */
@Composable
private fun TextQueContesta(text: String, estil: TextStyle) {
    val reduir = LocalReduirAnimacions.current
    AnimatedContent(
        targetState = text,
        transitionSpec = {
            if (reduir) {
                EnterTransition.None togetherWith ExitTransition.None
            } else {
                (slideInVertically(tween(260)) { it } + fadeIn(tween(200))) togetherWith (slideOutVertically(tween(200)) { -it } + fadeOut(tween(160)))
            }
        },
        label = "text del botó",
    ) { t -> Text(t, style = estil.copy(color = LocalContentColor.current)) }
}

@Composable
fun BotoPrincipal(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    carregant: Boolean = false,
) {
    BotoAmbOmbra(onClick, Colors.Ambre, Colors.TintaAmbre, modifier, enabled && !carregant) {
        if (carregant) {
            CircularProgressIndicator(Modifier.size(22.dp), color = Colors.TintaAmbre, strokeWidth = 2.dp)
        } else {
            TextQueContesta(text, MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp))
        }
    }
}

@Composable
fun BotoSecundari(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icona: ImageVector? = null,
    enabled: Boolean = true,
) {
    BotoAmbOmbra(onClick, Colors.Fons, Colors.Text, modifier, enabled, ombra = 3.dp, alcadaMinima = 48.dp) {
        if (icona != null) {
            Icon(icona, contentDescription = null, modifier = Modifier.size(18.dp))
            Box(Modifier.width(8.dp))
        }
        TextQueContesta(text, MaterialTheme.typography.labelLarge)
    }
}

/** Botó d'icona quadrat (48 dp), amb vora de tinta. */
@Composable
fun BotoIcona(
    icona: ImageVector,
    descripcio: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Colors.Text,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(48.dp)
            .background(Colors.Fons)
            .border(2.dp, Colors.Text),
        colors = IconButtonDefaults.iconButtonColors(contentColor = tint),
    ) {
        Icon(icona, contentDescription = descripcio, modifier = Modifier.size(22.dp))
    }
}

@Composable
fun Targeta(
    modifier: Modifier = Modifier,
    fons: Color = Colors.Fons,
    vora: Color = Colors.Text,
    content: @Composable () -> Unit,
) {
    Box(
        modifier
            .background(fons)
            .border(1.5.dp, vora)
            .padding(16.dp),
    ) { content() }
}

@Composable
fun BarraProgres(
    progres: Float,
    modifier: Modifier = Modifier,
    color: Color = Colors.Ambre,
    alcada: Int = 8,
) {
    val p = progres.coerceIn(0f, 1f)
    Box(
        modifier
            .fillMaxWidth()
            .height(alcada.dp)
            .background(Colors.Superficie2)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(p, 0f..1f) },
    ) {
        Box(
            Modifier
                .fillMaxWidth(p)
                .height(alcada.dp)
                .background(color),
        )
    }
}

/** Petita xifra amb etiqueta, per a les estadístiques. */
@Composable
fun Xifra(valor: String, etiqueta: String, modifier: Modifier = Modifier, color: Color = Colors.Text) {
    Column(
        modifier
            .background(Colors.Fons)
            .border(1.5.dp, Colors.Text)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(valor, style = MaterialTheme.typography.headlineSmall.copy(color = color))
        Text(etiqueta, style = MaterialTheme.typography.bodySmall, maxLines = 1)
    }
}

@Composable
fun FilaTitol(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

@Composable
fun Carregant(modifier: Modifier = Modifier) {
    val text = stringResource(R.string.carregant)
    Box(modifier.semantics { contentDescription = text }, contentAlignment = Alignment.Center) {
        AnimacioDeCarrega(Modifier.size(160.dp))
    }
}

/** L'animació de càrrega triada (dels sacs) o, si no se n'ha triat cap, la sardana. */
@Composable
fun AnimacioDeCarrega(modifier: Modifier = Modifier) {
    val triada = LocalAnimacioCarrega.current
    if (triada != null && triada in ANIMACIONS_CARREGA) AnimacioCarrega(triada, modifier) else SardanaCarregant(modifier)
}

/** La pantalla d'obertura: el fanal s'encén i la rotllana balla mentre l'app es prepara. */
@Composable
fun Obertura(modifier: Modifier = Modifier) {
    Column(
        modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Logo(stringResource(R.string.logo_descripcio), Modifier.size(150.dp), encen = true)
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
        AnimacioDeCarrega(Modifier.size(120.dp))
    }
}
