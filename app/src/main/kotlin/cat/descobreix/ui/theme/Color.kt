package cat.descobreix.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/** Colors de docs/disseny.md: "nit i fanals". */
object Colors {
    val Fons = Color(0xFF0B0E13)
    val Superficie = Color(0xFF151A22)
    val Superficie2 = Color(0xFF1C2330)
    val Linia = Color(0xFF273041)
    val Text = Color(0xFFEEF1F5)
    val TextSecundari = Color(0xFF9AA4B2)

    /** Descobert: el teu municipi d'inici. */
    val Ambre = Color(0xFFF2B544)

    /** Descobert: la resta. */
    val AmbreFosc = Color(0xFFB8862F)

    /** Text sobre fons ambre. */
    val TintaAmbre = Color(0xFF1A1305)

    /** Disponible: ratllat ambre fosc. */
    val Disponible1 = Color(0xFF2A2618)
    val Disponible2 = Color(0xFF4A3E1E)

    /** A la boira. */
    val Boira = Color(0xFF161B23)
    val VoraBoira = Color(0xFF222A36)

    val Error = Color(0xFFF08A7A)

    /** Fons de les missions completades. */
    val FonsCompletada = Color(0xFF1F1C12)
}

/**
 * Color de la selecció i de les accions secundàries, que l'usuari tria al perfil. Tots tenen un
 * contrast de 4,5:1 com a mínim sobre els fons de l'app i es distingeixen de l'ambre del territori
 * descobert i del color d'error (ho comproven els tests).
 */
enum class ColorSecundari(val color: Color) {
    BLAU(Color(0xFF5AB8E8)),
    TURQUESA(Color(0xFF3ECFBF)),
    VERD(Color(0xFF6CD68A)),
    LILA(Color(0xFFA99BF5)),
    ROSA(Color(0xFFF590C0)),
    ;

    companion object {
        val PER_DEFECTE = BLAU

        fun perNom(nom: String?): ColorSecundari = entries.firstOrNull { it.name == nom } ?: PER_DEFECTE
    }
}

val LocalColorSecundari = compositionLocalOf { ColorSecundari.PER_DEFECTE.color }

/** Selecció i accions secundàries: el color que ha triat l'usuari. */
val Colors.Secundari: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalColorSecundari.current
