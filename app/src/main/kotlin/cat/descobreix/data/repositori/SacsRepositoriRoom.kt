package cat.descobreix.data.repositori

import androidx.room.withTransaction
import cat.descobreix.data.db.BaseDades
import cat.descobreix.data.db.MovimentPuntsEntity
import cat.descobreix.data.db.SacEntity
import cat.descobreix.domain.ClauObjecte
import cat.descobreix.domain.Sac
import cat.descobreix.joc.config.ConfiguracioJoc.TipusObjecte
import cat.descobreix.joc.progressio.ContingutSac
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SacsRepositoriRoom @Inject constructor(
    private val db: BaseDades,
    private val rellotge: Rellotge,
) : SacsRepositori {
    private val dao = db.sacs()

    override val sacs: Flow<List<Sac>> = dao.tots().map { l -> l.map { it.domini() } }

    override suspend fun sacsAra(): List<Sac> = dao.totsAra().map { it.domini() }

    override suspend fun afegeix(origens: List<String>): List<String> = db.withTransaction {
        val ja = dao.totsAra().map { it.origen }.toSet()
        val ara = rellotge.ara()
        origens.filter { it !in ja }.distinct().onEach { dao.insereix(SacEntity(uuid(), it, null, null, null, null, ara, ara)) }
    }

    override suspend fun obre(origen: String, contingut: ContingutSac) = db.withTransaction {
        val sac = checkNotNull(dao.totsAra().firstOrNull { it.origen == origen }) { "No hi ha cap sac $origen" }
        check(sac.obertEl == null) { "El sac $origen ja està obert" }
        val ara = rellotge.ara()
        val obert = when (contingut) {
            is ContingutSac.Nou -> sac.copy(objecteTipus = contingut.objecte.tipus.name, objecteId = contingut.objecte.id)
            is ContingutSac.Punts -> {
                db.progres().insereixMoviment(
                    MovimentPuntsEntity(uuid(), MovimentPuntsEntity.GUANY, contingut.punts, MovimentPuntsEntity.MOTIU_SAC, origen, ara, ara),
                )
                sac.copy(punts = contingut.punts)
            }
        }
        dao.actualitza(obert.copy(obertEl = ara, modificatEl = ara))
    }

    private fun SacEntity.domini(): Sac {
        val objecte = if (objecteTipus != null && objecteId != null) ClauObjecte(TipusObjecte.valueOf(objecteTipus), objecteId) else null
        return Sac(origen, creatEl, obertEl != null, objecte, punts)
    }
}
