package cat.descobreix.data.repositori

import androidx.room.withTransaction
import cat.descobreix.data.db.BaseDades
import cat.descobreix.data.db.MissioCompletadaEntity
import cat.descobreix.data.db.MovimentPuntsEntity
import cat.descobreix.data.db.MunicipiDescobertEntity
import cat.descobreix.domain.MissioCompletada
import cat.descobreix.domain.Progres
import cat.descobreix.joc.model.CodiIne
import cat.descobreix.joc.progressio.MedallaGuanyada
import cat.descobreix.joc.regles.Ubicacio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.UUID
import javax.inject.Inject

class ProgresRepositoriRoom @Inject constructor(
    private val db: BaseDades,
    private val rellotge: Rellotge,
) : ProgresRepositori {
    private val dao = db.progres()

    override val progres: Flow<Progres> =
        combine(dao.descoberts(), dao.completades(), dao.moviments()) { d, c, m -> construeix(d, c, m) }

    override suspend fun progresAra(): Progres = construeix(dao.descobertsAra(), dao.completadesAra(), dao.movimentsAra())

    override suspend fun iniciaPartida(codi: CodiIne) {
        db.withTransaction {
            check(dao.descobertsAra().isEmpty()) { "La partida ja està iniciada" }
            val ara = rellotge.ara()
            dao.insereixDescobert(MunicipiDescobertEntity(uuid(), codi, esInici = true, cost = 0, creatEl = ara, modificatEl = ara))
        }
    }

    override suspend fun desbloqueja(codi: CodiIne, cost: Int) {
        db.withTransaction {
            val p = progresAra()
            check(codi !in p.conjuntDescoberts) { "$codi ja està descobert" }
            check(p.saldo >= cost) { "No hi ha prou punts" }
            val ara = rellotge.ara()
            dao.insereixDescobert(MunicipiDescobertEntity(uuid(), codi, esInici = false, cost = cost, creatEl = ara, modificatEl = ara))
            dao.insereixMoviment(
                MovimentPuntsEntity(uuid(), MovimentPuntsEntity.DESPESA, cost, MovimentPuntsEntity.MOTIU_DESBLOQUEIG, codi, ara, ara),
            )
        }
    }

    override suspend fun completaMissio(missioId: String, codi: CodiIne, punts: Int, bonus: Int, ubicacio: Ubicacio?, fotoId: String?) {
        db.withTransaction {
            check(dao.completadesAra().none { it.missioId == missioId }) { "La missió $missioId ja està completada" }
            val ara = rellotge.ara()
            dao.insereixCompletada(
                MissioCompletadaEntity(
                    id = uuid(), missioId = missioId, codiIne = codi, punts = punts, bonus = bonus,
                    lat = ubicacio?.lat, lon = ubicacio?.lon, precisio = ubicacio?.precisioMetres, fotoId = fotoId,
                    creatEl = ara, modificatEl = ara,
                ),
            )
            dao.insereixMoviment(MovimentPuntsEntity(uuid(), MovimentPuntsEntity.GUANY, punts, MovimentPuntsEntity.MOTIU_MISSIO, missioId, ara, ara))
            if (bonus > 0) {
                dao.insereixMoviment(MovimentPuntsEntity(uuid(), MovimentPuntsEntity.GUANY, bonus, MovimentPuntsEntity.MOTIU_BONUS, codi, ara, ara))
            }
        }
    }

    override suspend fun atorgaMedalles(guanyades: List<MedallaGuanyada>): List<MedallaGuanyada> = db.withTransaction {
        val jaSumades = dao.movimentsAra().filter { it.motiu == MovimentPuntsEntity.MOTIU_MEDALLA }.map { it.referencia }.toSet()
        val noves = guanyades.filter { it.id !in jaSumades }
        val ara = rellotge.ara()
        for (m in noves) {
            dao.insereixMoviment(MovimentPuntsEntity(uuid(), MovimentPuntsEntity.GUANY, m.punts, MovimentPuntsEntity.MOTIU_MEDALLA, m.id, ara, ara))
        }
        noves
    }

    private fun construeix(
        descoberts: List<MunicipiDescobertEntity>,
        completades: List<MissioCompletadaEntity>,
        moviments: List<MovimentPuntsEntity>,
    ) = Progres(
        inici = descoberts.firstOrNull { it.esInici }?.codiIne,
        descoberts = descoberts.map { it.codiIne },
        completades = completades.associate { it.missioId to MissioCompletada(it.missioId, it.codiIne, it.punts, it.bonus, it.creatEl) },
        puntsGuanyats = moviments.filter { it.tipus == MovimentPuntsEntity.GUANY }.sumOf { it.quantitat },
        puntsGastats = moviments.filter { it.tipus == MovimentPuntsEntity.DESPESA }.sumOf { it.quantitat },
    )
}

internal fun uuid(): String = UUID.randomUUID().toString()

/** Hora actual en mil·lisegons. És una interfície perquè els tests en puguin fixar el valor. */
fun interface Rellotge {
    fun ara(): Long
}
