package cat.descobreix.data.ranquing

import cat.descobreix.data.sincronitzacio.Sincronitzador
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServeiRanquingSupabase @Inject constructor(
    private val supabase: SupabaseClient,
    private val sincronitzador: Sincronitzador,
) : ServeiRanquing {
    override suspend fun classificacio(criteri: CriteriRanquing, nomesSeguits: Boolean): List<FilaRanquing> =
        supabase.postgrest
            .rpc(
                "ranquing",
                buildJsonObject {
                    put("criteri", criteri.name)
                    put("nomes_seguits", nomesSeguits)
                    put("limit_files", LIMIT_RANQUING)
                },
            )
            .decodeList<FilaRemota>()
            .map { FilaRanquing(it.posicio, it.usuariId, it.nomUsuari, it.foto, it.punts, it.municipis, it.socJo) }

    /** El progrés es puja amb la sincronització de sempre (secció 9.2). */
    override suspend fun pujaProgres() {
        sincronitzador.sincronitza()
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
}
