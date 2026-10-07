package cat.descobreix.data.social

import cat.descobreix.data.compte.TipusPerfil
import cat.descobreix.joc.model.CodiIne
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.OffsetDateTime
import javax.inject.Inject

// Seguir altra gent (docs/decisions-pendents.md). Tot això passa al servidor: cal connexió.

/** Una persona en una llista (resultats de cercar, sol·licituds, gent que segueixes). */
data class Persona(val id: String, val nom: String, val tipus: TipusPerfil)

enum class EstatSeguiment { PENDENT, ACCEPTAT }

/** El perfil d'una altra persona i com està el seguiment amb tu. */
data class PerfilPersona(
    val id: String,
    val nom: String,
    val tipus: TipusPerfil,
    val public: Boolean,
    val seguidors: Int,
    val seguits: Int,
    /** Si la segueixes: null (no), pendent (no t'ha acceptat) o acceptat. */
    val elSegueixo: EstatSeguiment?,
    val emSegueix: Boolean,
) {
    /** Si se'n pot veure el mapa i les fotos per a seguidors (si l'ha acceptat). */
    val visible: Boolean get() = elSegueixo == EstatSeguiment.ACCEPTAT
}

enum class TipusEvent { MUNICIPI, FOTO }

/** Una cosa del mur: algú que segueixes ha desbloquejat un municipi o ha penjat una foto. */
data class EventMur(
    val tipus: TipusEvent,
    val usuariId: String,
    val nom: String,
    val codiIne: CodiIne,
    val fotoId: String?,
    val rutaMiniatura: String?,
    val creatEl: Long,
)

/** Una foto d'una altra persona que es pot veure. */
data class FotoPersona(val id: String, val codiIne: CodiIne, val ruta: String, val rutaMiniatura: String, val creatEl: Long)

/** Una reacció que t'han enviat: un emoji (amb la seva frase) o un missatge, per un municipi o per una foto. */
data class ReaccioRebuda(
    val id: String,
    val autorId: String,
    val autorNom: String,
    val objectiu: TipusEvent,
    val objectiuId: String,
    val emoji: String?,
    val missatge: String?,
    val creatEl: Long,
)

/** Totes les operacions poden llançar excepcions (sense connexió, per exemple). */
interface ServeiSocial {
    suspend fun cerca(text: String): List<Persona>

    suspend fun perfil(id: String): PerfilPersona?

    suspend fun segueix(id: String)

    /** Deixar de seguir algú (o retirar la sol·licitud). */
    suspend fun deixaDeSeguir(id: String)

    suspend fun accepta(seguidorId: String)

    /** Rebutjar una sol·licitud o treure un seguidor. */
    suspend fun treu(seguidorId: String)

    suspend fun sollicituds(): List<Persona>

    /** La gent que segueixes (també les sol·licituds que encara no t'han acceptat). */
    suspend fun seguits(): List<Pair<Persona, EstatSeguiment>>

    /** La gent que segueix [id] (si es pot veure). */
    suspend fun seguitsDe(id: String): List<Persona>

    suspend fun mur(abans: Long?): List<EventMur>

    suspend fun municipisDe(id: String): Set<CodiIne>

    suspend fun fotosDe(id: String): List<FotoPersona>

    /** El fitxer d'una foto (o d'una miniatura) d'algú altre que es pot veure. */
    suspend fun imatge(ruta: String): ByteArray

    /** Anima algú que segueixes per un municipi o per una foto: amb un emoji o amb un missatge (no tots dos). */
    suspend fun anima(destinatari: String, objectiu: TipusEvent, objectiuId: String, emoji: String?, missatge: String?)

    /** Les reaccions que t'han enviat, de la més nova a la més antiga. */
    suspend fun reaccionsRebudes(): List<ReaccioRebuda>

    /** Treu una reacció que t'han enviat. */
    suspend fun treuReaccio(id: String)

    suspend fun bloqueja(id: String)

    suspend fun desbloqueja(id: String)

    suspend fun hasBloquejat(id: String): Boolean

    /** Denuncia algú; si és per una reacció, se'n desa el text perquè es pugui revisar. */
    suspend fun denuncia(id: String, reaccio: ReaccioRebuda?, motiu: String?)
}

class ServeiSocialSupabase @Inject constructor(client: dagger.Lazy<SupabaseClient>) : ServeiSocial {
    // El client es crea quan cal: sense configuració (per exemple, als tests d'interfície), les crides fallen com sense connexió.
    private val supabase: SupabaseClient by lazy { client.get() }

    private fun jo(): String = checkNotNull(supabase.auth.currentUserOrNull()?.id) { "Cal una sessió" }

    override suspend fun cerca(text: String): List<Persona> {
        val t = text.trim().lowercase().filter { it.isLetterOrDigit() || it == '_' }
        if (t.isEmpty()) return emptyList()
        return supabase.from(PERFILS).select {
            filter {
                ilike("nom_usuari", "$t%")
                neq("id", jo())
            }
            order("nom_usuari", Order.ASCENDING)
            limit(30)
        }.decodeList<PerfilRemot>().map { it.persona() }
    }

    override suspend fun perfil(id: String): PerfilPersona? =
        supabase.postgrest.rpc("perfil_de", buildJsonObject { put("usuari", id) }).decodeList<PerfilDeRemot>().firstOrNull()?.let {
            PerfilPersona(
                it.id, it.nomUsuari, tipus(it.tipus), it.public, it.seguidors.toInt(), it.seguits.toInt(),
                it.elSegueixo?.let { e -> EstatSeguiment.entries.firstOrNull { x -> x.name == e } }, it.emSegueix,
            )
        }

    override suspend fun segueix(id: String) {
        supabase.from(SEGUIMENTS).insert(buildJsonObject { put("seguit_id", id) })
    }

    override suspend fun deixaDeSeguir(id: String) {
        supabase.from(SEGUIMENTS).delete { filter { eq("seguidor_id", jo()); eq("seguit_id", id) } }
    }

    override suspend fun accepta(seguidorId: String) {
        supabase.from(SEGUIMENTS).update({ set("estat", EstatSeguiment.ACCEPTAT.name) }) {
            filter { eq("seguidor_id", seguidorId); eq("seguit_id", jo()) }
        }
    }

    override suspend fun treu(seguidorId: String) {
        supabase.from(SEGUIMENTS).delete { filter { eq("seguidor_id", seguidorId); eq("seguit_id", jo()) } }
    }

    override suspend fun sollicituds(): List<Persona> {
        val ids = supabase.from(SEGUIMENTS).select {
            filter { eq("seguit_id", jo()); eq("estat", EstatSeguiment.PENDENT.name) }
        }.decodeList<SeguimentRemot>().map { it.seguidorId }
        return persones(ids)
    }

    override suspend fun seguits(): List<Pair<Persona, EstatSeguiment>> {
        val s = supabase.from(SEGUIMENTS).select { filter { eq("seguidor_id", jo()) } }.decodeList<SeguimentRemot>()
        val p = persones(s.map { it.seguitId }).associateBy { it.id }
        return s.mapNotNull { r -> p[r.seguitId]?.let { it to (EstatSeguiment.entries.firstOrNull { e -> e.name == r.estat } ?: EstatSeguiment.PENDENT) } }
            .sortedBy { it.first.nom }
    }

    override suspend fun seguitsDe(id: String): List<Persona> {
        val ids = supabase.from(SEGUIMENTS).select {
            filter { eq("seguidor_id", id); eq("estat", EstatSeguiment.ACCEPTAT.name) }
        }.decodeList<SeguimentRemot>().map { it.seguitId }
        return persones(ids).sortedBy { it.nom }
    }

    override suspend fun mur(abans: Long?): List<EventMur> =
        supabase.postgrest.rpc(
            "mur",
            buildJsonObject {
                if (abans != null) put("abans", abans)
                put("quants", PAGINA_MUR)
            },
        ).decodeList<EventRemot>().map {
            EventMur(
                if (it.tipus == "FOTO") TipusEvent.FOTO else TipusEvent.MUNICIPI, it.usuariId, it.nomUsuari, it.codiIne,
                it.fotoId, it.rutaMiniatura, it.creatEl,
            )
        }

    override suspend fun municipisDe(id: String): Set<CodiIne> =
        supabase.from("municipis_descoberts").select { filter { eq("usuari_id", id) } }.decodeList<DescobertRemot>().map { it.codiIne }.toSet()

    override suspend fun fotosDe(id: String): List<FotoPersona> =
        supabase.from("fotos").select {
            filter {
                eq("usuari_id", id)
                exact("esborrat_el", null)
            }
            order("creat_el", Order.DESCENDING)
            limit(200)
        }.decodeList<FotoRemotaPersona>().map { FotoPersona(it.id, it.codiIne, it.ruta, it.rutaMiniatura, it.creatEl) }

    override suspend fun imatge(ruta: String): ByteArray = supabase.storage.from(BUCKET).downloadAuthenticated(ruta)

    override suspend fun anima(destinatari: String, objectiu: TipusEvent, objectiuId: String, emoji: String?, missatge: String?) {
        require((emoji == null) != (missatge == null)) { "Un emoji o un missatge" }
        supabase.from(REACCIONS).insert(
            buildJsonObject {
                put("destinatari_id", destinatari)
                put("objectiu_tipus", objectiu.name)
                put("objectiu_id", objectiuId)
                if (emoji != null) put("emoji", emoji)
                if (missatge != null) put("missatge", missatge.trim())
            },
        )
    }

    override suspend fun reaccionsRebudes(): List<ReaccioRebuda> {
        val r = supabase.from(REACCIONS).select {
            filter { eq("destinatari_id", jo()) }
            order("creat_el", Order.DESCENDING)
            limit(50)
        }.decodeList<ReaccioRemota>()
        val noms = persones(r.map { it.autorId }.distinct()).associate { it.id to it.nom }
        return r.map {
            ReaccioRebuda(
                it.id, it.autorId, noms[it.autorId] ?: "", if (it.objectiuTipus == "FOTO") TipusEvent.FOTO else TipusEvent.MUNICIPI,
                it.objectiuId, it.emoji, it.missatge, OffsetDateTime.parse(it.creatEl).toInstant().toEpochMilli(),
            )
        }
    }

    override suspend fun treuReaccio(id: String) {
        supabase.from(REACCIONS).delete { filter { eq("id", id) } }
    }

    override suspend fun bloqueja(id: String) {
        supabase.from(BLOQUEJOS).insert(buildJsonObject { put("bloquejat_id", id) })
    }

    override suspend fun desbloqueja(id: String) {
        supabase.from(BLOQUEJOS).delete { filter { eq("bloquejador_id", jo()); eq("bloquejat_id", id) } }
    }

    override suspend fun hasBloquejat(id: String): Boolean =
        supabase.from(BLOQUEJOS).select { filter { eq("bloquejador_id", jo()); eq("bloquejat_id", id) } }.decodeList<BloqueigRemot>().isNotEmpty()

    override suspend fun denuncia(id: String, reaccio: ReaccioRebuda?, motiu: String?) {
        supabase.from(DENUNCIES).insert(
            buildJsonObject {
                put("denunciat_id", id)
                if (reaccio != null) {
                    put("reaccio_id", reaccio.id)
                    put("text_denunciat", reaccio.missatge ?: reaccio.emoji)
                }
                motiu?.trim()?.takeIf { it.isNotEmpty() }?.let { put("motiu", it.take(500)) }
            },
        )
    }

    private suspend fun persones(ids: List<String>): List<Persona> {
        if (ids.isEmpty()) return emptyList()
        return supabase.from(PERFILS).select { filter { isIn("id", ids) } }.decodeList<PerfilRemot>().map { it.persona() }
    }

    @Serializable
    private data class PerfilRemot(val id: String, @SerialName("nom_usuari") val nomUsuari: String, val tipus: String = "") {
        fun persona() = Persona(id, nomUsuari, tipus(tipus))
    }

    @Serializable
    private data class PerfilDeRemot(
        val id: String,
        @SerialName("nom_usuari") val nomUsuari: String,
        val tipus: String,
        val public: Boolean,
        val seguidors: Long,
        val seguits: Long,
        @SerialName("el_segueixo") val elSegueixo: String? = null,
        @SerialName("em_segueix") val emSegueix: Boolean,
    )

    @Serializable
    private data class SeguimentRemot(
        @SerialName("seguidor_id") val seguidorId: String,
        @SerialName("seguit_id") val seguitId: String,
        val estat: String,
    )

    @Serializable
    private data class EventRemot(
        val tipus: String,
        @SerialName("usuari_id") val usuariId: String,
        @SerialName("nom_usuari") val nomUsuari: String,
        @SerialName("codi_ine") val codiIne: String,
        @SerialName("foto_id") val fotoId: String? = null,
        @SerialName("ruta_miniatura") val rutaMiniatura: String? = null,
        @SerialName("creat_el") val creatEl: Long,
    )

    @Serializable
    private data class DescobertRemot(@SerialName("codi_ine") val codiIne: String)

    @Serializable
    private data class FotoRemotaPersona(
        val id: String,
        @SerialName("codi_ine") val codiIne: String,
        val ruta: String,
        @SerialName("ruta_miniatura") val rutaMiniatura: String,
        @SerialName("creat_el") val creatEl: Long,
    )

    @Serializable
    private data class ReaccioRemota(
        val id: String,
        @SerialName("autor_id") val autorId: String,
        @SerialName("objectiu_tipus") val objectiuTipus: String,
        @SerialName("objectiu_id") val objectiuId: String,
        val emoji: String? = null,
        val missatge: String? = null,
        @SerialName("creat_el") val creatEl: String,
    )

    @Serializable
    private data class BloqueigRemot(@SerialName("bloquejat_id") val bloquejatId: String)

    private companion object {
        const val REACCIONS = "reaccions"
        const val BLOQUEJOS = "bloquejos"
        const val DENUNCIES = "denuncies"
        const val PERFILS = "perfils"
        const val SEGUIMENTS = "seguiments"
        const val BUCKET = "descobreix-fotos"
        const val PAGINA_MUR = 50

        fun tipus(t: String) = TipusPerfil.entries.firstOrNull { it.name == t } ?: TipusPerfil.EXPLORADOR
    }
}
