package cat.descobreix.data.ranquing

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import cat.descobreix.data.db.BaseDades
import cat.descobreix.data.db.MissioCompletadaEntity
import cat.descobreix.data.db.MunicipiDescobertEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServeiRanquingSupabase @Inject constructor(
    private val supabase: SupabaseClient,
    private val db: BaseDades,
    private val dataStore: DataStore<Preferences>,
) : ServeiRanquing {
    private val mutex = Mutex()

    override suspend fun classificacio(criteri: CriteriRanquing, nomesAmics: Boolean): List<FilaRanquing> =
        supabase.postgrest
            .rpc(
                "ranquing",
                buildJsonObject {
                    put("criteri", criteri.name)
                    put("nomes_amics", nomesAmics)
                    put("limit_files", LIMIT_RANQUING)
                },
            )
            .decodeList<FilaRemota>()
            .map { FilaRanquing(it.posicio, it.usuariId, it.nomUsuari, it.foto, it.punts, it.municipis, it.socJo) }

    /**
     * Els municipis descoberts i les missions completades només s'afegeixen (secció 9.2), així que
     * n'hi ha prou de recordar fins a quina data de creació s'han pujat. Les files repetides el
     * servidor les ignora. Primer els municipis, perquè les missions només compten si el municipi
     * és descobert.
     */
    override suspend fun pujaProgres() = mutex.withLock {
        val usuari = supabase.auth.currentUserOrNull()?.id ?: return@withLock
        val dao = db.progres()
        puja(CURSOR_DESCOBERTS, dao.descobertsAra(), { it.creatEl }) { files ->
            supabase.postgrest.from("municipis_descoberts").upsert(files.map { it.aRemot(usuari) }) {
                onConflict = "usuari_id,codi_ine"
                ignoreDuplicates = true
            }
        }
        puja(CURSOR_COMPLETADES, dao.completadesAra(), { it.creatEl }) { files ->
            supabase.postgrest.from("missions_completades").upsert(files.map { it.aRemot(usuari) }) {
                onConflict = "usuari_id,missio_id"
                ignoreDuplicates = true
            }
        }
    }

    private suspend fun <T> puja(clau: Preferences.Key<Long>, totes: List<T>, creatEl: (T) -> Long, envia: suspend (List<T>) -> Unit) {
        val cursor = dataStore.data.first()[clau]
        val pendents = Pujada.pendents(totes, cursor, creatEl)
        for (bloc in pendents.chunked(MIDA_BLOC)) {
            envia(bloc)
            dataStore.edit { it[clau] = bloc.maxOf(creatEl) }
        }
    }

    @Serializable
    private data class FilaRemota(
        val posicio: Int,
        @SerialName("usuari_id") val usuariId: String,
        @SerialName("nom_usuari") val nomUsuari: String,
        val foto: String? = null,
        val punts: Long,
        val municipis: Long,
        @SerialName("soc_jo") val socJo: Boolean,
    )

    @Serializable
    private data class DescobertRemot(
        val id: String,
        @SerialName("usuari_id") val usuariId: String,
        @SerialName("codi_ine") val codiIne: String,
        @SerialName("es_inici") val esInici: Boolean,
        val cost: Int,
        @SerialName("creat_el") val creatEl: Long,
        @SerialName("modificat_el") val modificatEl: Long,
    )

    @Serializable
    private data class CompletadaRemota(
        val id: String,
        @SerialName("usuari_id") val usuariId: String,
        @SerialName("missio_id") val missioId: String,
        @SerialName("codi_ine") val codiIne: String,
        val punts: Int,
        val bonus: Int,
        val lat: Double?,
        val lon: Double?,
        val precisio: Double?,
        @SerialName("foto_id") val fotoId: String?,
        @SerialName("creat_el") val creatEl: Long,
        @SerialName("modificat_el") val modificatEl: Long,
    )

    private fun MunicipiDescobertEntity.aRemot(usuari: String) =
        DescobertRemot(id, usuari, codiIne, esInici, cost, creatEl, modificatEl)

    private fun MissioCompletadaEntity.aRemot(usuari: String) =
        CompletadaRemota(id, usuari, missioId, codiIne, punts, bonus, lat, lon, precisio, fotoId, creatEl, modificatEl)

    private companion object {
        const val MIDA_BLOC = 500
        val CURSOR_DESCOBERTS = longPreferencesKey("pujat_descoberts_fins")
        val CURSOR_COMPLETADES = longPreferencesKey("pujat_completades_fins")
    }
}

object Pujada {
    /**
     * Files que encara no s'han pujat: les creades a partir del [cursor], ordenades per data de
     * creació. Inclou les de la mateixa data que el cursor, per si n'hi havia més d'una amb la
     * mateixa data i no es van pujar totes.
     */
    fun <T> pendents(totes: List<T>, cursor: Long?, creatEl: (T) -> Long): List<T> =
        totes.filter { cursor == null || creatEl(it) >= cursor }.sortedBy(creatEl)
}
