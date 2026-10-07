package cat.descobreix.data.repositori

import cat.descobreix.data.db.BaseDades
import cat.descobreix.data.db.MissioPropiaEntity
import cat.descobreix.domain.MissioPropia
import cat.descobreix.joc.model.CodiIne
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MissionsPropiesRepositoriRoom @Inject constructor(
    db: BaseDades,
    private val rellotge: Rellotge,
) : MissionsPropiesRepositori {
    private val dao = db.missionsPropies()

    override fun de(codi: CodiIne): Flow<List<MissioPropia>> = dao.de(codi).map { l ->
        l.map { MissioPropia(it.id, it.codiIne, it.titol, it.descripcio, it.completada, it.creatEl) }
    }

    override suspend fun afegeix(codi: CodiIne, titol: String, descripcio: String?) {
        val ara = rellotge.ara()
        dao.insereix(MissioPropiaEntity(uuid(), codi, titol.trim(), descripcio?.trim()?.ifEmpty { null }, false, ara, ara))
    }

    override suspend fun canviaCompletada(id: String, completada: Boolean) {
        val m = dao.perId(id) ?: return
        dao.actualitza(m.copy(completada = completada, modificatEl = rellotge.ara()))
    }

    override suspend fun esborra(id: String) = dao.esborra(id, rellotge.ara())
}
