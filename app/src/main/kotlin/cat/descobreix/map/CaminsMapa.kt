package cat.descobreix.map

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import cat.descobreix.joc.dades.GeometriaMapa
import cat.descobreix.joc.dades.Poligon

/**
 * Els `Path` de tots els municipis, construïts una sola vegada (mai en cada fotograma).
 * N'hi ha dos jocs: el general, per quan es veu tota Catalunya, i el detallat, per al zoom.
 */
class CaminsMapa(val general: List<Path>, val detall: List<Path>) {
    companion object {
        fun construeix(g: GeometriaMapa): CaminsMapa = CaminsMapa(
            general = g.municipis.map { cami(it.general) },
            detall = g.municipis.map { cami(it.detall) },
        )

        private fun cami(poligons: List<Poligon>): Path = Path().apply {
            fillType = PathFillType.EvenOdd
            for (p in poligons) for (anell in p.anells) {
                moveTo(anell[0].toFloat(), anell[1].toFloat())
                var i = 2
                while (i < anell.size) {
                    lineTo(anell[i].toFloat(), anell[i + 1].toFloat())
                    i += 2
                }
                close()
            }
        }
    }
}
