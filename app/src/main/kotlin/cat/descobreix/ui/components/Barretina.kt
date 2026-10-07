package cat.descobreix.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import cat.descobreix.ui.theme.Colors

// La barretina dels emojis (disseny/emojis/barretina_nova.py), que és la moneda del joc.
// Els camins són en l'espai del dibuix original; la ratlla i l'interior de la vora són forats, perquè es vegi el fons.
private val Cos = cami("M50,290 L48,222 C30,226 6,214 4,180 C2,140 28,92 70,78 C120,62 200,66 245,90 C284,112 298,160 300,232 L301,260 Z")
private val Vora = cami("M38,271 L312,233 C318,232 321,235 322,241 L325,270 C326,276 323,280 317,281 L45,318 C39,319 36,316 35,310 L32,281 C31,275 33,272 38,271 Z")
private val Ratlla = cami("M188,104 C210,110 232,124 246,150")

private fun cami(d: String): Path = PathParser().parsePathString(d).toPath()

/** La icona de la moneda: una barretina d'un color, encabida en un quadrat de 24 × 24 com la resta d'icones. */
@Composable
fun IconaBarretina(modifier: Modifier = Modifier, color: Color = Colors.Ambre, descripcio: String? = null) {
    val semantica = if (descripcio != null) Modifier.semantics { contentDescription = descripcio } else Modifier
    Canvas(
        modifier
            .aspectRatio(1f)
            .then(semantica)
            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen),
    ) {
        val u = size.minDimension / 24f
        translate(.6f * u, 1.6f * u) {
            scale(.068f * u, Offset.Zero) {
                drawPath(Cos, color)
                drawPath(Ratlla, Color.Black, style = Stroke(16f, cap = StrokeCap.Round), blendMode = BlendMode.Clear)
                drawPath(Vora, Color.Black, blendMode = BlendMode.Clear)
                drawPath(Vora, color, style = Stroke(14f))
            }
        }
    }
}
