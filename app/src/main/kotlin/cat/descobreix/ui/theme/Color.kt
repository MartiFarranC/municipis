package cat.descobreix.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * El color secundari de l'app: l'ambre o un dels colors dels sacs. Substitueix l'ambre als botons, als municipis
 * descoberts i a les xifres; els fons i els textos no canvien. És un estat de Compose: en canviar-lo, es torna a pintar.
 */
object ColorSecundari {
    val Ambre = Color(0xFFF2B544)

    var actual by mutableStateOf(Ambre)

    val esAmbre: Boolean get() = actual == Ambre
}

/** Colors de docs/disseny.md: "nit i fanals". */
object Colors {
    val Fons = Color(0xFF0B0E13)
    val Superficie = Color(0xFF151A22)
    val Superficie2 = Color(0xFF1C2330)
    val Linia = Color(0xFF273041)
    val Text = Color(0xFFEEF1F5)
    val TextSecundari = Color(0xFF9AA4B2)

    /** Descobert: el teu municipi d'inici. És el color secundari (l'ambre, si no se n'ha triat cap altre). */
    val Ambre: Color get() = ColorSecundari.actual

    /** Descobert: la resta. */
    val AmbreFosc: Color get() = if (ColorSecundari.esAmbre) Color(0xFFB8862F) else lerp(Ambre, Color.Black, .25f)

    /** Text sobre fons ambre. */
    val TintaAmbre = Color(0xFF1A1305)

    /** Disponible: ratllat ambre fosc. */
    val Disponible1: Color get() = if (ColorSecundari.esAmbre) Color(0xFF2A2618) else lerp(Fons, Ambre, .12f)
    val Disponible2: Color get() = if (ColorSecundari.esAmbre) Color(0xFF4A3E1E) else lerp(Fons, Ambre, .28f)

    /** A la boira. */
    val Boira = Color(0xFF161B23)
    val VoraBoira = Color(0xFF222A36)

    /** Selecció i accions secundàries. */
    val Blau = Color(0xFF5AB8E8)

    val Error = Color(0xFFF08A7A)

    /** Fons de les missions completades. */
    val FonsCompletada: Color get() = if (ColorSecundari.esAmbre) Color(0xFF1F1C12) else lerp(Fons, Ambre, .08f)
}
