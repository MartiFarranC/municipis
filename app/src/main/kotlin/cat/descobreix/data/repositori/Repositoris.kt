package cat.descobreix.data.repositori

import cat.descobreix.domain.Foto
import cat.descobreix.domain.MissioPropia
import cat.descobreix.domain.Progres
import cat.descobreix.joc.model.CodiIne
import cat.descobreix.joc.model.Visibilitat
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
}

data class CameraMapa(val x: Float, val y: Float, val escala: Float)
