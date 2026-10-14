package cat.descobreix.domain

import cat.descobreix.data.assets.Dades
import cat.descobreix.data.assets.FontDadesJoc
import cat.descobreix.data.repositori.AjuntamentsRepositori
import cat.descobreix.data.repositori.FotosRepositori
import cat.descobreix.data.repositori.ProgresRepositori
import cat.descobreix.data.repositori.SacsRepositori
import cat.descobreix.joc.config.ConfiguracioJoc.Objecte
import cat.descobreix.joc.dades.GeometriaMapa
import cat.descobreix.joc.model.CodiIne
import cat.descobreix.joc.model.Missio
import cat.descobreix.joc.progressio.ContingutSac
import cat.descobreix.joc.progressio.DadesSacs
import cat.descobreix.joc.progressio.Medalla
import cat.descobreix.joc.progressio.Sacs
import cat.descobreix.joc.model.TipusMissio
import cat.descobreix.joc.regles.DisponibilitatOficial
import cat.descobreix.joc.regles.MissioAjuntament
import cat.descobreix.joc.regles.PuntsMissio
import cat.descobreix.joc.regles.ResultatDesbloqueig
import cat.descobreix.joc.regles.Ubicacio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/** Medalles que s'acaben d'aconseguir (o de pujar de nivell), amb els punts que han donat. */
data class MedallesNoves(val medalles: List<Medalla>, val punts: Int)

/** Accions del joc: aplica les regles del mòdul `joc` i en desa el resultat. */
@Singleton
class Joc @Inject constructor(
    private val dadesJoc: FontDadesJoc,
    private val repositori: ProgresRepositori,
    private val sacsRepositori: SacsRepositori,
    private val fotos: FotosRepositori,
    private val ajuntaments: AjuntamentsRepositori = AjuntamentsRepositori.Buit,
) {
    private val mutex = Mutex()

    val progres: Flow<Progres> = repositori.progres

    private val _medallesNoves = MutableSharedFlow<MedallesNoves>(extraBufferCapacity = 4)

    /** Cada vegada que una acció fa guanyar medalles. Es mostren amb una celebració. */
    val medallesNoves: SharedFlow<MedallesNoves> = _medallesNoves.asSharedFlow()

    private val _sacsNous = MutableSharedFlow<List<String>>(extraBufferCapacity = 4)

    /** Cada vegada que es guanyen sacs (els seus orígens). Es mostren tancats perquè l'usuari els obri. */
    val sacsNous: SharedFlow<List<String>> = _sacsNous.asSharedFlow()

    val sacs: Flow<List<Sac>> = sacsRepositori.sacs

    suspend fun dades(): Dades = dadesJoc.obte()

    suspend fun mapa(): GeometriaMapa = dadesJoc.mapa()

    /** Una missió pel seu id: una de les automàtiques o una oficial d'un ajuntament (secció 9.6). */
    suspend fun missio(id: String): Missio? {
        val d = dades()
        if (!id.startsWith(MissioAjuntament.PREFIX)) return d.missions.missio(id)
        val m = ajuntaments.missionsAra().firstOrNull { it.idMissio == id } ?: return null
        return d.ajuntaments.comMissio(m)
    }

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
        if (missio.tipus == TipusMissio.OFICIAL && !oficialDisponible(missio.id)) return@withLock PuntsMissio(0, 0)
        val punts = dades().regles.puntsPerCompletar(missio, progres.completades.keys)
        if (punts.total > 0) {
            repositori.completaMissio(missio.id, missio.municipi, punts.missio, punts.bonus, ubicacio, fotoId)
            actualitzaMedalles()
        }
        punts
    }

    /** Si una missió oficial encara existeix i es pot fer avui (les festes, només els seus dies). */
    private suspend fun oficialDisponible(id: String): Boolean {
        val m = ajuntaments.missionsAra().firstOrNull { it.idMissio == id && !it.retirada } ?: return false
        return dades().ajuntaments.disponibilitat(m, avui()) == DisponibilitatOficial.Disponible
    }

    /** Després de desar una foto que no és d'una missió: la primera dona un sac. */
    suspend fun fotoDesada() = mutex.withLock { actualitzaSacs() }

    /** Dona els sacs que toquen i encara no s'han donat (per exemple, els de quan encara no hi havia sacs). */
    suspend fun revisaSacs() = mutex.withLock { actualitzaSacs() }

    /** Les coses de la col·lecció que es tenen: les de tothom i les que han sortit dels sacs. */
    val colleccio: Flow<List<Objecte>> = sacsRepositori.sacs.map { objectesDe(dades().sacs, it) }

    suspend fun colleccioAra(): List<Objecte> = objectesDe(dades().sacs, sacsRepositori.sacsAra())

    private fun objectesDe(s: Sacs, sacs: List<Sac>): List<Objecte> {
        val sortits = sacs.mapNotNull { it.objecte }.toSet()
        return s.inicials + s.delsSacs.filter { ClauObjecte(it.tipus, it.id) in sortits }
    }

    /**
     * Obre un sac: en surt a l'atzar una cosa que encara no es té o, si ja es té tot, punts.
     * @throws IllegalStateException si el sac no existeix o ja està obert.
     */
    suspend fun obreSac(origen: String): ContingutSac = mutex.withLock {
        val sac = checkNotNull(sacsRepositori.sacsAra().firstOrNull { it.origen == origen }) { "No hi ha cap sac $origen" }
        check(!sac.obert) { "El sac $origen ja està obert" }
        val contingut = dades().sacs.obre(colleccioAra(), Random.Default)
        sacsRepositori.obre(origen, contingut)
        contingut
    }

    /** Suma els punts de les medalles noves i les anuncia. Es crida amb el mutex agafat. */
    private suspend fun actualitzaMedalles() {
        val m = dades().medalles
        val p = repositori.progresAra()
        val medalles = m.calcula(m.dades(p.conjuntDescoberts, p.completades.keys))
        val noves = repositori.atorgaMedalles(m.guanyades(medalles))
        if (noves.isNotEmpty()) {
            val ids = noves.map { it.id }.toSet()
            _medallesNoves.tryEmit(MedallesNoves(medalles.filter { md -> m.guanyades(md).any { it.id in ids } }, noves.sumOf { it.punts }))
        }
        actualitzaSacs()
    }

    companion object {
        /** Avui a Catalunya: les dates de les festes dels ajuntaments són d'aquí. */
        fun avui(): LocalDate = LocalDate.now(ZoneId.of("Europe/Madrid"))
    }

    /** Desa els sacs nous i els anuncia. Es crida amb el mutex agafat. */
    private suspend fun actualitzaSacs() {
        val d = dades()
        val p = repositori.progresAra()
        val m = d.medalles
        val medalles = m.guanyades(m.calcula(m.dades(p.conjuntDescoberts, p.completades.keys)))
        val dadesSacs = DadesSacs(p.descoberts.size, p.completades.size, fotos.totes.first().size, medalles)
        val nous = sacsRepositori.afegeix(d.sacs.guanyats(dadesSacs))
        if (nous.isNotEmpty()) _sacsNous.tryEmit(nous)
    }
}
