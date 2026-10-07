package cat.descobreix.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.descobreix.R
import cat.descobreix.ui.components.BotoPrincipal
import cat.descobreix.ui.components.IconaBarretina
import cat.descobreix.ui.components.SardanaConfirmacio
import cat.descobreix.ui.components.SardanaPremi
import cat.descobreix.ui.theme.ChakraPetch
import cat.descobreix.ui.theme.Colors
import cat.descobreix.ui.theme.LocalReduirAnimacions
import kotlinx.coroutines.delay

/** Si el missatge és una bona notícia que es celebra amb la sardana (i no amb una notificació breu). */
val Missatge.esCelebracio: Boolean
    get() = when (this) {
        is Missatge.PuntsGuanyats -> punts + bonus > 0
        is Missatge.FotoDesada -> punts + bonus > 0
        is Missatge.Desbloquejat -> true
        else -> false
    }

private val Missatge.puntsCelebrats: Int
    get() = when (this) {
        is Missatge.PuntsGuanyats -> punts + bonus
        is Missatge.FotoDesada -> punts + bonus
        else -> 0
    }

/**
 * Celebra una bona notícia a pantalla completa: primer la confirmació (la rotllana aixeca les mans)
 * i després el premi (salta, cau confeti i els punts compten cap amunt).
 */
@Composable
fun Celebracio(missatge: Missatge, onTanca: () -> Unit) {
    val reduir = LocalReduirAnimacions.current
    var premi by remember(missatge) { mutableStateOf(reduir) }
    LaunchedEffect(missatge) {
        if (!premi) {
            delay(1600)
            premi = true
        }
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(Colors.Fons.copy(alpha = .92f))
            // Absorbeix els tocs perquè no arribin a la pantalla de sota.
            .clickable(remember { MutableInteractionSource() }, indication = null, onClick = {}),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = premi,
            transitionSpec = { (fadeIn(tween(300)) + scaleIn(tween(300), initialScale = .9f)) togetherWith fadeOut(tween(200)) },
            label = "celebracio",
        ) { esPremi ->
            if (esPremi) Premi(missatge, onTanca) else Confirmacio(missatge)
        }
    }
}

@Composable
private fun Confirmacio(missatge: Missatge) {
    Column(
        Modifier
            .clip(RoundedCornerShape(22.dp))
            .background(Colors.Superficie)
            .border(1.dp, Colors.Linia, RoundedCornerShape(22.dp))
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SardanaConfirmacio(Modifier.size(150.dp))
        Text(
            stringResource(if (missatge is Missatge.Desbloquejat) R.string.celebracio_desbloquejat else R.string.celebracio_missio),
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Composable
private fun Premi(missatge: Missatge, onTanca: () -> Unit) {
    val reduir = LocalReduirAnimacions.current
    val total = missatge.puntsCelebrats
    val compte = remember(missatge) { Animatable(if (reduir) total.toFloat() else 0f) }
    LaunchedEffect(missatge) { compte.animateTo(total.toFloat(), tween(1000)) }
    Column(
        Modifier
            .systemBarsPadding()
            .padding(24.dp)
            .widthIn(max = 360.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SardanaPremi(Modifier.size(220.dp))
        if (total > 0) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    stringResource(R.string.celebracio_punts, compte.value.toInt()),
                    fontFamily = ChakraPetch,
                    fontWeight = FontWeight.Bold,
                    fontSize = 46.sp,
                    color = Colors.Ambre,
                )
                IconaBarretina(Modifier.size(44.dp))
            }
        }
        Text(textDe(missatge), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        BotoPrincipal(stringResource(R.string.continua), onTanca, Modifier.padding(top = 12.dp))
    }
}
