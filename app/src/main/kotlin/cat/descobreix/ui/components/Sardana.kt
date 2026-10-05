package cat.descobreix.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import cat.descobreix.ui.theme.Colors
import cat.descobreix.ui.theme.LocalReduirAnimacions
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Animacions de l'app: una rotllana de sardanistes. Es dibuixen en un quadrat de 100 × 100 unitats.

private const val DANSAIRES = 8
private val Terra = Color(0xFF3A4558)
private val AmbreClar = Color(0xFFFFE3A3)
private val Vermell = Color(0xFFD7262B)

private val posicions = List(DANSAIRES) { k ->
    val a = Math.toRadians(k * 360.0 / DANSAIRES)
    Offset((50 + 30 * cos(a)).toFloat(), (60 + 12 * sin(a)).toFloat())
}

/** Primer els de darrere, perquè els de davant els tapin. */
private val ordre = posicions.indices.sortedBy { posicions[it].y }

/** Els de davant de la rotllana fan més llum que els de darrere. */
private fun colorDe(p: Offset) = if (p.y > 60f) Colors.Ambre else Colors.AmbreFosc

/** Dibuixa en un quadrat de 100 unitats centrat a l'espai disponible. */
@Composable
private fun Escena(modifier: Modifier, dibuix: DrawScope.() -> Unit) {
    Canvas(modifier) {
        val s = size.minDimension / 100f
        translate((size.width - 100f * s) / 2f, (size.height - 100f * s) / 2f) {
            scale(s, pivot = Offset.Zero) { dibuix() }
        }
    }
}

private fun DrawScope.terra() =
    drawOval(Terra, topLeft = Offset(16f, 53f), size = Size(68f, 26f), style = Stroke(1.2f))

private fun DrawScope.cos(p: Offset, color: Color) {
    drawCircle(color, 2.4f, Offset(p.x, p.y - 8f))
    val cos = Path().apply {
        moveTo(p.x - 3f, p.y - 4f)
        relativeLineTo(6f, 0f)
        relativeLineTo(1f, 9f)
        relativeLineTo(-8f, 0f)
        close()
    }
    drawPath(cos, color)
}

private fun DrawScope.bracosAmunt(p: Offset, color: Color) {
    drawLine(color, Offset(p.x - 3f, p.y - 3f), Offset(p.x - 7f, p.y - 9f), 1.6f, StrokeCap.Round)
    drawLine(color, Offset(p.x + 3f, p.y - 3f), Offset(p.x + 7f, p.y - 9f), 1.6f, StrokeCap.Round)
}

/** Les mans agafades entre veïns: avall en els curts i amunt en els llargs. */
private fun DrawScope.mans(amunt: Boolean) {
    val alt = if (amunt) -12f else 5f
    for (k in 0 until DANSAIRES) {
        val a = posicions[k]
        val b = posicions[(k + 1) % DANSAIRES]
        val mig = Offset((a.x + b.x) / 2f, (a.y + b.y) / 2f + alt)
        val corba = Path().apply {
            moveTo(a.x, a.y - 3f)
            quadraticTo(mig.x, mig.y, b.x, b.y - 3f)
        }
        val color = if ((a.y + b.y) / 2f > 60f) Colors.Ambre else Colors.AmbreFosc
        drawPath(corba, color, style = Stroke(1.5f, cap = StrokeCap.Round))
    }
}

/** El pas de la sardana: punteja a banda i banda, i als llargs el moviment és més ample. */
private val pas = listOf(0f to -3f, .25f to 3f, .5f to -3f, .62f to 5f, .75f to -5f, .88f to 5f, 1f to -3f)

private fun desplacament(t: Float): Float {
    val i = pas.indexOfLast { it.first <= t }.coerceIn(0, pas.size - 2)
    val (t0, x0) = pas[i]
    val (t1, x1) = pas[i + 1]
    val f = ((t - t0) / (t1 - t0)).coerceIn(0f, 1f)
    return x0 + (x1 - x0) * (f * f * (3 - 2 * f))
}

/** Animació de càrrega: la rotllana balla al seu lloc. */
@Composable
fun SardanaCarregant(modifier: Modifier = Modifier) {
    val reduir = LocalReduirAnimacions.current
    val transicio = rememberInfiniteTransition(label = "sardana")
    val t by transicio.animateFloat(0f, 1f, infiniteRepeatable(tween(4800, easing = LinearEasing)), label = "pas")
    val bot by transicio.animateFloat(0f, 1f, infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "bot")
    Escena(modifier) {
        val temps = if (reduir) 0f else t
        terra()
        translate(left = desplacament(temps)) {
            mans(amunt = temps >= .5f)
            translate(top = if (reduir) 0f else -1.6f * bot) {
                for (k in ordre) cos(posicions[k], colorDe(posicions[k]))
            }
        }
    }
}

/** Animació de confirmació: els sardanistes aixequen les mans i apareix la marca de fet. Dura [durada] ms. */
@Composable
fun SardanaConfirmacio(modifier: Modifier = Modifier, durada: Int = 1400) {
    val reduir = LocalReduirAnimacions.current
    val progres = remember { Animatable(if (reduir) 1f else 0f) }
    LaunchedEffect(Unit) { progres.animateTo(1f, tween(durada, easing = LinearEasing)) }
    val marca = remember {
        Path().apply {
            moveTo(44f, 20f)
            relativeLineTo(4f, 4f)
            relativeLineTo(8f, -9f)
        }
    }
    val llargada = remember { PathMeasure().apply { setPath(marca, false) }.length }
    Escena(modifier) {
        val p = progres.value
        // Les mans pugen de cop al començament i tornen a lloc.
        val salt = when {
            p < .15f -> p / .15f
            p < .3f -> 1f - (p - .15f) / .15f
            else -> 0f
        }
        terra()
        translate(top = -3f * salt) {
            for (k in ordre) {
                val c = colorDe(posicions[k])
                cos(posicions[k], c)
                bracosAmunt(posicions[k], c)
            }
        }
        val dibuixada = ((p - .35f) / .35f).coerceIn(0f, 1f)
        if (dibuixada > 0f) {
            val tros = Path()
            PathMeasure().apply { setPath(marca, false) }.getSegment(0f, llargada * dibuixada, tros, true)
            drawPath(tros, Colors.Ambre, style = Stroke(3f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}

private class Confeti(val x: Float, val gir: Float, val color: Color)

private val confeti = Random(11).let { r ->
    List(14) { i -> Confeti(8f + r.nextFloat() * 84f, r.nextInt(-200, 201).toFloat(), listOf(Colors.Ambre, Vermell, AmbreClar, Colors.Ambre)[i % 4]) }
}

/** Animació de premi: la rotllana salta de festa i cau confeti. */
@Composable
fun SardanaPremi(modifier: Modifier = Modifier) {
    val reduir = LocalReduirAnimacions.current
    val transicio = rememberInfiniteTransition(label = "premi")
    val t by transicio.animateFloat(0f, 1f, infiniteRepeatable(tween(3200, easing = LinearEasing)), label = "festa")
    Escena(modifier) {
        val segons = t * 3.2f
        for ((n, k) in ordre.withIndex()) {
            // Cada sardanista salta amb un petit retard respecte al del costat.
            val fase = ((segons + n * .1f) / .8f) % 2f
            val f = if (fase < 1f) fase else 2f - fase
            val alt = if (reduir) 0f else -6f * (f * f * (3 - 2 * f))
            translate(top = alt) {
                cos(posicions[k], Colors.Ambre)
                bracosAmunt(posicions[k], Colors.Ambre)
            }
        }
        if (!reduir) {
            clipRect(0f, 0f, 100f, 100f) {
                for ((i, c) in confeti.withIndex()) {
                    val fase = (((segons - i * .06f) % 3.2f) + 3.2f) % 3.2f / 3.2f
                    val alfa = when {
                        fase < .08f -> 0f
                        fase < .12f -> (fase - .08f) / .04f
                        fase < .85f -> 1f
                        else -> 1f - (fase - .85f) / .15f
                    }
                    if (alfa <= 0f) continue
                    val q = ((fase - .08f) / .92f).coerceIn(0f, 1f)
                    val caiguda = 100f * q * q
                    val centre = Offset(c.x + 1.5f, 4.5f + caiguda)
                    rotate(c.gir * q, pivot = centre) {
                        drawRect(c.color.copy(alpha = alfa), topLeft = Offset(c.x, 2f + caiguda), size = Size(3f, 5f))
                    }
                }
            }
        }
    }
}
