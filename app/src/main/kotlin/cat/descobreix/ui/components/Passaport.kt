package cat.descobreix.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.descobreix.data.repositori.AjuntamentsRepositoriRoom
import cat.descobreix.joc.dades.GeometriaMapa
import cat.descobreix.joc.model.CodiIne
import cat.descobreix.joc.progressio.SegellPosat
import cat.descobreix.sacs.portada
import cat.descobreix.ui.theme.ChakraPetch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// El passaport (docs/decisions-pendents.md): pàgines de paper, una (o més) per comarca, on l'usuari
// posa el segell rectangular de cada municipi on vol. Els dibuixos segueixen el prototip de disseny/passaport.

object ColorsPassaport {
    val Paper = Color(0xFFF3EBD8)
    val VoraPaper = Color(0xFFD8CCB0)
    val TextPaper = Color(0xFF4A3E28)
    val TextSuau = Color(0xFF8A7A5A)

    /** Les tintes dels segells; cada segell en fa servir una (l'índex es desa amb el segell). */
    val Tintes = listOf(Color(0xFF2F5FA8), Color(0xFFB0303A), Color(0xFF2E7D4F), Color(0xFF6A3FA0), Color(0xFF1F7A8C))

    fun tinta(index: Int): Color = Tintes[index.mod(Tintes.size)]
}

/** Amplada del segell respecte de la pàgina, i la seva proporció (alçada / amplada). */
const val AMPLADA_SEGELL = .34f
const val PROPORCIO_SEGELL = .6f

/** Proporció de la pàgina del passaport (amplada / alçada). */
const val PROPORCIO_PAGINA = .72f

/** La silueta d'un municipi, en les coordenades del mapa. */
fun siluetaMunicipi(mapa: GeometriaMapa, index: Int): Silueta {
    val forma = mapa.municipis[index]
    val cami = Path()
    for (p in forma.detall) for (anell in p.anells) {
        cami.moveTo(anell[0].toFloat(), anell[1].toFloat())
        var j = 2
        while (j < anell.size) {
            cami.lineTo(anell[j].toFloat(), anell[j + 1].toFloat())
            j += 2
        }
        cami.close()
    }
    val r = forma.requadre
    return Silueta(cami, Rect(r.minX.toFloat(), r.minY.toFloat(), r.maxX.toFloat(), r.maxY.toFloat()))
}

/** Un segell ja posat, amb el que cal per dibuixar-lo. */
data class SegellDibuix(val posat: SegellPosat, val nom: String, val data: String, val silueta: Silueta?)

/**
 * El segell rectangular: doble vora, la silueta del municipi, el nom i la data, tot del color de la tinta.
 * Si l'ajuntament hi ha posat un dibuix propi ([propi], secció 9.6 dels requisits), va en lloc de la silueta.
 */
@Composable
fun DibuixSegell(nom: String, data: String, silueta: Silueta?, tinta: Color, modifier: Modifier = Modifier, propi: ImageBitmap? = null) {
    val mesurador = rememberTextMeasurer()
    Canvas(modifier.aspectRatio(1f / PROPORCIO_SEGELL)) {
        val s = size.width / 100f
        scale(s, pivot = Offset.Zero) {
            drawRoundRect(tinta, Offset(3f, 3f), Size(94f, 54f), CornerRadius(5f), style = Stroke(3f))
            drawRoundRect(tinta, Offset(8f, 8f), Size(84f, 44f), CornerRadius(3f), style = Stroke(1f))
            if (propi != null) {
                drawImage(propi, dstOffset = IntOffset(7, 17), dstSize = IntSize(26, 26), colorFilter = ColorFilter.tint(tinta))
            } else {
                silueta?.let { encabeix(it, Offset(20f, 30f), 26f, tinta) }
            }
            textSegell(mesurador, nom.uppercase(), Offset(64f, 26f), 8f, 50f, tinta, s)
            textSegell(mesurador, data, Offset(64f, 39f), 7f, 50f, tinta, s)
        }
    }
}

private fun DrawScope.encabeix(si: Silueta, centre: Offset, mida: Float, color: Color) {
    val r = si.requadre
    val k = mida / maxOf(r.width, r.height)
    translate(centre.x - r.center.x * k, centre.y - r.center.y * k) {
        scale(k, pivot = Offset.Zero) {
            drawPath(si.cami, color)
            drawPath(si.cami, color, style = Stroke(.8f / k))
        }
    }
}

private fun DrawScope.textSegell(mesurador: TextMeasurer, text: String, centre: Offset, mida: Float, ampladaMaxima: Float, color: Color, escala: Float) {
    fun estil(m: Float) = TextStyle(fontFamily = ChakraPetch, fontWeight = FontWeight.Bold, fontSize = (m * escala).toSp(), color = color)
    var r = mesurador.measure(text, estil(mida), softWrap = false, maxLines = 1)
    val amplada = r.size.width / escala
    if (amplada > ampladaMaxima) r = mesurador.measure(text, estil(mida * ampladaMaxima / amplada), softWrap = false, maxLines = 1)
    val w = r.size.width / escala
    val h = r.size.height / escala
    scale(1f / escala, pivot = Offset.Zero) {
        drawText(r, topLeft = Offset((centre.x - w / 2f) * escala, (centre.y - h / 2f) * escala))
    }
}

/**
 * Una pàgina del passaport amb els seus segells. [contingut] es dibuixa a sobre (el segell que s'està posant,
 * les esquitxades…) i rep l'amplada i l'alçada de la pàgina.
 */
@Composable
fun PaginaPassaport(
    comarca: String,
    recompte: String,
    numero: String,
    siluetaComarca: Silueta?,
    segells: List<SegellDibuix>,
    modifier: Modifier = Modifier,
    contingut: @Composable (amplada: Dp, alcada: Dp) -> Unit = { _, _ -> },
) {
    val forma = RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp, topEnd = 14.dp, bottomEnd = 14.dp)
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .aspectRatio(PROPORCIO_PAGINA)
            .clip(forma)
            .background(ColorsPassaport.Paper),
    ) {
        val amplada = maxWidth
        val alcada = maxHeight
        Box(
            Modifier
                .fillMaxSize()
                .padding(8.dp)
                .border(1.dp, ColorsPassaport.VoraPaper, RoundedCornerShape(4.dp)),
        )
        siluetaComarca?.let { si ->
            Canvas(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = amplada * .08f, vertical = alcada * .12f),
            ) {
                encabeix(si, Offset(size.width / 2f, size.height / 2f), minOf(size.width, size.height), ColorsPassaport.TextPaper.copy(alpha = .07f))
            }
        }
        Column(Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
            Box(Modifier.fillMaxWidth()) {
                Text(comarca.uppercase(), fontFamily = ChakraPetch, fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 1.6.sp, color = ColorsPassaport.TextPaper)
                Text(recompte, fontFamily = ChakraPetch, fontSize = 13.sp, color = ColorsPassaport.TextSuau, modifier = Modifier.align(Alignment.CenterEnd))
            }
        }
        Text(
            numero,
            fontFamily = ChakraPetch,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            color = ColorsPassaport.TextSuau,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
        )
        for (s in segells) SegellALaPagina(s, amplada, alcada)
        contingut(amplada, alcada)
    }
}

/** El dibuix propi del segell d'un municipi, si el seu ajuntament n'ha posat un i ja s'ha baixat. */
@Composable
fun rememberSegellPropi(codi: CodiIne): ImageBitmap? {
    val fitxer = AjuntamentsRepositoriRoom.fitxerSegell(LocalContext.current, codi)
    val imatge by produceState<ImageBitmap?>(null, fitxer.path, fitxer.lastModified()) {
        value = withContext(Dispatchers.IO) { if (fitxer.exists()) BitmapFactory.decodeFile(fitxer.path)?.asImageBitmap() else null }
    }
    return imatge
}

/** Col·loca un segell al seu lloc de la pàgina, girat. */
@Composable
fun SegellALaPagina(s: SegellDibuix, amplada: Dp, alcada: Dp, modifier: Modifier = Modifier) {
    val w = amplada * AMPLADA_SEGELL
    val h = w * PROPORCIO_SEGELL
    DibuixSegell(
        s.nom,
        s.data,
        s.silueta,
        ColorsPassaport.tinta(s.posat.tinta),
        propi = rememberSegellPropi(s.posat.codi),
        modifier = modifier
            .size(w, h)
            .offset(amplada * s.posat.x - w / 2, alcada * s.posat.y - h / 2)
            .graphicsLayer {
                rotationZ = s.posat.gir
                alpha = .9f
            },
    )
}

/** La tapa del passaport: la triada (el color o el dibuix) i la silueta de Catalunya en daurat. */
@Composable
fun TapaDelPassaport(tapa: String, siluetaCatalunya: List<Silueta>, titol: String, subtitol: String, modifier: Modifier = Modifier) {
    val p = portada(tapa)
    val daurat = p.daurat
    val forma = RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp, topEnd = 14.dp, bottomEnd = 14.dp)
    Box(
        modifier
            .aspectRatio(PROPORCIO_PAGINA)
            .clip(forma)
            .background(p.fons),
        contentAlignment = Alignment.Center,
    ) {
        p.dibuix?.let { Image(painterResource(it), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
        Box(
            Modifier
                .fillMaxSize()
                .padding(10.dp)
                .border(1.dp, daurat.copy(alpha = .6f), RoundedCornerShape(6.dp)),
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(titol, fontFamily = ChakraPetch, fontWeight = FontWeight.Bold, fontSize = 22.sp, letterSpacing = 4.sp, color = p.text)
            if (p.silueta && siluetaCatalunya.isNotEmpty()) {
                Canvas(
                    Modifier
                        .fillMaxWidth(.62f)
                        .aspectRatio(1f)
                        .padding(vertical = 12.dp),
                ) {
                    val r = siluetaCatalunya.map { it.requadre }.reduce { a, b -> Rect(minOf(a.left, b.left), minOf(a.top, b.top), maxOf(a.right, b.right), maxOf(a.bottom, b.bottom)) }
                    val k = minOf(size.width / r.width, size.height / r.height)
                    translate(size.width / 2f - r.center.x * k, size.height / 2f - r.center.y * k) {
                        scale(k, pivot = Offset.Zero) {
                            for (si in siluetaCatalunya) {
                                drawPath(si.cami, daurat)
                                drawPath(si.cami, daurat, style = Stroke(1.2f / k))
                            }
                        }
                    }
                }
            }
            if (!p.silueta) Spacer(Modifier.fillMaxWidth(.62f).aspectRatio(1f))
            Text(subtitol, fontFamily = ChakraPetch, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.6.sp, color = p.text)
        }
    }
}
