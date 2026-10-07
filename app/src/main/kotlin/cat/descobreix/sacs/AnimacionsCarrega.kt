package cat.descobreix.sacs

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import cat.descobreix.ui.theme.Colors
import cat.descobreix.ui.theme.LocalReduirAnimacions
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Les animacions de càrrega dels sacs (disseny/sacs/gen-extres.py): dibuixos d'una línia en el color secundari, que
// es mouen sols, en un quadrat de 100 × 100 unitats. Els temps i els moviments són els del CSS del disseny.

/** Les animacions de càrrega que hi ha, per id. */
val ANIMACIONS_CARREGA: Set<String> = setOf(
    "bici", "bruixola", "gegants", "ocell", "onades", "punts",
    "bastons", "globus", "segell", "campana", "caragol", "castell", "rotllana", "sol-lluna", "cotxe", "espiga",
)

/** Dibuixa l'animació de càrrega [id]. Si l'usuari ha demanat menys animacions, queda quieta. */
@Composable
fun AnimacioCarrega(id: String, modifier: Modifier = Modifier) {
    val reduir = LocalReduirAnimacions.current
    var ms by remember { mutableLongStateOf(0L) }
    if (!reduir) {
        LaunchedEffect(Unit) {
            val inici = withFrameMillis { it }
            while (true) withFrameMillis { ms = it - inici }
        }
    }
    val a = Colors.Ambre
    val af = Colors.AmbreFosc
    Canvas(modifier) {
        val s = size.minDimension / 100f
        translate((size.width - 100f * s) / 2f, (size.height - 100f * s) / 2f) {
            scale(s, pivot = Offset.Zero) {
                clipRect(0f, 0f, 100f, 100f) { Escena(this, ms, a, af).dibuixa(id) }
            }
        }
    }
}

private val camins = HashMap<String, Path>()

/** Un camí SVG, en les coordenades del disseny. Es fan una sola vegada. */
private fun cami(d: String): Path = camins.getOrPut(d) { PathParser().parsePathString(d).toPath() }

/** Suavitzat com el ease-in-out del CSS. */
private fun suau(x: Float): Float = x * x * (3 - 2 * x)

/** Fracció del cicle (0–1) al temps [ms], per a una animació de [periode] ms que comença amb [retard] ms. */
private fun cicle(ms: Long, periode: Int, retard: Int = 0): Float = (((ms - retard) % periode + periode) % periode).toFloat() / periode

/** Com `alternate`: va de 0 a 1 i torna, cada tram de [periode] ms, suavitzat. */
private fun alterna(ms: Long, periode: Int, retard: Int = 0, suavitza: Boolean = true): Float {
    val x = cicle(ms, periode * 2, retard) * 2f
    val f = if (x < 1f) x else 2f - x
    return if (suavitza) suau(f) else f
}

/** Interpola els fotogrames clau ([parells] de temps 0–1 i valor) al temps [t], suavitzant cada tram. */
private fun claus(t: Float, vararg parells: Pair<Float, Float>, suavitza: Boolean = true): Float {
    val i = parells.indexOfLast { it.first <= t }.coerceIn(0, parells.size - 2)
    val (t0, v0) = parells[i]
    val (t1, v1) = parells[i + 1]
    val f = ((t - t0) / (t1 - t0)).coerceIn(0f, 1f)
    return v0 + (v1 - v0) * (if (suavitza) suau(f) else f)
}

private class Escena(val d: DrawScope, val ms: Long, val a: Color, val af: Color) {
    fun linia(cami: String, gruix: Float = 2.4f, color: Color = a, alfa: Float = 1f) =
        d.drawPath(cami(cami), color, alpha = alfa, style = Stroke(gruix, cap = StrokeCap.Round, join = StrokeJoin.Round))

    fun ple(cami: String, color: Color = a, alfa: Float = 1f) = d.drawPath(cami(cami), color, alpha = alfa)

    fun cercle(x: Float, y: Float, r: Float, color: Color = a, alfa: Float = 1f) = d.drawCircle(color, r, Offset(x, y), alpha = alfa)

    fun anell(x: Float, y: Float, r: Float, gruix: Float, color: Color = a) = d.drawCircle(color, r, Offset(x, y), style = Stroke(gruix))

    fun persona(x: Float, y: Float, s: Float = 1f, color: Color = a, alfa: Float = 1f) {
        cercle(x, y - 7 * s, 2.4f * s, color, alfa)
        d.drawPath(
            Path().apply {
                moveTo(x - 3 * s, y - 3.5f * s)
                relativeLineTo(6 * s, 0f)
                relativeLineTo(1 * s, 9 * s)
                relativeLineTo(-8 * s, 0f)
                close()
            },
            color,
            alpha = alfa,
        )
    }

    fun gira(graus: Float, x: Float, y: Float, bloc: Escena.() -> Unit) = d.rotate(graus, Offset(x, y)) { bloc() }

    fun mou(x: Float, y: Float, bloc: Escena.() -> Unit) = d.translate(x, y) { bloc() }

    fun escala(sx: Float, sy: Float, x: Float, y: Float, bloc: Escena.() -> Unit) = d.scale(sx, sy, Offset(x, y)) { bloc() }

    fun dibuixa(id: String) {
        when (id) {
            "castell" -> castell()
            "bastons" -> bastons()
            "globus" -> globus()
            "segell" -> segell()
            "campana" -> campana()
            "caragol" -> caragol()
            "rotllana" -> rotllana()
            "sol-lluna" -> solLluna()
            "cotxe" -> cotxe()
            "espiga" -> espiga()
            "bici" -> bici()
            "bruixola" -> bruixola()
            "gegants" -> gegants()
            "ocell" -> ocell()
            "onades" -> onades()
            "punts" -> punts()
        }
    }

    /** Castell que es fa: els pisos apareixen de baix a dalt i desapareixen. */
    private fun castell() {
        val pisos = listOf(listOf(-12f, 0f, 12f), listOf(-6f, 6f), listOf(0f))
        for ((i, xs) in pisos.withIndex()) {
            val t = cicle(ms, 2400, i * 300)
            val alfa = claus(t, 0f to 0f, .1f to 0f, .25f to 1f, .75f to 1f, .9f to 0f, 1f to 0f)
            val y = claus(t, 0f to -8f, .1f to -8f, .25f to 0f, 1f to 0f)
            mou(0f, y) { for (dx in xs) persona(50 + dx, 86f - 14 * i, 1f, a, alfa) }
        }
        linia("M20,90h60", 1.4f, af)
    }

    /** Ball de bastons: els dos bastons es creuen i piquen. */
    private fun bastons() {
        val f = alterna(ms, 600)
        gira(-14f + 18f * f, 30f, 76f) { linia("M30,76L58,28", 3.4f) }
        gira(14f - 18f * f, 70f, 76f) { linia("M70,76L42,28", 3.4f) }
        linia("M50,22v-8M42,24l-5,-6M58,24l5,-6", 2f, af, f)
    }

    /** Globus que puja i baixa. */
    private fun globus() {
        mou(0f, 10f - 18f * alterna(ms, 2400)) {
            cercle(50f, 40f, 18f)
            linia("M38,52l8,16M62,52l-8,16", 1.4f, af)
            d.drawRoundRect(af, Offset(44f, 68f), Size(12f, 9f), CornerRadius(2f))
        }
    }

    /** El segell pica: cau de gran i deixa una onada. */
    private fun segell() {
        val t = cicle(ms, 1600)
        // cubic-bezier(.5,0,.75,0): accelera cap al final.
        val q = claus(t, 0f to 0f, .35f to 1f, suavitza = false).let { it * it }
        val alfa = if (t < .35f) q else claus(t, .35f to 1f, .85f to 1f, 1f to 0f)
        val e = 1.8f - .8f * q
        escala(e, e, 50f, 49f) {
            gira(-8f * (1 - q), 50f, 49f) {
                d.drawRoundRect(a, Offset(26f, 34f), Size(48f, 30f), CornerRadius(4f), alpha = alfa, style = Stroke(2.6f))
                linia("M34,49h32M38,56h24", 1.6f, a, alfa)
            }
        }
        val alfaOna = claus(t, 0f to 0f, .34f to 0f, .4f to .8f, .7f to 0f, 1f to 0f)
        val eo = claus(t, 0f to .9f, .34f to .9f, .7f to 1.25f, 1f to 1.25f)
        escala(eo, eo, 50f, 49f) {
            d.drawRoundRect(af, Offset(22f, 30f), Size(56f, 38f), CornerRadius(6f), alpha = alfaOna, style = Stroke(1.6f))
        }
    }

    /** La campana es gronxa i fa ones de so. */
    private fun campana() {
        linia("M50,12v6", 2f)
        gira(-26f + 52f * alterna(ms, 800), 50f, 18f) {
            ple("M36,30c0,-8 28,-8 28,0l2,20c2,10 6,14 12,16H22c6,-2 10,-6 12,-16z")
            cercle(50f, 72f, 4f, af)
        }
        for ((i, so) in listOf("M14,40q-5,10 0,20", "M86,40q5,10 0,20").withIndex()) {
            val t = cicle(ms, 800, i * 400)
            linia(so, 2f, af, claus(t, 0f to 0f, .4f to 1f, 1f to 0f))
        }
    }

    /** A poc a poc: el cargol avança. */
    private fun caragol() {
        mou(-22f + 40f * cicle(ms, 4000), 0f) {
            linia("M24,76h46c4,0 6,-3 6,-6l4,-8")
            anell(48f, 60f, 14f, 2.4f)
            linia("M48,60m-6,0a6,6 0 1 1 6,6", 2f, af)
            linia("M80,62l2,-8M76,62l-2,-8", 1.4f)
        }
        linia("M14,80h72", 1f, af, .4f)
    }

    /** Rotllana de sardana: la rotllana es mou a banda i banda. */
    private fun rotllana() {
        mou(claus(cicle(ms, 6000), 0f to -3f, .5f to 3f, 1f to -3f, suavitza = false), 0f) {
            for (g in 0 until 360 step 45) {
                val r = g * PI / 180
                persona(50 + 28 * cos(r).toFloat(), 58 + 10 * sin(r).toFloat(), .9f, if (sin(r) > 0) a else af)
            }
        }
        linia("M18,74q32,12 64,0", 1f, af, .4f)
    }

    /** Sol i lluna: giren al voltant de l'horitzó. */
    private fun solLluna() {
        gira(360f * cicle(ms, 4000), 50f, 52f) {
            cercle(50f, 22f, 8f)
            for (g in 0 until 360 step 45) {
                val r = g * PI / 180
                val c = cos(r).toFloat()
                val s = sin(r).toFloat()
                d.drawLine(a, Offset(50 + 11 * c, 22 + 11 * s), Offset(50 + 14 * c, 22 + 14 * s), 1.6f, StrokeCap.Round)
            }
            ple("M46,74a9,9 0 1 0 10,10a7,7 0 1 1 -10,-10z", af)
        }
        linia("M10,52h80", 1f, af, .4f)
    }

    /** Carretera i manta: les ratlles passen i el cotxe tremola. */
    private fun cotxe() {
        for ((i, x) in listOf(10f, 40f, 70f).withIndex()) {
            mou(20f - 40f * cicle(ms, 800, -i * 270), 0f) { linia("M$x,82h10", 2f, af) }
        }
        mou(0f, -1.5f * alterna(ms, 300)) {
            ple("M22,64h56v-8l-10,-4l-8,-10H40l-8,10l-10,4z")
            cercle(34f, 66f, 6f, af)
            cercle(66f, 66f, 6f, af)
        }
    }

    /** Espigues que el vent gronxa. */
    private fun espiga() {
        for ((i, x) in listOf(36f, 50f, 64f).withIndex()) {
            val t = cicle(ms, 2000, i * 200)
            gira(claus(t, 0f to -8f, .5f to 8f, 1f to -8f), x, 88f) {
                linia("M$x,88V44", 1.8f, af)
                for (k in 0 until 5) {
                    val gx = x + if (k % 2 == 1) 3f else -3f
                    val gy = 44f + k * 5
                    gira(if (k % 2 == 1) 20f else -20f, gx, gy) { d.drawOval(a, Offset(gx - 2.4f, gy - 4f), Size(4.8f, 8f)) }
                }
            }
        }
    }

    /** Pedalant: les rodes giren. */
    private fun bici() {
        val g = 360f * cicle(ms, 1000)
        for (x in listOf(30f, 72f)) {
            gira(g, x, 64f) {
                anell(x, 64f, 13f, 2.4f)
                linia("M$x,51v26M${x - 13},64h26", 1f, af)
            }
        }
        linia("M30,64l14,-22h18l10,22M44,42l8,22l10,-22M58,36h8", 2.2f)
    }

    /** Brúixola: l'agulla busca el nord. */
    private fun bruixola() {
        anell(50f, 52f, 30f, 2.4f)
        linia("M50,26v4M50,74v4M24,52h4M72,52h4", 2f, af)
        gira(claus(cicle(ms, 2400), 0f to -30f, .5f to 40f, 1f to -30f), 50f, 52f) {
            ple("M50,30l5,22h-10z")
            ple("M50,74l5,-22h-10z", af)
        }
    }

    /** Gegants ballant: es gronxen l'un cap a l'altre. */
    private fun gegants() {
        val g = claus(cicle(ms, 1600), 0f to -7f, .5f to 7f, 1f to -7f)
        gira(g, 34f, 84f) { persona(34f, 80f, 2.6f) }
        gira(-g, 66f, 84f) { persona(66f, 80f, 2.6f, af) }
    }

    /** Ocell volant: bat les ales mentre va i ve. */
    private fun ocell() {
        val t = cicle(ms, 3000)
        mou(claus(t, 0f to -8f, .5f to 8f, 1f to -8f), claus(t, 0f to 6f, .5f to -6f, 1f to 6f)) {
            escala(1f, 1f - 1.6f * alterna(ms, 400), 50f, 50f) { linia("M30,50q10,-12 20,0q10,-12 20,0", 2.6f) }
        }
    }

    /** Onades que passen. */
    private fun onades() {
        for ((i, y) in listOf(42, 54, 66).withIndex()) {
            mou(-20f * cicle(ms, 1400, -i * 400), 0f) {
                linia("M-20,${y}q10,-7 20,0t20,0t20,0t20,0t20,0t20,0t20,0", 2.2f, if (i % 2 == 0) a else af)
            }
        }
    }

    /** Tres punts que salten l'un darrere l'altre. */
    private fun punts() {
        for (i in 0 until 3) {
            val t = cicle(ms, 900, i * 150)
            mou(0f, claus(t, 0f to 0f, .3f to -10f, .6f to 0f, 1f to 0f)) { cercle(30f + 20 * i, 52f, 6f) }
        }
    }
}
