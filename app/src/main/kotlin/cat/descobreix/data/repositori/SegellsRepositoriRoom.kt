package cat.descobreix.data.repositori

import cat.descobreix.data.db.BaseDades
import cat.descobreix.data.db.SegellEntity
import cat.descobreix.domain.Segell
import cat.descobreix.joc.progressio.SegellPosat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SegellsRepositoriRoom @Inject constructor(
    db: BaseDades,
    private val rellotge: Rellotge,
) : SegellsRepositori {
    private val dao = db.segells()

    override val segells: Flow<List<Segell>> = dao.tots().map { l -> l.map { it.domini() } }

    override suspend fun segellsAra(): List<Segell> = dao.totsAra().map { it.domini() }

    override suspend fun posa(segell: SegellPosat) {
        check(dao.totsAra().none { it.codiIne == segell.codi }) { "${segell.codi} ja té segell" }
        val ara = rellotge.ara()
        dao.insereix(
            SegellEntity(
                id = uuid(), codiIne = segell.codi, comarca = segell.comarca, pagina = segell.pagina,
                x = segell.x, y = segell.y, gir = segell.gir, tinta = segell.tinta, creatEl = ara, modificatEl = ara,
            ),
        )
    }

    private fun SegellEntity.domini() = Segell(SegellPosat(codiIne, comarca, pagina, x, y, gir, tinta), creatEl)
}
