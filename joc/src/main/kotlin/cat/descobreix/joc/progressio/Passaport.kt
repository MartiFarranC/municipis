package cat.descobreix.joc.progressio

import cat.descobreix.joc.config.ConfiguracioJoc
import cat.descobreix.joc.model.CodiIne
import kotlin.random.Random

/**
 * Un segell posat al passaport. [x] i [y] són el centre del segell, en fracció de l'amplada i l'alçada de la pàgina;
 * [gir] són graus; [tinta] és l'índex del color de la tinta.
 */
data class SegellPosat(
    val codi: CodiIne,
    val comarca: String,
    val pagina: Int,
    val x: Float,
    val y: Float,
    val gir: Float,
    val tinta: Int,
)

/**
 * Regles del passaport: cada municipi té un segell, que es posa a la pàgina de la seva comarca on l'usuari tria.
 * Quan una pàgina té [ConfiguracioJoc.Passaport.segellsPerPagina] segells, se'n comença una altra.
 */
class Passaport(private val config: ConfiguracioJoc.Passaport) {
    /** La pàgina (començant per 0) on va el segell següent d'una comarca que ja en té [segellsComarca]. */
    fun paginaNova(segellsComarca: Int): Int = segellsComarca / config.segellsPerPagina

    /** Quantes pàgines ocupen els segells d'una comarca (almenys una). */
    fun pagines(segellsComarca: Int): Int = maxOf(1, (segellsComarca + config.segellsPerPagina - 1) / config.segellsPerPagina)

    /**
     * On queda el segell si l'usuari toca a ([x], [y]): el segell sencer ha de cabre dins la pàgina, sota la capçalera.
     * [ampladaSegell] i [alcadaSegell] són la mida del segell en fracció de la pàgina.
     */
    fun limita(x: Float, y: Float, ampladaSegell: Float, alcadaSegell: Float): Pair<Float, Float> {
        val mx = ampladaSegell / 2f + MARGE
        val my = alcadaSegell / 2f + MARGE
        return x.coerceIn(mx, 1f - mx) to y.coerceIn(CAPCALERA + my, 1f - my - MARGE)
    }

    /** Un gir i una tinta a l'atzar, perquè cada segell surti diferent com en un passaport de veritat. */
    fun estil(atzar: Random): Pair<Float, Int> = (atzar.nextFloat() * 2f - 1f) * GIR_MAXIM to atzar.nextInt(TINTES)

    companion object {
        /** Espai de la vora del paper. */
        const val MARGE = .03f

        /** Espai de dalt per al nom de la comarca i el recompte. */
        const val CAPCALERA = .08f
        const val GIR_MAXIM = 10f
        const val TINTES = 5
    }
}
