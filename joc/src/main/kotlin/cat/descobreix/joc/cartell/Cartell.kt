package cat.descobreix.joc.cartell

import java.text.Normalizer
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Un rectangle en píxels, amb els costats esquerre i superior inclosos i els altres exclosos. */
data class Rectangle(val esquerra: Int, val dalt: Int, val dreta: Int, val baix: Int) {
    val amplada: Int get() = dreta - esquerra
    val alcada: Int get() = baix - dalt
}

/** Un requadre en fracció de la vista (0–1), tal com el veu l'usuari a la pantalla. */
data class Requadre(val esquerra: Float, val dalt: Float, val dreta: Float, val baix: Float)

/**
 * La foto del cartell (docs/decisions-pendents.md): l'usuari ajusta un requadre sobre la càmera i només se'n desa la
 * part de dins; després es comprova que el text que s'hi llegeix diu el nom del municipi.
 */
object Cartell {
    /**
     * Passa el [requadre] de la vista a píxels de la foto, ja girada dreta. La vista mostra la foto omplint-la i
     * centrada (com el `FILL_CENTER` de la càmera): el que sobresurt pels costats no es veu.
     */
    fun retall(requadre: Requadre, vistaAmplada: Int, vistaAlcada: Int, fotoAmplada: Int, fotoAlcada: Int): Rectangle {
        val escala = max(vistaAmplada.toFloat() / fotoAmplada, vistaAlcada.toFloat() / fotoAlcada)
        fun x(f: Float) = ((f * vistaAmplada - vistaAmplada / 2f) / escala + fotoAmplada / 2f).roundToInt().coerceIn(0, fotoAmplada)
        fun y(f: Float) = ((f * vistaAlcada - vistaAlcada / 2f) / escala + fotoAlcada / 2f).roundToInt().coerceIn(0, fotoAlcada)
        return Rectangle(x(min(requadre.esquerra, requadre.dreta)), y(min(requadre.dalt, requadre.baix)), x(max(requadre.esquerra, requadre.dreta)), y(max(requadre.dalt, requadre.baix)))
    }

    /** Majúscules, sense accents i només lletres i xifres (la ela geminada i els apòstrofs fora). */
    fun normalitza(text: String): String =
        Normalizer.normalize(text.uppercase(), Normalizer.Form.NFD).filter { it in 'A'..'Z' || it in '0'..'9' }

    /**
     * Si el [text] llegit al cartell diu el [nom] del municipi. La lectura no és perfecta (cartells bruts, lletres
     * que es confonen), així que s'accepta que hi hagi el nom amb alguns errors: un per cada cinc lletres, com a mínim un.
     */
    fun diuElNom(text: String, nom: String): Boolean {
        val llegit = normalitza(text)
        val esperat = normalitza(nom)
        if (llegit.isEmpty() || esperat.isEmpty()) return false
        return distanciaDinsDe(esperat, llegit) <= max(1, esperat.length / 5)
    }

    /** La mínima distància d'edició entre [patro] i qualsevol tros de [text] (Levenshtein per a subcadenes). */
    internal fun distanciaDinsDe(patro: String, text: String): Int {
        var anterior = IntArray(text.length + 1) // comença a qualsevol lloc del text sense cost
        for (i in 1..patro.length) {
            val actual = IntArray(text.length + 1)
            actual[0] = i
            for (j in 1..text.length) {
                val cost = if (patro[i - 1] == text[j - 1]) 0 else 1
                actual[j] = minOf(anterior[j - 1] + cost, anterior[j] + 1, actual[j - 1] + 1)
            }
            anterior = actual
        }
        return anterior.min()
    }
}
