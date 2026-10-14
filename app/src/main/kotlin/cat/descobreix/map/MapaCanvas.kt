package cat.descobreix.map

import android.graphics.Bitmap
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawTransform
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
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
import cat.descobreix.ui.theme.Archivo
import cat.descobreix.ui.theme.Colors
import cat.descobreix.ui.theme.Tema
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

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
    posicio: PosicioUsuari?,
    camera: CameraMapa,
    animacions: Boolean,
    descripcio: String,
    onToc: (Offset) -> Unit,
    modifier: Modifier = Modifier,
) {
    val ambit = rememberCoroutineScope()
    val mesurador = rememberTextMeasurer(cacheSize = 256)
    val estilEtiqueta = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 0.04.em)
    val textura = remember { texturaNuvols() }
    val pinzellNuvols = remember(textura) { ShaderBrush(ImageShader(textura, TileMode.Repeated, TileMode.Repeated)) }
    val colorNuvols = Colors.Superficie2.let { if (Tema.fosc) lerp(it, Color.White, .12f) else lerp(it, Color.Black, .24f) }
    val obertures = remember { mutableStateMapOf<Int, Float>() }
    // Quan un municipi passa a descobert, els núvols que el tapaven s'obren (1,3 s).
    var descobertsAbans by remember { mutableStateOf<Set<Int>?>(null) }
    LaunchedEffect(estats) {
        val ara = estats.indices.filter { estats[it] == EstatMunicipi.DESCOBERT }.toSet()
        val nous = descobertsAbans?.let { ara - it }.orEmpty()
        descobertsAbans = ara
        if (!animacions) return@LaunchedEffect
        for (i in nous) {
            launch {
                val a = Animatable(0f)
                a.animateTo(1f, tween(1300, easing = FastOutSlowInEasing)) { obertures[i] = value }
                obertures.remove(i)
            }
        }
    }

    Box(modifier) {
        Canvas(
            Modifier
                .matchParentSize()
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
            val visibles = geometria.municipis.indices.filter { i ->
                geometria.municipis[i].requadre.talla(visible.left.toDouble(), visible.top.toDouble(), visible.right.toDouble(), visible.bottom.toDouble())
            }
            val estatDe = { i: Int -> estats.getOrNull(i) ?: EstatMunicipi.BOIRA }
            val transformacio: DrawTransform.() -> Unit = {
                translate(camera.desplacament.x, camera.desplacament.y)
                scale(escala, escala, pivot = Offset.Zero)
            }

            // 1. Els municipis (docs/disseny.md, mapa «Núvols»): els teus, plens del color fort; la resta, en blanc.
            withTransform(transformacio) {
                for (i in visibles) {
                    drawPath(camiActual[i], if (estatDe(i) == EstatMunicipi.DESCOBERT) Colors.Ambre else Colors.Fons)
                }
                for (i in visibles) {
                    val (color, g) = when (estatDe(i)) {
                        EstatMunicipi.DESCOBERT -> Colors.Text to 1.3f
                        EstatMunicipi.DISPONIBLE -> Colors.Text to .9f
                        EstatMunicipi.BOIRA -> Colors.Linia to .8f
                    }
                    drawPath(camiActual[i], color, style = Stroke(width = g * gruix))
                }
            }

            // 2. Els núvols: una capa que tapa el mapa, amb forats als teus municipis i mig forat als veïns.
            //    Es fa en una capa a part perquè els forats (BlendMode.DstOut) només esborrin els núvols.
            drawIntoCanvas { it.saveLayer(Rect(Offset.Zero, size), Paint()) }
            drawRect(Colors.Boira.copy(alpha = if (Tema.fosc) .55f else .72f))
            val mosaic = textura.width.toFloat()
            // Els núvols es mouen una mica més a poc a poc que el mapa: sembla que siguin més a prop.
            val dx = (camera.desplacament.x * .6f) % mosaic - mosaic
            val dy = (camera.desplacament.y * .6f) % mosaic - mosaic
            translate(dx, dy) {
                drawRect(pinzellNuvols, size = Size(size.width + 2 * mosaic, size.height + 2 * mosaic), colorFilter = ColorFilter.tint(colorNuvols, BlendMode.SrcIn))
            }
            withTransform(transformacio) {
                for (i in visibles) {
                    val estat = estatDe(i)
                    if (estat == EstatMunicipi.BOIRA) continue
                    val forca = if (estat == EstatMunicipi.DESCOBERT) 1f else .5f
                    val obrint = obertures[i]
                    val cami = camiActual[i]
                    val dibuixaForat: DrawScope.() -> Unit = {
                        // Vores difuminades: traços amples i transparents al voltant del forat.
                        for ((amplada, alfa) in VORES_FORAT) drawPath(cami, Color.Black.copy(alpha = alfa * forca), style = Stroke(amplada * gruix), blendMode = BlendMode.DstOut)
                        drawPath(cami, Color.Black.copy(alpha = forca), blendMode = BlendMode.DstOut)
                    }
                    if (obrint != null && obrint < 1f) {
                        // Els núvols s'obren des del centre del municipi.
                        val r = geometria.municipis[i].requadre
                        val centre = Offset(((r.minX + r.maxX) / 2).toFloat(), ((r.minY + r.maxY) / 2).toFloat())
                        scale(obrint, obrint, pivot = centre) { dibuixaForat() }
                    } else {
                        dibuixaForat()
                    }
                }
            }
            drawIntoCanvas { it.restore() }

            // 3. La selecció, per sobre dels núvols.
            withTransform(transformacio) {
                if (seleccionat != null && seleccionat in camiActual.indices) {
                    drawPath(camiActual[seleccionat], Colors.Text, style = Stroke(width = 3f * gruix))
                }
            }

            dibuixaEtiquetes(geometria, estats, noms, seleccionat, visibles, camera, mesurador, estilEtiqueta)
        }
        // La personeta va en una capa a part: la seva animació no fa tornar a dibuixar el mapa.
        if (posicio != null) Personeta(posicio, camera, animacions, Modifier.matchParentSize())
    }
}

/**
 * On és l'usuari: un sardanista com els de l'animació de càrrega (ui/components/Sardana.kt), que punteja al seu
 * lloc. És de tinta, perquè es vegi també a sobre dels teus municipis, que són del color fort.
 */
@Composable
private fun Personeta(posicio: PosicioUsuari, camera: CameraMapa, animacions: Boolean, modifier: Modifier) {
    val transicio = rememberInfiniteTransition(label = "personeta")
    val bot by transicio.animateFloat(0f, 1f, infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "bot")
    val pas by transicio.animateFloat(0f, 1f, infiniteRepeatable(tween(2400, easing = LinearEasing)), label = "pas")
    Canvas(modifier) {
        if (!camera.inicialitzada) return@Canvas
        val peus = camera.aPantalla(posicio.x, posicio.y)
        // Només es llegeix l'animació si n'hi ha: si no, la capa no es redibuixa a cada fotograma.
        val t = if (animacions) pas else 0f
        val b = if (animacions) bot else 0f
        val tinta = Colors.Text
        // Unitats del dibuix de la sardana; la figura fa unes 15 unitats d'alt.
        translate(peus.x, peus.y) {
            scale(1.9.dp.toPx(), pivot = Offset.Zero) {
                // Una onada que surt dels peus, cada cop que fa el pas.
                if (animacions) {
                    val r = 3f + 9f * t
                    drawOval(tinta.copy(alpha = .45f * (1f - t)), Offset(-r, -r * .35f), Size(2 * r, r * .7f), style = Stroke(.7f))
                }
                drawOval(tinta.copy(alpha = .25f), Offset(-5f, -1.2f), Size(10f, 2.4f))
                // Punteja: un bot petit i un pas a banda i banda.
                translate(left = sin(2 * PI.toFloat() * t) * 1.2f, top = -5f - 1.6f * b) {
                    drawCircle(tinta, 2.4f, Offset(0f, -8f))
                    val cos = Path().apply {
                        moveTo(-3f, -4f)
                        relativeLineTo(6f, 0f)
                        relativeLineTo(1f, 9f)
                        relativeLineTo(-8f, 0f)
                        close()
                    }
                    drawPath(cos, tinta)
                    drawLine(tinta, Offset(-3f, -3f), Offset(-6f, 3f), 1.6f, StrokeCap.Round)
                    drawLine(tinta, Offset(3f, -3f), Offset(6f, 3f), 1.6f, StrokeCap.Round)
                }
            }
        }
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
) {
    val minim = 56.dp.toPx()
    val marge = 6.dp.toPx()
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
        // Els noms dels pobles que són a la boira no es veuen: és el que s'ha de descobrir.
        if (estat == EstatMunicipi.BOIRA && i != seleccionat) continue
        val llindar = if (estat == EstatMunicipi.BOIRA) minim * 2 else minim
        if (i != seleccionat && amplada < llindar) continue
        val nom = noms.getOrNull(i) ?: continue
        val color = when {
            i == seleccionat -> Colors.Blau
            estat == EstatMunicipi.DESCOBERT -> Colors.AmbreText
            estat == EstatMunicipi.DISPONIBLE -> Colors.Text
            else -> Colors.TextSecundari
        }
        val text = mesurador.measure(nom.uppercase(), estil.copy(color = color))
        val p = camera.aPantalla(m.etiquetaX.toFloat(), m.etiquetaY.toFloat())
        val mida = Size(text.size.width + marge * 2, text.size.height + marge)
        val origen = Offset(p.x - mida.width / 2, p.y - mida.height / 2)
        drawRect(Colors.Fons, origen, mida)
        drawRect(Colors.Text, origen, mida, style = Stroke(1.dp.toPx()))
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
                Colors.Text,
                topLeft = Offset(v.left * f, v.top * f),
                size = Size(v.width * f, v.height * f),
                style = Stroke(1.5.dp.toPx()),
            )
        }
    }
}

/** Vores dels forats als núvols: (amplada en píxels del mapa, opacitat). */
private val VORES_FORAT = listOf(14f to .12f, 9f to .2f, 5f to .3f)

/**
 * La textura dels núvols: soroll de valor suavitzat en quatre octaves, que es pot repetir sense que es noti la
 * costura. Només és l'opacitat (blanc amb alfa); el color el posa el tema.
 */
private fun texturaNuvols(mida: Int = 256, llavor: Int = 9): ImageBitmap {
    fun hash(x: Int, y: Int, o: Int): Float {
        var h = x * 374761393 + y * 668265263 + o * 1442695041 + llavor * 982451653
        h = (h xor (h ushr 13)) * 1274126177
        return ((h xor (h ushr 16)) and 0xFFFF) / 65535f
    }
    fun suau(t: Float) = t * t * (3 - 2 * t)
    fun soroll(x: Float, y: Float, cel: Int, o: Int): Float {
        val gx = x * cel / mida
        val gy = y * cel / mida
        val x0 = gx.toInt()
        val y0 = gy.toInt()
        val fx = suau(gx - x0)
        val fy = suau(gy - y0)
        val v = { a: Int, b: Int -> hash(a.mod(cel), b.mod(cel), o) }
        val dalt = v(x0, y0) + (v(x0 + 1, y0) - v(x0, y0)) * fx
        val baix = v(x0, y0 + 1) + (v(x0 + 1, y0 + 1) - v(x0, y0 + 1)) * fx
        return dalt + (baix - dalt) * fy
    }
    val pixels = IntArray(mida * mida)
    for (y in 0 until mida) for (x in 0 until mida) {
        var n = 0f
        var amp = .5f
        var cel = 4
        for (o in 0 until 4) {
            n += soroll(x.toFloat(), y.toFloat(), cel, o) * amp
            amp /= 2
            cel *= 2
        }
        val alfa = ((n - .35f) / .45f).coerceIn(0f, 1f)
        pixels[y * mida + x] = ((alfa * 235).toInt() shl 24) or 0xFFFFFF
    }
    return Bitmap.createBitmap(pixels, mida, mida, Bitmap.Config.ARGB_8888).asImageBitmap()
}
