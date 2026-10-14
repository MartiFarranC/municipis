package cat.descobreix.data.db

import androidx.room.ColumnInfo
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
    /** MISSIO, BONUS, DESBLOQUEIG, MEDALLA o SAC. */
    val motiu: String,
    /** Identificador de la missió, codi INE del municipi, identificador de la medalla o origen del sac. */
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
        const val MOTIU_SAC = "SAC"
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
    /** Esborrat lògic, perquè l'esborrat es pugui sincronitzar. */
    val esborratEl: Long? = null,
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
    /** PRIVADA, SEGUIDORS o PUBLICA. */
    val visibilitat: String,
    val esPortada: Boolean,
    val missioId: String?,
    val creatEl: Long,
    val modificatEl: Long,
    /** És la foto del cartell feta amb el requadre (ja retallada): la que va al catàleg. */
    @ColumnInfo(defaultValue = "0") val esCromo: Boolean = false,
    /** Esborrat lògic, perquè l'esborrat es pugui sincronitzar. */
    val esborratEl: Long? = null,
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


/**
 * Un sac guanyat. [origen] diu per què es va guanyar (`primera_foto`, `medalla_comarca_24_bronze`…) i no es repeteix.
 * Quan s'obre, es desa què en va sortir: una cosa ([objecteTipus] i [objecteId]) o, si ja es tenia tot, [punts].
 */
@Entity(tableName = "sacs", indices = [Index(value = ["origen"], unique = true)])
data class SacEntity(
    @PrimaryKey val id: String,
    val origen: String,
    val objecteTipus: String?,
    val objecteId: String?,
    val punts: Int?,
    val obertEl: Long?,
    val creatEl: Long,
    val modificatEl: Long,
)

/** Una fila que ha canviat al mòbil i s'ha de pujar al servidor: el nom de la taula i l'id de la fila. */
@Entity(tableName = "canvis_pendents", primaryKeys = ["taula", "id"])
data class CanviPendentEntity(val taula: String, val id: String)

/** Una sola fila (id 0): si s'estan aplicant dades del servidor, que els triggers no han de posar a la cua. */
@Entity(tableName = "control_sincronitzacio")
data class ControlSincronitzacioEntity(@PrimaryKey val id: Int = 0, val aplicant: Boolean = false)

// Contingut dels ajuntaments (requisits.md, secció 9.6). És una còpia del servidor, que l'app només llegeix: no es
// puja mai i no té triggers de la cua. [retirada] és l'esborrat lògic del servidor.

/** Un ajuntament que col·labora. */
@Entity(tableName = "ajuntaments")
data class AjuntamentEntity(
    @PrimaryKey val codiIne: String,
    val presentacio: String?,
    val web: String?,
    val oficinaTurisme: String?,
    val teSegell: Boolean,
    val retirat: Boolean,
)

/** Una missió oficial d'un ajuntament. Les dates són ISO (`2026-04-05`). */
@Entity(tableName = "missions_ajuntament", indices = [Index(value = ["codiIne"])])
data class MissioAjuntamentEntity(
    @PrimaryKey val id: String,
    val codiIne: String,
    val titol: String,
    val descripcio: String?,
    val prova: String,
    val lat: Double?,
    val lon: Double?,
    val resumQr: String?,
    val dataInici: String?,
    val dataFi: String?,
    val ordre: Int,
    val retirada: Boolean,
)

/** Un avantatge fora de l'app que ofereix un ajuntament. */
@Entity(tableName = "avantatges", indices = [Index(value = ["codiIne"])])
data class AvantatgeEntity(
    @PrimaryKey val id: String,
    val codiIne: String,
    val titol: String,
    val descripcio: String?,
    val condicions: String?,
    val validFins: String?,
    val retirat: Boolean,
)
