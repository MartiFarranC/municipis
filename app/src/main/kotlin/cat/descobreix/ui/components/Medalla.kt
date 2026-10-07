package cat.descobreix.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import cat.descobreix.joc.dades.Geografia
import cat.descobreix.joc.dades.GeometriaMapa
import cat.descobreix.joc.progressio.Medalla
import cat.descobreix.joc.progressio.NivellMedalla
import cat.descobreix.joc.progressio.TipusMedalla
import cat.descobreix.ui.theme.ChakraPetch

// Medalla clàssica (estil A de disseny/medalles): rodona, penjada d'una cinta amb la senyera, amb la silueta
// de la comarca (o una icona) al mig i el nom en una banda a sota. Es dibuixa en un quadrat de 100 × 100 unitats.

/** Silueta d'una comarca: la unió dels seus municipis, en les coordenades del mapa. */
class Silueta(val cami: Path, val requadre: Rect)

/** Construeix la silueta de cada comarca a partir de la geometria del mapa. És feina pesada: fer-ho fora del fil principal. */
fun siluetesComarques(mapa: GeometriaMapa, geografia: Geografia): Map<String, Silueta> =
    geografia.municipis.indices.groupBy { geografia.municipis[it].comarca }.mapValues { (_, indexs) ->
        val cami = Path()
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE
        var maxY = -Float.MAX_VALUE
        for (i in indexs) {
            val forma = mapa.municipis[i]
            minX = minOf(minX, forma.requadre.minX.toFloat())
            minY = minOf(minY, forma.requadre.minY.toFloat())
            maxX = maxOf(maxX, forma.requadre.maxX.toFloat())
            maxY = maxOf(maxY, forma.requadre.maxY.toFloat())
            for (p in forma.general) for (anell in p.anells) {
                cami.moveTo(anell[0].toFloat(), anell[1].toFloat())
                var j = 2
                while (j < anell.size) {
                    cami.lineTo(anell[j].toFloat(), anell[j + 1].toFloat())
                    j += 2
                }
                cami.close()
            }
        }
        Silueta(cami, Rect(minX, minY, maxX, maxY))
    }

private class Metall(val base: Color, val fosc: Color, val clar: Color, val relleu: Color)

private val Bronze = Metall(Color(0xFFC98A4E), Color(0xFF8A5A2E), Color(0xFFEDBB8A), Color(0xFF5E3A1A))
private val Plata = Metall(Color(0xFFC3CCD6), Color(0xFF7E8A9A), Color(0xFFF2F5F8), Color(0xFF4A5361))
private val Or = Metall(Color(0xFFF2B544), Color(0xFFB8862F), Color(0xFFFFE3A3), Color(0xFF6A4A10))
private val Gris = Metall(Color(0xFF2A313D), Color(0xFF1C2230), Color(0xFF3A4558), Color(0xFF4A5568))
private val GrocSenyera = Color(0xFFF2C230)
private val VermellSenyera = Color(0xFFD7262B)

private fun metallDe(nivell: NivellMedalla?) = when (nivell) {
    NivellMedalla.BRONZE -> Bronze
    NivellMedalla.PLATA -> Plata
    NivellMedalla.OR -> Or
    null -> Gris
}

/**
 * Dibuixa una medalla. Les que encara no s'han aconseguit surten en gris.
 * @param etiqueta el text de la banda (el nom de la comarca, «100 MUNICIPIS»…), ja en majúscules.
 * @param silueta la silueta de la comarca, per a les de comarca (si encara no s'ha carregat, no es dibuixa).
 * @param nivell el nivell que es dibuixa; per defecte, el que té la medalla.
 */
@Composable
fun DibuixMedalla(
    medalla: Medalla,
    etiqueta: String,
    silueta: Silueta?,
    modifier: Modifier = Modifier,
    nivell: NivellMedalla? = medalla.nivell,
) {
    val mesurador = rememberTextMeasurer()
    Canvas(modifier) {
        val s = size.minDimension / 100f
        translate((size.width - 100f * s) / 2f, (size.height - 100f * s) / 2f) {
            scale(s, pivot = Offset.Zero) { dibuixaMedalla(medalla, etiqueta, silueta, metallDe(nivell), nivell == null, mesurador, s) }
        }
    }
}

private fun DrawScope.dibuixaMedalla(
    m: Medalla,
    etiqueta: String,
    silueta: Silueta?,
    metall: Metall,
    gris: Boolean,
    mesurador: TextMeasurer,
    escala: Float,
) {
    if (!gris) cinta()
    drawCircle(metall.fosc, 34f, Offset(50f, 58f))
    drawCircle(metall.base, 30f, Offset(50f, 58f))
    drawCircle(metall.clar.copy(alpha = .7f), 25f, Offset(50f, 58f), style = Stroke(1.4f))
    val tinta = if (gris) metall.clar else metall.relleu
    when (m.tipus) {
        TipusMedalla.COMARCA -> silueta?.let { dibuixaSilueta(it, Offset(50f, 57f), 36f, tinta) }
        TipusMedalla.MUNICIPIS -> text(mesurador, "${m.objectiu}", Offset(50f, 57f), 21f, 44f, tinta, escala)
        TipusMedalla.CAPITALS -> estrella(Offset(50f, 57f), 16f, tinta)
        TipusMedalla.CARTELLS -> cartell(Offset(50f, 57f), tinta)
    }
    val banda = Path().apply {
        moveTo(14f, 86f)
        lineTo(86f, 86f)
        lineTo(80f, 92f)
        lineTo(86f, 98f)
        lineTo(14f, 98f)
        lineTo(20f, 92f)
        close()
    }
    drawPath(banda, if (gris) metall.base else metall.fosc)
    text(mesurador, etiqueta, Offset(50f, 92f), 7.5f, 60f, if (gris) metall.clar else Color.White, escala)
}

/** La cinta de penjar amb les quatre barres vermelles sobre groc. */
private fun DrawScope.cinta() {
    drawPath(
        Path().apply {
            moveTo(36f, 0f)
            lineTo(64f, 0f)
            lineTo(58.96f, 26f)
            lineTo(41.04f, 26f)
            close()
        },
        GrocSenyera,
    )
    for (i in 0 until 4) {
        val x = 36f + 28f * (.14f + i * .2f)
        drawPath(
            Path().apply {
                moveTo(x, 0f)
                lineTo(x + 2.52f, 0f)
                lineTo(x + 2.52f - .84f, 26f)
                lineTo(x - .84f, 26f)
                close()
            },
            VermellSenyera,
        )
    }
}

/** Encabeix la silueta en un quadrat de costat [mida] centrat a [centre]. */
private fun DrawScope.dibuixaSilueta(si: Silueta, centre: Offset, mida: Float, color: Color) {
    val r = si.requadre
    val k = mida / maxOf(r.width, r.height)
    translate(centre.x - r.center.x * k, centre.y - r.center.y * k) {
        scale(k, pivot = Offset.Zero) {
            drawPath(si.cami, color)
            // Una mica de vora del mateix color tapa les juntes entre municipis.
            drawPath(si.cami, color, style = Stroke(1.2f / k))
        }
    }
}

private fun DrawScope.estrella(c: Offset, r: Float, color: Color) {
    val p = Path()
    for (i in 0 until 10) {
        val radi = if (i % 2 == 0) r else r * .45f
        val a = Math.toRadians(-90.0 + i * 36.0)
        val x = c.x + radi * Math.cos(a).toFloat()
        val y = c.y + radi * Math.sin(a).toFloat()
        if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
    }
    p.close()
    drawPath(p, color)
}

/** Un cartell d'entrada de poble: la placa i els dos pals. */
private fun DrawScope.cartell(c: Offset, color: Color) {
    drawRoundRect(
        color,
        topLeft = Offset(c.x - 14f, c.y - 11f),
        size = Size(28f, 15f),
        cornerRadius = CornerRadius(2f),
        style = Stroke(2.6f),
    )
    drawLine(color, Offset(c.x - 7f, c.y + 4f), Offset(c.x - 7f, c.y + 14f), 2.6f)
    drawLine(color, Offset(c.x + 7f, c.y + 4f), Offset(c.x + 7f, c.y + 14f), 2.6f)
}

/** Text centrat a [centre], de [mida] unitats, que s'encongeix si passa de [ampladaMaxima]. */
private fun DrawScope.text(
    mesurador: TextMeasurer,
    text: String,
    centre: Offset,
    mida: Float,
    ampladaMaxima: Float,
    color: Color,
    escala: Float,
) {
    // Es mesura en píxels reals (el text no es pot escalar amb el llenç) i es torna a unitats.
    fun estil(m: Float) = TextStyle(fontFamily = ChakraPetch, fontWeight = FontWeight.Bold, fontSize = (m * escala).toSp(), color = color)
    var resultat = mesurador.measure(text, estil(mida), softWrap = false, maxLines = 1)
    val amplada = resultat.size.width / escala
    if (amplada > ampladaMaxima) resultat = mesurador.measure(text, estil(mida * ampladaMaxima / amplada), softWrap = false, maxLines = 1)
    val w = resultat.size.width / escala
    val h = resultat.size.height / escala
    scale(1f / escala, pivot = Offset.Zero) {
        drawText(resultat, topLeft = Offset((centre.x - w / 2f) * escala, (centre.y - h / 2f) * escala))
    }
}
