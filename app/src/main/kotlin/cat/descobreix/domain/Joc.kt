package cat.descobreix.domain

import cat.descobreix.data.assets.Dades
import cat.descobreix.data.assets.FontDadesJoc
import cat.descobreix.data.repositori.ProgresRepositori
import cat.descobreix.joc.dades.GeometriaMapa
import cat.descobreix.joc.model.CodiIne
import cat.descobreix.joc.model.Missio
import cat.descobreix.joc.regles.PuntsMissio
import cat.descobreix.joc.regles.ResultatDesbloqueig
import cat.descobreix.joc.regles.Ubicacio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/** Accions del joc: aplica les regles del mòdul `joc` i en desa el resultat. */
@Singleton
class Joc @Inject constructor(
    private val dadesJoc: FontDadesJoc,
    private val repositori: ProgresRepositori,
) {
    private val mutex = Mutex()

    val progres: Flow<Progres> = repositori.progres

    suspend fun dades(): Dades = dadesJoc.obte()

    suspend fun mapa(): GeometriaMapa = dadesJoc.mapa()

    suspend fun iniciaPartida(codi: CodiIne) = mutex.withLock {
        dades().geografia.municipi(codi)
        repositori.iniciaPartida(codi)
    }

    /** Desbloqueja un municipi si les regles ho permeten. Retorna el resultat de l'avaluació. */
    suspend fun desbloqueja(codi: CodiIne): ResultatDesbloqueig = mutex.withLock {
        val resultat = dades().regles.avaluaDesbloqueig(codi, repositori.progresAra().estatJoc())
        if (resultat is ResultatDesbloqueig.Permes) repositori.desbloqueja(codi, resultat.cost)
        resultat
    }

    suspend fun avaluaDesbloqueig(codi: CodiIne): ResultatDesbloqueig =
        dades().regles.avaluaDesbloqueig(codi, repositori.progresAra().estatJoc())

    /**
     * Completa una missió ja validada. Retorna els punts guanyats (zero si ja estava completada).
     * El municipi ha d'estar descobert.
     */
    suspend fun completaMissio(missio: Missio, ubicacio: Ubicacio?, fotoId: String?): PuntsMissio = mutex.withLock {
        val progres = repositori.progresAra()
        check(missio.municipi in progres.conjuntDescoberts) { "El municipi ${missio.municipi} no està desbloquejat" }
        val punts = dades().regles.puntsPerCompletar(missio, progres.completades.keys)
        if (punts.total > 0) {
            repositori.completaMissio(missio.id, missio.municipi, punts.missio, punts.bonus, ubicacio, fotoId)
        }
        punts
    }
}
