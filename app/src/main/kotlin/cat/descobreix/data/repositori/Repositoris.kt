package cat.descobreix.data.repositori

import cat.descobreix.domain.Foto
import cat.descobreix.domain.MissioPropia
import cat.descobreix.domain.Progres
import cat.descobreix.domain.Segell
import cat.descobreix.joc.model.CodiIne
import cat.descobreix.joc.model.Visibilitat
import cat.descobreix.domain.Sac
import cat.descobreix.joc.progressio.ContingutSac
import cat.descobreix.joc.progressio.MedallaGuanyada
import cat.descobreix.joc.progressio.SegellPosat
import cat.descobreix.joc.regles.Ubicacio
import kotlinx.coroutines.flow.Flow

// L'app accedeix a les dades només a través d'aquestes interfícies, perquè a la versió 2
// s'hi pugui afegir una font remota sense tocar la UI.

interface ProgresRepositori {
    val progres: Flow<Progres>

    suspend fun progresAra(): Progres

    /** Desbloqueja el municipi d'inici. Només es pot fer una vegada. */
    suspend fun iniciaPartida(codi: CodiIne)

    /**
     * Desbloqueja un municipi i en descompta el cost.
     * @throws IllegalStateException si ja està descobert o no hi ha prou saldo.
     */
    suspend fun desbloqueja(codi: CodiIne, cost: Int)

    /**
     * Marca una missió com a completada i en suma els punts (i el bonus, si n'hi ha).
     * @throws IllegalStateException si ja estava completada.
     */
    suspend fun completaMissio(missioId: String, codi: CodiIne, punts: Int, bonus: Int, ubicacio: Ubicacio?, fotoId: String?)

    /**
     * Suma els punts de les medalles guanyades que encara no s'havien sumat (cada una només una vegada).
     * Retorna les que són noves.
     */
    suspend fun atorgaMedalles(guanyades: List<MedallaGuanyada>): List<MedallaGuanyada>
}

interface MissionsPropiesRepositori {
    fun de(codi: CodiIne): Flow<List<MissioPropia>>

    suspend fun afegeix(codi: CodiIne, titol: String, descripcio: String?)

    suspend fun canviaCompletada(id: String, completada: Boolean)

    suspend fun esborra(id: String)
}

interface FotosRepositori {
    val totes: Flow<List<Foto>>

    fun de(codi: CodiIne): Flow<List<Foto>>

    fun foto(id: String): Flow<Foto?>

    /** Desa una foto feta amb la càmera (JPEG). La primera foto d'un municipi en serà la portada. */
    suspend fun desa(codi: CodiIne, jpeg: ByteArray, rotacioGraus: Int, ubicacio: Ubicacio?, missioId: String?): Foto

    suspend fun canviaVisibilitat(id: String, visibilitat: Visibilitat)

    suspend fun fesPortada(id: String)

    suspend fun esborra(id: String)
}

/** Preferències de l'usuari (DataStore). */
interface PreferenciesRepositori {
    /** Última posició del mapa: x, y del centre (unitats del mapa) i escala. */
    val cameraMapa: Flow<CameraMapa?>

    suspend fun desaCameraMapa(camera: CameraMapa)

    /** La tapa del passaport que ha triat l'usuari: una de les tres de sempre o una portada dels sacs (pel seu id). */
    val tapaPassaport: Flow<String>

    suspend fun desaTapaPassaport(tapa: String)

    /** El color secundari de l'app (l'id d'un color dels sacs), o null per a l'ambre de sempre. */
    val colorApp: Flow<String?>

    suspend fun desaColorApp(color: String?)

    /** L'animació de càrrega (l'id d'una dels sacs), o null per a la sardana de sempre. */
    val animacioCarrega: Flow<String?>

    suspend fun desaAnimacioCarrega(animacio: String?)
}

/** Les tres tapes del passaport que hi ha sempre. */
object TapesPassaport {
    const val GRANAT = "granat"
    const val BLAU = "blau"
    const val APP = "app"
    val classiques = listOf(GRANAT, BLAU, APP)
}

/** Els sacs guanyats i el que n'ha sortit. */
interface SacsRepositori {
    val sacs: Flow<List<Sac>>

    suspend fun sacsAra(): List<Sac>

    /** Desa els sacs guanyats que encara no hi eren. Retorna els orígens dels nous. */
    suspend fun afegeix(origens: List<String>): List<String>

    /**
     * Desa el que ha sortit d'un sac; si són punts, els suma.
     * @throws IllegalStateException si el sac no existeix o ja estava obert.
     */
    suspend fun obre(origen: String, contingut: ContingutSac)
}

/** Els segells del passaport, al mòbil. */
interface SegellsRepositori {
    val segells: Flow<List<Segell>>

    suspend fun segellsAra(): List<Segell>

    /**
     * Desa un segell nou.
     * @throws IllegalStateException si el municipi ja té segell.
     */
    suspend fun posa(segell: SegellPosat)
}

data class CameraMapa(val x: Float, val y: Float, val escala: Float)
