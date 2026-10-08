package cat.descobreix.data.sincronitzacio

import cat.descobreix.data.db.AjuntamentEntity
import cat.descobreix.data.db.AvantatgeEntity
import cat.descobreix.data.db.FotoEntity
import cat.descobreix.data.db.MissioAjuntamentEntity
import cat.descobreix.data.db.MissioCompletadaEntity
import cat.descobreix.data.db.MissioPropiaEntity
import cat.descobreix.data.db.MovimentPuntsEntity
import cat.descobreix.data.db.MunicipiDescobertEntity
import cat.descobreix.data.db.SacEntity
import cat.descobreix.data.db.SegellEntity
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Les files tal com són a les taules de l'esquema descobreix del servidor (supabase/migrations). L'usuari no s'envia:
// el posa el servidor (auth.uid()). `sincronitzat_el` el posa el servidor i només es llegeix.

@Serializable
data class DescobertRemot(
    val id: String,
    @SerialName("codi_ine") val codiIne: String,
    @SerialName("es_inici") val esInici: Boolean,
    val cost: Int,
    @SerialName("creat_el") val creatEl: Long,
    @SerialName("modificat_el") val modificatEl: Long,
    @SerialName("sincronitzat_el") val sincronitzatEl: String? = null,
)

@Serializable
data class CompletadaRemota(
    val id: String,
    @SerialName("missio_id") val missioId: String,
    @SerialName("codi_ine") val codiIne: String,
    val punts: Int,
    val bonus: Int,
    val lat: Double? = null,
    val lon: Double? = null,
    val precisio: Double? = null,
    @SerialName("foto_id") val fotoId: String? = null,
    @SerialName("creat_el") val creatEl: Long,
    @SerialName("modificat_el") val modificatEl: Long,
    @SerialName("sincronitzat_el") val sincronitzatEl: String? = null,
)

@Serializable
data class MovimentRemot(
    val id: String,
    val tipus: String,
    val quantitat: Int,
    val motiu: String,
    val referencia: String,
    @SerialName("creat_el") val creatEl: Long,
    @SerialName("modificat_el") val modificatEl: Long,
    @SerialName("sincronitzat_el") val sincronitzatEl: String? = null,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class PropiaRemota(
    val id: String,
    @SerialName("codi_ine") val codiIne: String,
    val titol: String,
    val descripcio: String? = null,
    val completada: Boolean,
    @SerialName("creat_el") val creatEl: Long,
    @SerialName("modificat_el") val modificatEl: Long,
    // S'envia encara que sigui null, perquè un esborrat que es desfà també arribi.
    @EncodeDefault @SerialName("esborrat_el") val esborratEl: Long? = null,
    @SerialName("sincronitzat_el") val sincronitzatEl: String? = null,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class FotoRemota(
    val id: String,
    @SerialName("codi_ine") val codiIne: String,
    val ruta: String,
    @SerialName("ruta_miniatura") val rutaMiniatura: String,
    val lat: Double? = null,
    val lon: Double? = null,
    val visibilitat: String,
    @SerialName("es_portada") val esPortada: Boolean,
    @SerialName("missio_id") val missioId: String? = null,
    @SerialName("es_cromo") val esCromo: Boolean = false,
    @SerialName("creat_el") val creatEl: Long,
    @SerialName("modificat_el") val modificatEl: Long,
    @EncodeDefault @SerialName("esborrat_el") val esborratEl: Long? = null,
    @SerialName("sincronitzat_el") val sincronitzatEl: String? = null,
)

@Serializable
data class SegellRemot(
    val id: String,
    @SerialName("codi_ine") val codiIne: String,
    val comarca: String,
    val pagina: Int,
    val x: Float,
    val y: Float,
    val gir: Float,
    val tinta: Int,
    @SerialName("creat_el") val creatEl: Long,
    @SerialName("modificat_el") val modificatEl: Long,
    @SerialName("sincronitzat_el") val sincronitzatEl: String? = null,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class SacRemot(
    val id: String,
    val origen: String,
    @EncodeDefault @SerialName("objecte_tipus") val objecteTipus: String? = null,
    @EncodeDefault @SerialName("objecte_id") val objecteId: String? = null,
    @EncodeDefault val punts: Int? = null,
    @EncodeDefault @SerialName("obert_el") val obertEl: Long? = null,
    @SerialName("creat_el") val creatEl: Long,
    @SerialName("modificat_el") val modificatEl: Long,
    @SerialName("sincronitzat_el") val sincronitzatEl: String? = null,
)

// Conversions entre Room i el servidor. Les rutes de les fotos són <usuari>/<fitxer> i <usuari>/miniatures/<fitxer>.

fun MunicipiDescobertEntity.remot() = DescobertRemot(id, codiIne, esInici, cost, creatEl, modificatEl)
fun DescobertRemot.local() = MunicipiDescobertEntity(id, codiIne, esInici, cost, creatEl, modificatEl)

fun MissioCompletadaEntity.remot() = CompletadaRemota(id, missioId, codiIne, punts, bonus, lat, lon, precisio, fotoId, creatEl, modificatEl)
fun CompletadaRemota.local() = MissioCompletadaEntity(id, missioId, codiIne, punts, bonus, lat, lon, precisio, fotoId, creatEl, modificatEl)

fun MovimentPuntsEntity.remot() = MovimentRemot(id, tipus, quantitat, motiu, referencia, creatEl, modificatEl)
fun MovimentRemot.local() = MovimentPuntsEntity(id, tipus, quantitat, motiu, referencia, creatEl, modificatEl)

fun MissioPropiaEntity.remot() = PropiaRemota(id, codiIne, titol, descripcio, completada, creatEl, modificatEl, esborratEl)
fun PropiaRemota.local() = MissioPropiaEntity(id, codiIne, titol, descripcio, completada, creatEl, modificatEl, esborratEl)

fun rutaFoto(usuari: String, fitxer: String) = "$usuari/$fitxer"
fun rutaMiniatura(usuari: String, fitxer: String) = "$usuari/miniatures/$fitxer"

fun FotoEntity.remot(usuari: String) = FotoRemota(
    id, codiIne, rutaFoto(usuari, fitxer), rutaMiniatura(usuari, miniatura), lat, lon, visibilitat, esPortada, missioId, esCromo,
    creatEl, modificatEl, esborratEl,
)
fun FotoRemota.local() = FotoEntity(
    id = id, codiIne = codiIne, fitxer = ruta.substringAfterLast('/'), miniatura = rutaMiniatura.substringAfterLast('/'),
    lat = lat, lon = lon, visibilitat = visibilitat, esPortada = esPortada, missioId = missioId, creatEl = creatEl,
    modificatEl = modificatEl, esCromo = esCromo, esborratEl = esborratEl,
)

fun SegellEntity.remot() = SegellRemot(id, codiIne, comarca, pagina, x, y, gir, tinta, creatEl, modificatEl)
fun SegellRemot.local() = SegellEntity(id, codiIne, comarca, pagina, x, y, gir, tinta, creatEl, modificatEl)

fun SacEntity.remot() = SacRemot(id, origen, objecteTipus, objecteId, punts, obertEl, creatEl, modificatEl)
fun SacRemot.local() = SacEntity(id, origen, objecteTipus, objecteId, punts, obertEl, creatEl, modificatEl)

// Contingut dels ajuntaments (secció 9.6): només es baixa. `esborrat_el` és l'esborrat lògic del servidor.

@Serializable
data class AjuntamentRemot(
    @SerialName("codi_ine") val codiIne: String,
    val presentacio: String? = null,
    val web: String? = null,
    @SerialName("oficina_turisme") val oficinaTurisme: String? = null,
    @SerialName("te_segell") val teSegell: Boolean = false,
    @SerialName("esborrat_el") val esborratEl: Long? = null,
    @SerialName("sincronitzat_el") val sincronitzatEl: String? = null,
) {
    fun local() = AjuntamentEntity(codiIne, presentacio, web, oficinaTurisme, teSegell, retirat = esborratEl != null)
}

@Serializable
data class MissioAjuntamentRemota(
    val id: String,
    @SerialName("codi_ine") val codiIne: String,
    val titol: String,
    val descripcio: String? = null,
    val prova: String,
    val lat: Double? = null,
    val lon: Double? = null,
    @SerialName("resum_qr") val resumQr: String? = null,
    @SerialName("data_inici") val dataInici: String? = null,
    @SerialName("data_fi") val dataFi: String? = null,
    val ordre: Int = 0,
    @SerialName("esborrat_el") val esborratEl: Long? = null,
    @SerialName("sincronitzat_el") val sincronitzatEl: String? = null,
) {
    fun local() = MissioAjuntamentEntity(id, codiIne, titol, descripcio, prova, lat, lon, resumQr, dataInici, dataFi, ordre, retirada = esborratEl != null)
}

@Serializable
data class AvantatgeRemot(
    val id: String,
    @SerialName("codi_ine") val codiIne: String,
    val titol: String,
    val descripcio: String? = null,
    val condicions: String? = null,
    @SerialName("valid_fins") val validFins: String? = null,
    @SerialName("esborrat_el") val esborratEl: Long? = null,
    @SerialName("sincronitzat_el") val sincronitzatEl: String? = null,
) {
    fun local() = AvantatgeEntity(id, codiIne, titol, descripcio, condicions, validFins, retirat = esborratEl != null)
}
