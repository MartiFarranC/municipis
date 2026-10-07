package cat.descobreix.sacs

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import cat.descobreix.R
import cat.descobreix.data.repositori.TapesPassaport
import cat.descobreix.joc.config.ConfiguracioJoc.Objecte
import cat.descobreix.joc.config.ConfiguracioJoc.Raresa
import cat.descobreix.joc.config.ConfiguracioJoc.TipusObjecte

/** Una tapa del passaport: el color de fons (i el dibuix, si en té), el daurat de la vora, el del text i si porta la silueta. */
data class Portada(@DrawableRes val dibuix: Int?, val fons: Color, val daurat: Color, val text: Color, val silueta: Boolean)

private val Daurat = Color(0xFFE8C36A)

/** Les tres tapes de sempre i les portades dels sacs. Si l'id no existeix, la granat. */
fun portada(id: String): Portada = when (id) {
    TapesPassaport.BLAU -> Portada(null, Color(0xFF16243F), Daurat, Daurat, true)
    TapesPassaport.APP -> Portada(null, Color(0xFF1C2330), Color(0xFFF2B544), Color(0xFFF2B544), true)
    else -> PORTADES[id] ?: Portada(null, Color(0xFF6E1B22), Daurat, Daurat, true)
}

/** El nom d'una tapa del passaport. */
@Composable
fun nomPortada(id: String): String = when (id) {
    TapesPassaport.GRANAT -> stringResource(R.string.tapa_granat)
    TapesPassaport.BLAU -> stringResource(R.string.tapa_blau)
    TapesPassaport.APP -> stringResource(R.string.tapa_app)
    else -> NOMS_OBJECTES["portada:$id"]?.let { stringResource(it) } ?: id
}

/** El color d'un id de color dels sacs (null si no existeix). */
fun colorSac(id: String): Color? = COLORS[id]

private fun clau(o: Objecte) = when (o.tipus) {
    TipusObjecte.EMOJI -> "emoji"
    TipusObjecte.PORTADA -> "portada"
    TipusObjecte.ANIMACIO -> "animacio"
    TipusObjecte.COLOR -> "color"
} + ":" + o.id

/** El nom d'una cosa de la col·lecció. Als emojis, la frase (la que es veu com a reacció). */
@Composable
fun nomObjecte(o: Objecte): String {
    val res = (if (o.tipus == TipusObjecte.EMOJI) FRASES_EMOJIS[o.id] else null) ?: NOMS_OBJECTES[clau(o)]
    return res?.let { stringResource(it) } ?: o.id
}

/** El dibuix d'un emoji. */
@DrawableRes
fun dibuixEmoji(id: String): Int? = DIBUIXOS_EMOJIS[id]

/** El color de cada raresa: comuna, gris clar; rara, blau; llegendària, or. No canvien amb el color secundari. */
fun colorRaresa(r: Raresa): Color = when (r) {
    Raresa.INICIAL, Raresa.COMUNA -> Color(0xFFC9D1DC)
    Raresa.RARA -> Color(0xFF5AB8E8)
    Raresa.LLEGENDARIA -> Color(0xFFF2B544)
}

@Composable
fun nomRaresa(r: Raresa): String = stringResource(
    when (r) {
        Raresa.INICIAL -> R.string.raresa_inicial
        Raresa.COMUNA -> R.string.raresa_comuna
        Raresa.RARA -> R.string.raresa_rara
        Raresa.LLEGENDARIA -> R.string.raresa_llegendaria
    },
)

@Composable
fun nomTipus(t: TipusObjecte): String = stringResource(
    when (t) {
        TipusObjecte.EMOJI -> R.string.tipus_emojis
        TipusObjecte.PORTADA -> R.string.tipus_portades
        TipusObjecte.ANIMACIO -> R.string.tipus_animacions
        TipusObjecte.COLOR -> R.string.tipus_colors
    },
)
