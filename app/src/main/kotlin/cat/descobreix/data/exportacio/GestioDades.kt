package cat.descobreix.data.exportacio

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import cat.descobreix.data.db.BaseDades
import cat.descobreix.data.repositori.FotosRepositoriRoom
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/** Exportar i esborrar tot el progrés de l'usuari (secció 8 de docs/requisits.md). */
@Singleton
class GestioDades @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: BaseDades,
    private val dataStore: DataStore<Preferences>,
) {
    private val json = Json { prettyPrint = true }

    /** Escriu un ZIP amb `progres.json` i les fotos. */
    suspend fun exporta(desti: Uri) = withContext(Dispatchers.IO) {
        val progres = db.progres()
        val dades = Exportacio(
            versio = 1,
            municipisDescoberts = progres.descobertsAra().map {
                Exportacio.Descobert(it.id, it.codiIne, it.esInici, it.cost, it.creatEl, it.modificatEl)
            },
            missionsCompletades = progres.completadesAra().map {
                Exportacio.Completada(it.id, it.missioId, it.codiIne, it.punts, it.bonus, it.lat, it.lon, it.precisio, it.fotoId, it.creatEl, it.modificatEl)
            },
            movimentsPunts = progres.movimentsAra().map {
                Exportacio.Moviment(it.id, it.tipus, it.quantitat, it.motiu, it.referencia, it.creatEl, it.modificatEl)
            },
            missionsPropies = db.missionsPropies().totesAra().map {
                Exportacio.Propia(it.id, it.codiIne, it.titol, it.descripcio, it.completada, it.creatEl, it.modificatEl)
            },
            fotos = db.fotos().totesAra().map {
                Exportacio.FotoExportada(it.id, it.codiIne, "fotos/${it.fitxer}", it.lat, it.lon, it.visibilitat, it.esPortada, it.missioId, it.creatEl, it.modificatEl, it.esCromo)
            },
            segells = db.segells().totsAra().map {
                Exportacio.SegellExportat(it.id, it.codiIne, it.comarca, it.pagina, it.x, it.y, it.gir, it.tinta, it.creatEl, it.modificatEl)
            },
            sacs = db.sacs().totsAra().map {
                Exportacio.SacExportat(it.id, it.origen, it.objecteTipus, it.objecteId, it.punts, it.obertEl, it.creatEl, it.modificatEl)
            },
        )
        val sortida = context.contentResolver.openOutputStream(desti) ?: error("No es pot escriure al fitxer triat")
        ZipOutputStream(sortida.buffered()).use { zip ->
            zip.putNextEntry(ZipEntry("progres.json"))
            zip.write(json.encodeToString(Exportacio.serializer(), dades).toByteArray())
            zip.closeEntry()
            val carpeta = FotosRepositoriRoom.carpeta(context)
            for (f in dades.fotos) {
                val fitxer = File(carpeta, f.fitxer.removePrefix("fotos/"))
                if (!fitxer.exists()) continue
                zip.putNextEntry(ZipEntry(f.fitxer))
                fitxer.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
            }
        }
    }

    /** Esborra el progrés, les missions pròpies, les fotos i les preferències. */
    suspend fun esborraTot() = withContext(Dispatchers.IO) {
        db.clearAllTables()
        FotosRepositoriRoom.carpeta(context).deleteRecursively()
        dataStore.edit { it.clear() }
    }

    @Serializable
    data class Exportacio(
        val versio: Int,
        val municipisDescoberts: List<Descobert>,
        val missionsCompletades: List<Completada>,
        val movimentsPunts: List<Moviment>,
        val missionsPropies: List<Propia>,
        val fotos: List<FotoExportada>,
        /** Els segells del passaport, amb el lloc de la pàgina on l'usuari els va posar. */
        val segells: List<SegellExportat> = emptyList(),
        /** Els sacs guanyats i el que n'ha sortit. */
        val sacs: List<SacExportat> = emptyList(),
    ) {
        @Serializable
        data class SacExportat(
            val id: String,
            val origen: String,
            val objecteTipus: String?,
            val objecteId: String?,
            val punts: Int?,
            val obertEl: Long?,
            val creatEl: Long,
            val modificatEl: Long,
        )

        @Serializable
        data class SegellExportat(
            val id: String,
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

        @Serializable
        data class Descobert(val id: String, val codiIne: String, val esInici: Boolean, val cost: Int, val creatEl: Long, val modificatEl: Long)

        @Serializable
        data class Completada(
            val id: String,
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

        @Serializable
        data class Moviment(
            val id: String,
            val tipus: String,
            val quantitat: Int,
            val motiu: String,
            val referencia: String,
            val creatEl: Long,
            val modificatEl: Long,
        )

        @Serializable
        data class Propia(
            val id: String,
            val codiIne: String,
            val titol: String,
            val descripcio: String?,
            val completada: Boolean,
            val creatEl: Long,
            val modificatEl: Long,
        )

        @Serializable
        data class FotoExportada(
            val id: String,
            val codiIne: String,
            val fitxer: String,
            val lat: Double?,
            val lon: Double?,
            val visibilitat: String,
            val esPortada: Boolean,
            val missioId: String?,
            val creatEl: Long,
            val modificatEl: Long,
            val esCromo: Boolean = false,
        )
    }
}
