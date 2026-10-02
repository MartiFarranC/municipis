package cat.descobreix.map

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Posició i zoom del mapa. Un punt del mapa (x, y) es dibuixa a la pantalla a
 * (x · escala + desplaçament.x, y · escala + desplaçament.y).
 */
@Stable
class CameraMapa(private val ampladaMapa: Float, private val alcadaMapa: Float) {
    var desplacament by mutableStateOf(Offset.Zero)
        private set
    var escala by mutableFloatStateOf(1f)
        private set
    var mida by mutableStateOf(Size.Zero)
        private set
    var inicialitzada by mutableStateOf(false)
        private set

    /** L'escala que fa que tota Catalunya hi càpiga. */
    val escalaMinima: Float
        get() = if (mida == Size.Zero) 1f else minOf(mida.width / ampladaMapa, mida.height / alcadaMapa) * 0.9f

    val escalaMaxima: Float get() = escalaMinima * ZOOM_MAXIM

    /** Quant s'ha ampliat respecte de la vista de tota Catalunya (1 = tota Catalunya). */
    val zoom: Float get() = escala / escalaMinima

    fun canviaMida(nova: Size) {
        if (nova == mida) return
        val centre = if (inicialitzada) aMapa(Offset(mida.width / 2, mida.height / 2)) else null
        mida = nova
        if (centre != null) centra(centre.x, centre.y, escala)
    }

    fun inicialitza(x: Float, y: Float, zoom: Float) {
        inicialitzada = true
        centra(x, y, escalaMinima * zoom)
    }

    fun aMapa(p: Offset): Offset = (p - desplacament) / escala

    fun aPantalla(x: Float, y: Float): Offset = Offset(x * escala + desplacament.x, y * escala + desplacament.y)

    /** Rectangle del mapa que es veu a la pantalla, en coordenades del mapa. */
    fun visible(): Rect {
        val a = aMapa(Offset.Zero)
        val b = aMapa(Offset(mida.width, mida.height))
        return Rect(a, b)
    }

    fun centreMapa(): Offset = aMapa(Offset(mida.width / 2, mida.height / 2))

    fun centra(x: Float, y: Float, novaEscala: Float = escala) {
        escala = novaEscala.coerceIn(escalaMinima, escalaMaxima)
        desplacament = Offset(mida.width / 2 - x * escala, mida.height / 2 - y * escala)
        limita()
    }

    fun transforma(centroide: Offset, despl: Offset, factor: Float) {
        val nova = (escala * factor).coerceIn(escalaMinima, escalaMaxima)
        val f = nova / escala
        desplacament = (desplacament - centroide) * f + centroide + despl
        escala = nova
        limita()
    }

    /** Doble toc: amplia al voltant del punt, amb animació si no s'han desactivat. */
    suspend fun amplia(punt: Offset, factor: Float, animat: Boolean) {
        if (!animat) {
            transforma(punt, Offset.Zero, factor)
            return
        }
        val anim = Animatable(1f)
        var anterior = 1f
        coroutineScope {
            launch {
                anim.animateTo(factor, tween(DURADA_ANIMACIO_MS)) {
                    transforma(punt, Offset.Zero, value / anterior)
                    anterior = value
                }
            }
        }
    }

    /** Que una part del mapa sempre quedi a la pantalla. */
    private fun limita() {
        val marge = minOf(mida.width, mida.height) * 0.5f
        val minX = marge - ampladaMapa * escala
        val maxX = mida.width - marge
        val minY = marge - alcadaMapa * escala
        val maxY = mida.height - marge
        desplacament = Offset(desplacament.x.coerceIn(minOf(minX, maxX), maxOf(minX, maxX)), desplacament.y.coerceIn(minOf(minY, maxY), maxOf(minY, maxY)))
    }

    companion object {
        const val ZOOM_MAXIM = 60f
        private const val DURADA_ANIMACIO_MS = 250
    }
}
