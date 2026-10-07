package cat.descobreix.domain

import cat.descobreix.data.assets.Dades
import cat.descobreix.data.assets.FontDadesJoc
import cat.descobreix.data.repositori.ProgresRepositori
import cat.descobreix.joc.dades.GeometriaMapa
import cat.descobreix.joc.model.CodiIne
import cat.descobreix.joc.model.Missio
import cat.descobreix.joc.progressio.Medalla
import cat.descobreix.joc.regles.PuntsMissio
import cat.descobreix.joc.regles.ResultatDesbloqueig
import cat.descobreix.joc.regles.Ubicacio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/** Medalles que s'acaben d'aconseguir (o de pujar de nivell), amb els punts que han donat. */
data class MedallesNoves(val medalles: List<Medalla>, val punts: Int)

/** Accions del joc: aplica les regles del mòdul `joc` i en desa el resultat. */
@Singleton
class Joc @Inject constructor(
    private val dadesJoc: FontDadesJoc,
    private val repositori: ProgresRepositori,
) {
    private val mutex = Mutex()

    val progres: Flow<Progres> = repositori.progres

    private val _medallesNoves = MutableSharedFlow<MedallesNoves>(extraBufferCapacity = 4)

    /** Cada vegada que una acció fa guanyar medalles. Es mostren amb una celebració. */
    val medallesNoves: SharedFlow<MedallesNoves> = _medallesNoves.asSharedFlow()

    suspend fun dades(): Dades = dadesJoc.obte()

    suspend fun mapa(): GeometriaMapa = dadesJoc.mapa()

    suspend fun iniciaPartida(codi: CodiIne) = mutex.withLock {
        dades().geografia.municipi(codi)
        repositori.iniciaPartida(codi)
    }

    /** Desbloqueja un municipi si les regles ho permeten. Retorna el resultat de l'avaluació. */
    suspend fun desbloqueja(codi: CodiIne): ResultatDesbloqueig = mutex.withLock {
        val resultat = dades().regles.avaluaDesbloqueig(codi, repositori.progresAra().estatJoc())
        if (resultat is ResultatDesbloqueig.Permes) {
            repositori.desbloqueja(codi, resultat.cost)
            actualitzaMedalles()
        }
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
            actualitzaMedalles()
        }
        punts
    }

    /** Suma els punts de les medalles noves i les anuncia. Es crida amb el mutex agafat. */
    private suspend fun actualitzaMedalles() {
        val m = dades().medalles
        val p = repositori.progresAra()
        val medalles = m.calcula(m.dades(p.conjuntDescoberts, p.completades.keys))
        val noves = repositori.atorgaMedalles(m.guanyades(medalles))
        if (noves.isEmpty()) return
        val ids = noves.map { it.id }.toSet()
        _medallesNoves.tryEmit(MedallesNoves(medalles.filter { md -> m.guanyades(md).any { it.id in ids } }, noves.sumOf { it.punts }))
    }
}
