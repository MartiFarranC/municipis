package cat.descobreix.data.sincronitzacio

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import javax.inject.Inject

/** El servidor de la sincronització. És una interfície perquè els tests en puguin fer servir un altre. */
interface ServidorSincronitzacio {
    /** L'usuari amb sessió, o null si no n'hi ha. */
    fun usuari(): String?

    /**
     * Puja files a una taula. Si [nomesNoves], les que ja hi són (pel mateix id) no es toquen; si no, se substitueixen.
     */
    suspend fun <T> puja(taula: String, serialitzador: KSerializer<T>, files: List<T>, nomesNoves: Boolean)

    /** Les files de l'[usuari] a la taula que han canviat des de [des] (instant ISO del servidor), en ordre. */
    suspend fun <T> baixa(taula: String, serialitzador: KSerializer<T>, usuari: String, des: String?, limit: Int): List<T>

    /** Les files d'una taula de contingut comú (la dels ajuntaments) que han canviat des de [des], en ordre. */
    suspend fun <T> baixaComu(taula: String, serialitzador: KSerializer<T>, des: String?, limit: Int): List<T>

    /** Un fitxer d'un bucket públic (els segells dels ajuntaments). */
    suspend fun baixaFitxerPublic(bucket: String, ruta: String): ByteArray

    suspend fun pujaFitxer(ruta: String, bytes: ByteArray)

    suspend fun baixaFitxer(ruta: String): ByteArray

    suspend fun esborraFitxers(rutes: List<String>)
}

class ServidorSupabase @Inject constructor(private val supabase: SupabaseClient) : ServidorSincronitzacio {
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    override fun usuari(): String? = supabase.auth.currentUserOrNull()?.id

    override suspend fun <T> puja(taula: String, serialitzador: KSerializer<T>, files: List<T>, nomesNoves: Boolean) {
        if (files.isEmpty()) return
        val cos = json.encodeToJsonElement(ListSerializer(serialitzador), files) as JsonArray
        supabase.from(taula).upsert(cos) {
            onConflict = "id"
            ignoreDuplicates = nomesNoves
        }
    }

    override suspend fun <T> baixa(taula: String, serialitzador: KSerializer<T>, usuari: String, des: String?, limit: Int): List<T> {
        val resposta = supabase.from(taula).select {
            // Només les files pròpies: la RLS també en deixa veure d'altres (per exemple, fotos públiques).
            filter {
                eq("usuari_id", usuari)
                if (des != null) gt("sincronitzat_el", des)
            }
            order("sincronitzat_el", Order.ASCENDING)
            limit(limit.toLong())
        }
        return json.decodeFromString(ListSerializer(serialitzador), resposta.data)
    }

    override suspend fun <T> baixaComu(taula: String, serialitzador: KSerializer<T>, des: String?, limit: Int): List<T> {
        val resposta = supabase.from(taula).select {
            if (des != null) filter { gt("sincronitzat_el", des) }
            order("sincronitzat_el", Order.ASCENDING)
            limit(limit.toLong())
        }
        return json.decodeFromString(ListSerializer(serialitzador), resposta.data)
    }

    override suspend fun baixaFitxerPublic(bucket: String, ruta: String): ByteArray = supabase.storage.from(bucket).downloadPublic(ruta)

    override suspend fun pujaFitxer(ruta: String, bytes: ByteArray) {
        supabase.storage.from(BUCKET).upload(ruta, bytes) { upsert = true }
    }

    override suspend fun baixaFitxer(ruta: String): ByteArray = supabase.storage.from(BUCKET).downloadAuthenticated(ruta)

    override suspend fun esborraFitxers(rutes: List<String>) {
        if (rutes.isNotEmpty()) supabase.storage.from(BUCKET).delete(rutes)
    }

    private companion object {
        const val BUCKET = "descobreix-fotos"
    }
}
