package cat.descobreix.data.repositori

import cat.descobreix.domain.Foto
import cat.descobreix.domain.MissioPropia
import cat.descobreix.domain.Progres
import cat.descobreix.domain.Segell
import cat.descobreix.joc.model.CodiIne
import cat.descobreix.joc.model.Visibilitat
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

    /** El color de la tapa del passaport que ha triat l'usuari. */
    val tapaPassaport: Flow<TapaPassaport>

    suspend fun desaTapaPassaport(tapa: TapaPassaport)
}

/** Els tres colors que es poden triar per a la tapa del passaport. */
enum class TapaPassaport { GRANAT, BLAU, APP }

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
