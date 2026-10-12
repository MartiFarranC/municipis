package cat.descobreix.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * El color fort de l'app: el groc o un dels colors dels sacs. És l'únic color de l'app; la resta és blanc i negre.
 * És un estat de Compose: en canviar-lo, es torna a pintar.
 */
object ColorSecundari {
    val Groc = Color(0xFFFFD000)

    var actual by mutableStateOf(Groc)

    val esGroc: Boolean get() = actual == Groc
}

/** Si l'app es pinta en fosc. Segueix el mòbil; ho posa DescobreixTheme. */
object Tema {
    var fosc by mutableStateOf(false)
}

/**
 * Colors de docs/disseny.md: blanc i negre, i un sol color fort. Tots canvien amb el tema clar o fosc.
 * Els noms «Ambre» són de l'estètica d'abans: avui volen dir «el color fort».
 */
object Colors {
    private fun c(clar: Long, fosc: Long) = Color(if (Tema.fosc) fosc else clar)

    val Fons: Color get() = c(0xFFFFFFFF, 0xFF0F0F0F)
    val Superficie: Color get() = c(0xFFF5F5F5, 0xFF1A1A1A)
    val Superficie2: Color get() = c(0xFFECECEC, 0xFF242424)
    val Linia: Color get() = c(0xFFE4E4E4, 0xFF2A2A2A)

    /** La tinta: textos, vores i ombres dures. */
    val Text: Color get() = c(0xFF111111, 0xFFF2F2F2)
    val TextSecundari: Color get() = c(0xFF6B6B6B, 0xFF9A9A9A)

    /** El color fort, per omplir: botons principals, els teus municipis, barres. */
    val Ambre: Color get() = ColorSecundari.actual

    /**
     * El color fort quan és text, icona o vora sobre el fons. En fosc es llegeix bé; en clar, el groc no es llegiria
     * sobre blanc, i es fa servir la tinta.
     */
    val AmbreText: Color get() = if (Tema.fosc) Ambre else Text

    /** El color fort, una mica més fosc (municipis descoberts que no són el d'inici). */
    val AmbreFosc: Color get() = if (ColorSecundari.esGroc) Ambre else lerp(Ambre, Color.Black, .15f)

    /** Text sobre el color fort. */
    val TintaAmbre = Color(0xFF111111)

    /** Disponible (veí): fons tènue del color fort. */
    val Disponible1: Color get() = if (Tema.fosc) lerp(Fons, Ambre, .12f) else lerp(Fons, Ambre, .18f)
    val Disponible2: Color get() = if (Tema.fosc) lerp(Fons, Ambre, .28f) else lerp(Fons, Ambre, .45f)

    /** A la boira. */
    val Boira: Color get() = c(0xFFECECEC, 0xFF1E1E1E)
    val VoraBoira: Color get() = c(0xFFDADADA, 0xFF2A2A2A)

    /** Selecció i accions secundàries: la tinta. */
    val Blau: Color get() = Text

    val Error: Color get() = c(0xFFC62D1F, 0xFFF08A7A)

    /** Fons de les missions completades. */
    val FonsCompletada: Color get() = if (Tema.fosc) lerp(Fons, Ambre, .08f) else lerp(Fons, Ambre, .12f)
}
