package cat.descobreix.data.repositori

import android.content.Context
import cat.descobreix.data.db.AjuntamentEntity
import cat.descobreix.data.db.AvantatgeEntity
import cat.descobreix.data.db.BaseDades
import cat.descobreix.data.db.MissioAjuntamentEntity
import cat.descobreix.joc.model.CodiIne
import cat.descobreix.joc.model.TipusProva
import cat.descobreix.joc.regles.MissioAjuntament
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.io.File
import java.time.LocalDate
import javax.inject.Inject

/** Un ajuntament que col·labora i el que hi ha posat (requisits.md, secció 9.6). */
data class Ajuntament(
    val codi: CodiIne,
    val presentacio: String?,
    val web: String?,
    val oficinaTurisme: String?,
    val teSegell: Boolean,
)

/** Un avantatge fora de l'app. L'app només el mostra. */
data class Avantatge(
    val id: String,
    val codi: CodiIne,
    val titol: String,
    val descripcio: String?,
    val condicions: String?,
    val validFins: LocalDate?,
)

/** El contingut dels ajuntaments, baixat del servidor. L'app només el llegeix. */
interface AjuntamentsRepositori {
    /** Els ajuntaments que col·laboren, pel codi del municipi. */
    val ajuntaments: Flow<Map<CodiIne, Ajuntament>>

    /** Totes les missions oficials, també les retirades (les regles decideixen quines es fan servir). */
    val missions: Flow<List<MissioAjuntament>>

    val avantatges: Flow<List<Avantatge>>

    suspend fun missionsAra(): List<MissioAjuntament>

    /** El fitxer del segell propi d'un municipi, si ja s'ha baixat. */
    fun segell(codi: CodiIne): File?

    /** Sense cap ajuntament: per als tests i per a quan no cal. */
    object Buit : AjuntamentsRepositori {
        override val ajuntaments: Flow<Map<CodiIne, Ajuntament>> = flowOf(emptyMap())
        override val missions: Flow<List<MissioAjuntament>> = flowOf(emptyList())
        override val avantatges: Flow<List<Avantatge>> = flowOf(emptyList())

        override suspend fun missionsAra(): List<MissioAjuntament> = emptyList()

        override fun segell(codi: CodiIne): File? = null
    }
}

class AjuntamentsRepositoriRoom @Inject constructor(
    @ApplicationContext private val context: Context,
    db: BaseDades,
) : AjuntamentsRepositori {
    private val dao = db.ajuntaments()

    override val ajuntaments: Flow<Map<CodiIne, Ajuntament>> = dao.ajuntaments().map { l -> l.associate { it.codiIne to it.domini() } }

    override val missions: Flow<List<MissioAjuntament>> = dao.missions().map { l -> l.mapNotNull { it.domini() } }

    override val avantatges: Flow<List<Avantatge>> = dao.avantatges().map { l -> l.map { it.domini() } }

    override suspend fun missionsAra(): List<MissioAjuntament> = missions.first()

    override fun segell(codi: CodiIne): File? = fitxerSegell(context, codi).takeIf { it.exists() }

    companion object {
        /** On es desa el segell propi d'un municipi (el baixa la sincronització). */
        fun fitxerSegell(context: Context, codi: CodiIne): File = File(File(context.filesDir, "ajuntaments").apply { mkdirs() }, "$codi.png")
    }
}

private fun AjuntamentEntity.domini() = Ajuntament(codiIne, presentacio, web, oficinaTurisme, teSegell)

private fun AvantatgeEntity.domini() = Avantatge(id, codiIne, titol, descripcio, condicions, validFins?.let(LocalDate::parse))

/** Una prova que aquesta versió de l'app no coneix: la missió no es fa servir. */
private fun MissioAjuntamentEntity.domini(): MissioAjuntament? {
    val p = TipusProva.entries.firstOrNull { it.name == prova } ?: return null
    return MissioAjuntament(
        id = id, municipi = codiIne, titol = titol, descripcio = descripcio, prova = p, lat = lat, lon = lon, resumQr = resumQr,
        dataInici = dataInici?.let(LocalDate::parse), dataFi = dataFi?.let(LocalDate::parse), ordre = ordre, retirada = retirada,
    )
}
