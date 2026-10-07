package cat.descobreix.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Totes les entitats de l'usuari tenen un identificador UUID i dates de creació i modificació
// (en mil·lisegons), perquè a la versió 2 es puguin sincronitzar amb un servidor.

@Entity(tableName = "municipis_descoberts", indices = [Index(value = ["codiIne"], unique = true)])
data class MunicipiDescobertEntity(
    @PrimaryKey val id: String,
    val codiIne: String,
    val esInici: Boolean,
    /** Punts que va costar desbloquejar-lo (0 per al municipi d'inici). */
    val cost: Int,
    val creatEl: Long,
    val modificatEl: Long,
)

@Entity(
    tableName = "missions_completades",
    indices = [Index(value = ["missioId"], unique = true), Index(value = ["codiIne"])],
)
data class MissioCompletadaEntity(
    @PrimaryKey val id: String,
    val missioId: String,
    val codiIne: String,
    val punts: Int,
    val bonus: Int,
    val lat: Double?,
    val lon: Double?,
    val precisio: Double?,
    val fotoId: String?,
    val creatEl: Long,
    val modificatEl: Long,
)

/** Cada guany o despesa de punts. El saldo és la suma dels guanys menys la de les despeses. */
@Entity(tableName = "moviments_punts", indices = [Index(value = ["referencia"])])
data class MovimentPuntsEntity(
    @PrimaryKey val id: String,
    /** GUANY o DESPESA. */
    val tipus: String,
    val quantitat: Int,
    /** MISSIO, BONUS, DESBLOQUEIG o MEDALLA. */
    val motiu: String,
    /** Identificador de la missió, codi INE del municipi o identificador de la medalla. */
    val referencia: String,
    val creatEl: Long,
    val modificatEl: Long,
) {
    companion object {
        const val GUANY = "GUANY"
        const val DESPESA = "DESPESA"
        const val MOTIU_MISSIO = "MISSIO"
        const val MOTIU_BONUS = "BONUS"
        const val MOTIU_DESBLOQUEIG = "DESBLOQUEIG"
        const val MOTIU_MEDALLA = "MEDALLA"
    }
}

/** Missió proposada per l'usuari. A la versió 1 només la veu qui la crea i no dona punts. */
@Entity(tableName = "missions_propies", indices = [Index(value = ["codiIne"])])
data class MissioPropiaEntity(
    @PrimaryKey val id: String,
    val codiIne: String,
    val titol: String,
    val descripcio: String?,
    val completada: Boolean,
    val creatEl: Long,
    val modificatEl: Long,
)

@Entity(tableName = "fotos", indices = [Index(value = ["codiIne"])])
data class FotoEntity(
    @PrimaryKey val id: String,
    val codiIne: String,
    /** Nom del fitxer a la carpeta de fotos de l'emmagatzematge intern. */
    val fitxer: String,
    val miniatura: String,
    val lat: Double?,
    val lon: Double?,
    /** PRIVADA, AMICS o PUBLICA. */
    val visibilitat: String,
    val esPortada: Boolean,
    val missioId: String?,
    val creatEl: Long,
    val modificatEl: Long,
)

/**
 * Un segell del passaport: un per municipi, on l'usuari l'ha posat a la pàgina de la seva comarca.
 * [x] i [y] són el centre del segell en fracció de la pàgina; [gir] en graus; [tinta] l'índex del color.
 */
@Entity(tableName = "segells", indices = [Index(value = ["codiIne"], unique = true), Index(value = ["comarca"])])
data class SegellEntity(
    @PrimaryKey val id: String,
    val codiIne: String,
    val comarca: String,
    val pagina: Int,
    val x: Float,
    val y: Float,
    val gir: Float,
    val tinta: Int,
    val creatEl: Long,
    val modificatEl: Long,
)

