package cat.descobreix.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import cat.descobreix.joc.dades.GeometriaMapa
import cat.descobreix.joc.model.EstatMunicipi
import cat.descobreix.ui.theme.ChakraPetch
import cat.descobreix.ui.theme.Colors
import cat.descobreix.ui.theme.Secundari
import kotlinx.coroutines.launch

/** A partir d'aquest zoom es dibuixa la geometria detallada. */
private const val ZOOM_DETALL = 3f

/** Màxim d'etiquetes per fotograma, perquè el mapa no quedi saturat. */
private const val MAX_ETIQUETES = 120

@Composable
fun MapaCanvas(
    geometria: GeometriaMapa,
    camins: CaminsMapa,
    estats: List<EstatMunicipi>,
    noms: List<String>,
    inici: Int?,
    seleccionat: Int?,
    camera: CameraMapa,
    animacions: Boolean,
    descripcio: String,
    onToc: (Offset) -> Unit,
    modifier: Modifier = Modifier,
) {
    val ambit = rememberCoroutineScope()
    val mesurador = rememberTextMeasurer(cacheSize = 256)
    val secundari = Colors.Secundari
    val estilEtiqueta = TextStyle(fontFamily = ChakraPetch, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 0.04.em)

    Canvas(
        modifier
            .onSizeChanged { camera.canviaMida(it.toSize()) }
            .semantics { contentDescription = descripcio }
            .pointerInput(camera) {
                detectTransformGestures { centroide, despl, zoom, _ -> camera.transforma(centroide, despl, zoom) }
            }
            .pointerInput(camera) {
                detectTapGestures(
                    onTap = { onToc(camera.aMapa(it)) },
                    onDoubleTap = { p -> ambit.launch { camera.amplia(p, 2f, animacions) } },
                )
            },
    ) {
        if (!camera.inicialitzada) return@Canvas
        val escala = camera.escala
        val visible = camera.visible()
        val camiActual = if (camera.zoom >= ZOOM_DETALL) camins.detall else camins.general
        val gruix = 1.dp.toPx() / escala
        val periode = 8.dp.toPx() / escala
        val ratllat = Brush.linearGradient(
            0f to Colors.Disponible1, 0.5f to Colors.Disponible1, 0.5f to Colors.Disponible2, 1f to Colors.Disponible2,
            start = Offset.Zero,
            end = Offset(periode, periode),
            tileMode = TileMode.Repeated,
        )
        val visibles = geometria.municipis.indices.filter { i ->
            geometria.municipis[i].requadre.talla(visible.left.toDouble(), visible.top.toDouble(), visible.right.toDouble(), visible.bottom.toDouble())
        }

        withTransform({
            translate(camera.desplacament.x, camera.desplacament.y)
            scale(escala, escala, pivot = Offset.Zero)
        }) {
            for (i in visibles) {
                val cami = camiActual[i]
                when (estats.getOrNull(i) ?: EstatMunicipi.BOIRA) {
                    EstatMunicipi.DESCOBERT -> drawPath(cami, if (i == inici) Colors.Ambre else Colors.AmbreFosc)
                    EstatMunicipi.DISPONIBLE -> drawPath(cami, ratllat)
                    EstatMunicipi.BOIRA -> drawPath(cami, Colors.Boira)
                }
            }
            val vora = Stroke(width = gruix)
            for (i in visibles) {
                val color = when (estats.getOrNull(i) ?: EstatMunicipi.BOIRA) {
                    EstatMunicipi.DESCOBERT -> Colors.Fons.copy(alpha = 0.6f)
                    EstatMunicipi.DISPONIBLE -> Colors.Disponible2
                    EstatMunicipi.BOIRA -> Colors.VoraBoira
                }
                drawPath(camiActual[i], color, style = vora)
            }
            if (seleccionat != null && seleccionat in camiActual.indices) {
                drawPath(camiActual[seleccionat], secundari.copy(alpha = 0.25f))
                drawPath(camiActual[seleccionat], secundari, style = Stroke(width = 2.5f * gruix))
            }
        }

        dibuixaEtiquetes(geometria, estats, noms, seleccionat, visibles, camera, mesurador, estilEtiqueta, secundari)
    }
}

/** Els noms dels municipis només apareixen quan hi caben, segons el zoom. */
private fun DrawScope.dibuixaEtiquetes(
    geometria: GeometriaMapa,
    estats: List<EstatMunicipi>,
    noms: List<String>,
    seleccionat: Int?,
    visibles: List<Int>,
    camera: CameraMapa,
    mesurador: TextMeasurer,
    estil: TextStyle,
    secundari: Color,
) {
    val minim = 56.dp.toPx()
    val marge = 6.dp.toPx()
    val radi = CornerRadius(6.dp.toPx())
    var dibuixades = 0
    // Primer el seleccionat i els descoberts, perquè tinguin prioritat.
    val ordre = visibles.sortedBy {
        when {
            it == seleccionat -> 0
            estats.getOrNull(it) == EstatMunicipi.DESCOBERT -> 1
            estats.getOrNull(it) == EstatMunicipi.DISPONIBLE -> 2
            else -> 3
        }
    }
    for (i in ordre) {
        if (dibuixades >= MAX_ETIQUETES) break
        val m = geometria.municipis[i]
        val amplada = (m.requadre.maxX - m.requadre.minX) * camera.escala
        val estat = estats.getOrNull(i) ?: EstatMunicipi.BOIRA
        val llindar = if (estat == EstatMunicipi.BOIRA) minim * 2 else minim
        if (i != seleccionat && amplada < llindar) continue
        val nom = noms.getOrNull(i) ?: continue
        val color = when {
            i == seleccionat -> secundari
            estat == EstatMunicipi.DESCOBERT -> Colors.Ambre
            estat == EstatMunicipi.DISPONIBLE -> Colors.Text
            else -> Colors.TextSecundari
        }
        val text = mesurador.measure(nom.uppercase(), estil.copy(color = color))
        val p = camera.aPantalla(m.etiquetaX.toFloat(), m.etiquetaY.toFloat())
        val mida = Size(text.size.width + marge * 2, text.size.height + marge)
        val origen = Offset(p.x - mida.width / 2, p.y - mida.height / 2)
        drawRoundRect(Colors.Fons.copy(alpha = 0.85f), origen, mida, radi)
        drawRoundRect(color.copy(alpha = 0.7f), origen, mida, radi, style = Stroke(1.dp.toPx()))
        drawText(text, topLeft = Offset(origen.x + marge, origen.y + marge / 2))
        dibuixades++
    }
}

/** Minimapa de tota Catalunya amb la zona visible marcada. Tocar-lo hi mou el mapa. */
@Composable
fun Minimapa(
    camins: CaminsMapa,
    geometria: GeometriaMapa,
    estats: List<EstatMunicipi>,
    camera: CameraMapa,
    descripcio: String,
    modifier: Modifier = Modifier,
) {
    val factor = { mida: Size -> minOf(mida.width / geometria.amplada, mida.height / geometria.alcada) }
    // Dos Canvas: el de sota (els municipis) no es redibuixa quan es mou la càmera.
    Box(modifier.semantics { contentDescription = descripcio }) {
        Canvas(Modifier.matchParentSize()) {
            val f = factor(size)
            withTransform({ scale(f, f, pivot = Offset.Zero) }) {
                for (i in camins.general.indices) {
                    val color = when (estats.getOrNull(i) ?: EstatMunicipi.BOIRA) {
                        EstatMunicipi.DESCOBERT -> Colors.Ambre
                        EstatMunicipi.DISPONIBLE -> Colors.Disponible2
                        EstatMunicipi.BOIRA -> Colors.Superficie2
                    }
                    drawPath(camins.general[i], color)
                }
            }
        }
        Canvas(
            Modifier
                .matchParentSize()
                .pointerInput(camera) {
                    detectTapGestures { p ->
                        val f = factor(size.toSize())
                        camera.centra(p.x / f, p.y / f)
                    }
                },
        ) {
            val f = factor(size)
            val v = camera.visible()
            drawRect(
                Color.White,
                topLeft = Offset(v.left * f, v.top * f),
                size = Size(v.width * f, v.height * f),
                style = Stroke(1.5.dp.toPx()),
            )
        }
    }
}
