package cat.descobreix.data.sincronitzacio

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.room.withTransaction
import cat.descobreix.data.db.BaseDades
import cat.descobreix.data.db.FotoEntity
import cat.descobreix.data.db.MissioPropiaEntity
import cat.descobreix.data.db.SacEntity
import cat.descobreix.data.repositori.AjuntamentsRepositoriRoom
import cat.descobreix.data.repositori.FotosRepositoriRoom
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import java.io.File
import java.time.OffsetDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sincronitza Room amb el servidor (requisits.md, secció 9.2). Room és la font principal: primer es baixa el que ha
 * canviat al servidor i s'ajunta amb el mòbil (vegeu [Fusio]); després es puja la cua de canvis del mòbil.
 */
@Singleton
class Sincronitzador @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: BaseDades,
    private val servidor: ServidorSincronitzacio,
    private val preferencies: DataStore<Preferences>,
) {
    private val dao = db.sincronitzacio()
    private val mutex = Mutex()

    /** Retorna fals si no hi ha sessió (no s'ha fet res). Les errades de xarxa es llancen, perquè es torni a provar. */
    suspend fun sincronitza(): Boolean = mutex.withLock {
        val usuari = servidor.usuari() ?: return false
        baixa(usuari)
        puja(usuari)
        baixaContingutAjuntaments()
        true
    }

    // Contingut dels ajuntaments (secció 9.6) ------------------------------------------------------------

    /** Baixa el que han posat els ajuntaments (és comú a tothom) i els segells propis que encara no hi són. */
    private suspend fun baixaContingutAjuntaments() {
        val dao = db.ajuntaments()
        baixaComu(T_AJUNTAMENTS, AjuntamentRemot.serializer(), { it.sincronitzatEl }) { remotes ->
            // Si ha canviat, potser també ha canviat el segell: es torna a baixar.
            for (a in remotes) AjuntamentsRepositoriRoom.fitxerSegell(context, a.codiIne).delete()
            dao.desaAjuntaments(remotes.map { it.local() })
        }
        baixaComu(T_MISSIONS_AJUNTAMENT, MissioAjuntamentRemota.serializer(), { it.sincronitzatEl }) { remotes ->
            dao.desaMissions(remotes.map { it.local() })
        }
        baixaComu(T_AVANTATGES, AvantatgeRemot.serializer(), { it.sincronitzatEl }) { remotes ->
            dao.desaAvantatges(remotes.map { it.local() })
        }
        for (a in dao.ajuntaments().first()) {
            val fitxer = AjuntamentsRepositoriRoom.fitxerSegell(context, a.codiIne)
            if (!a.teSegell || fitxer.exists()) continue
            try {
                val bytes = servidor.baixaFitxerPublic(BUCKET_AJUNTAMENTS, "${a.codiIne}/segell.png")
                withContext(Dispatchers.IO) { fitxer.writeBytes(bytes) }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                // Es tornarà a provar a la propera sincronització; mentrestant, el segell genèric.
                Log.w(TAG, "No s'ha pogut baixar el segell de ${a.codiIne}", e)
            }
        }
    }

    private suspend fun <T> baixaComu(taula: String, serialitzador: KSerializer<T>, sincronitzatEl: (T) -> String?, aplica: suspend (List<T>) -> Unit) {
        val clau = stringPreferencesKey("cursor_$taula")
        var cursor = preferencies.data.first()[clau]?.let { OffsetDateTime.parse(it).toInstant().minusSeconds(MARGE_S).toString() }
        while (true) {
            val pagina = servidor.baixaComu(taula, serialitzador, cursor, PAGINA)
            if (pagina.isEmpty()) break
            aplica(pagina)
            val ultim = pagina.mapNotNull(sincronitzatEl).maxOrNull() ?: break
            preferencies.edit { it[clau] = ultim }
            if (pagina.size < PAGINA || ultim == cursor) break
            cursor = ultim
        }
    }

    // Baixar ------------------------------------------------------------------------------------------

    private suspend fun baixa(usuari: String) {
        baixaTaula(T_DESCOBERTS, DescobertRemot.serializer(), usuari, { it.sincronitzatEl }) { remotes ->
            val r = Fusio.afegibles(db.progres().descobertsAra(), remotes.map { it.local() }, { it.id }, { it.codiIne })
            r.treuLocals.forEach { dao.treu(T_DESCOBERTS, it) }
            aplica {
                if (r.treuLocals.isNotEmpty()) esborra(T_DESCOBERTS, r.treuLocals)
                dao.insereixDescoberts(r.desa)
            }
        }
        baixaTaula(T_COMPLETADES, CompletadaRemota.serializer(), usuari, { it.sincronitzatEl }) { remotes ->
            val r = Fusio.afegibles(db.progres().completadesAra(), remotes.map { it.local() }, { it.id }, { it.missioId })
            r.treuLocals.forEach { dao.treu(T_COMPLETADES, it) }
            aplica {
                if (r.treuLocals.isNotEmpty()) esborra(T_COMPLETADES, r.treuLocals)
                dao.insereixCompletades(r.desa)
            }
        }
        baixaTaula(T_MOVIMENTS, MovimentRemot.serializer(), usuari, { it.sincronitzatEl }) { remotes ->
            aplica { dao.insereixMoviments(remotes.map { it.local() }) }
        }
        baixaTaula(T_SEGELLS, SegellRemot.serializer(), usuari, { it.sincronitzatEl }) { remotes ->
            val r = Fusio.afegibles(db.segells().totsAra(), remotes.map { it.local() }, { it.id }, { it.codiIne })
            r.treuLocals.forEach { dao.treu(T_SEGELLS, it) }
            aplica {
                if (r.treuLocals.isNotEmpty()) esborra(T_SEGELLS, r.treuLocals)
                dao.insereixSegells(r.desa)
            }
        }
        baixaTaula(T_PROPIES, PropiaRemota.serializer(), usuari, { it.sincronitzatEl }) { remotes ->
            val r = Fusio.modificables(dao.totesLesPropies(), remotes.map { it.local() }, { it.id }, { it.id }, MissioPropiaEntity::modificatEl)
            aplica { dao.desaPropies(r.desa) }
        }
        baixaTaula(T_FOTOS, FotoRemota.serializer(), usuari, { it.sincronitzatEl }) { remotes ->
            val r = Fusio.modificables(dao.totesLesFotos(), remotes.map { it.local() }, { it.id }, { it.id }, FotoEntity::modificatEl)
            for (f in r.desa) if (f.esborratEl == null) baixaFitxers(usuari, f)
            aplica { dao.desaFotos(r.desa) }
        }
        baixaTaula(T_SACS, SacRemot.serializer(), usuari, { it.sincronitzatEl }) { remotes ->
            val r = Fusio.modificables(
                dao.totsElsSacs(), remotes.map { it.local() }, { it.id }, { it.origen }, SacEntity::modificatEl,
                // El mateix sac guanyat a dos mòbils: es queda l'id del servidor, i si al mòbil s'havia obert i al servidor no, el que n'ha sortit.
                ajunta = { l, rem -> if (rem.obertEl == null && l.obertEl != null) l.copy(id = rem.id) else rem },
            )
            r.treuLocals.forEach { dao.treu(T_SACS, it) }
            aplica {
                if (r.treuLocals.isNotEmpty()) esborra(T_SACS, r.treuLocals)
                dao.desaSacs(r.desa)
            }
            r.tornaAPujar.forEach { encua(T_SACS, it) }
        }
    }

    /** Baixa una taula per pàgines des de l'últim cursor, i en desa el cursor nou quan s'han aplicat. */
    private suspend fun <T> baixaTaula(taula: String, serialitzador: KSerializer<T>, usuari: String, sincronitzatEl: (T) -> String?, aplica: suspend (List<T>) -> Unit) {
        val clau = stringPreferencesKey("cursor_$taula")
        // Uns minuts enrere: una fila escrita en una transacció que s'ha confirmat tard pot tenir un instant anterior al cursor.
        var cursor = preferencies.data.first()[clau]?.let { OffsetDateTime.parse(it).toInstant().minusSeconds(MARGE_S).toString() }
        while (true) {
            val pagina = servidor.baixa(taula, serialitzador, usuari, cursor, PAGINA)
            if (pagina.isEmpty()) break
            aplica(pagina)
            val ultim = pagina.mapNotNull(sincronitzatEl).maxOrNull() ?: break
            preferencies.edit { it[clau] = ultim }
            if (pagina.size < PAGINA || ultim == cursor) break
            cursor = ultim
        }
    }

    /** Aplica dades del servidor sense que els triggers les posin a la cua per tornar-les a pujar. */
    private suspend fun aplica(bloc: suspend () -> Unit) = db.withTransaction {
        dao.aplicant(true)
        try {
            bloc()
        } finally {
            dao.aplicant(false)
        }
    }

    private fun esborra(taula: String, ids: List<String>) {
        val llocs = ids.joinToString(",") { "?" }
        db.openHelper.writableDatabase.execSQL("DELETE FROM `$taula` WHERE id IN ($llocs)", ids.toTypedArray())
    }

    private fun encua(taula: String, id: String) {
        db.openHelper.writableDatabase.execSQL("INSERT OR IGNORE INTO canvis_pendents (taula, id) VALUES (?, ?)", arrayOf(taula, id))
    }

    private suspend fun baixaFitxers(usuari: String, f: FotoEntity) = withContext(Dispatchers.IO) {
        val carpeta = FotosRepositoriRoom.carpeta(context).apply { mkdirs() }
        val gran = File(carpeta, f.fitxer)
        if (!gran.exists()) gran.writeBytes(servidor.baixaFitxer(rutaFoto(usuari, f.fitxer)))
        val petita = File(File(carpeta, "miniatures").apply { mkdirs() }, f.miniatura)
        if (!petita.exists()) petita.writeBytes(servidor.baixaFitxer(rutaMiniatura(usuari, f.miniatura)))
    }

    // Pujar --------------------------------------------------------------------------------------------

    private suspend fun puja(usuari: String) {
        val perTaula = dao.pendents().groupBy({ it.taula }, { it.id })
        for (taula in TAULES) {
            val ids = perTaula[taula] ?: continue
            for (tros in ids.chunked(PAGINA)) {
                when (taula) {
                    T_DESCOBERTS -> servidor.puja(taula, DescobertRemot.serializer(), dao.descoberts(tros).map { it.remot() }, nomesNoves = true)
                    T_COMPLETADES -> servidor.puja(taula, CompletadaRemota.serializer(), dao.completades(tros).map { it.remot() }, nomesNoves = true)
                    T_MOVIMENTS -> servidor.puja(taula, MovimentRemot.serializer(), dao.moviments(tros).map { it.remot() }, nomesNoves = true)
                    T_SEGELLS -> servidor.puja(taula, SegellRemot.serializer(), dao.segells(tros).map { it.remot() }, nomesNoves = true)
                    T_PROPIES -> servidor.puja(taula, PropiaRemota.serializer(), dao.propies(tros).map { it.remot() }, nomesNoves = false)
                    T_SACS -> servidor.puja(taula, SacRemot.serializer(), dao.sacs(tros).map { it.remot() }, nomesNoves = false)
                    T_FOTOS -> {
                        val fotos = dao.fotos(tros)
                        for (f in fotos) pujaFitxers(usuari, f)
                        servidor.puja(taula, FotoRemota.serializer(), fotos.map { it.remot(usuari) }, nomesNoves = false)
                    }
                }
                tros.forEach { dao.treu(taula, it) }
            }
        }
    }

    private suspend fun pujaFitxers(usuari: String, f: FotoEntity) {
        if (f.esborratEl != null) {
            try {
                servidor.esborraFitxers(listOf(rutaFoto(usuari, f.fitxer), rutaMiniatura(usuari, f.miniatura)))
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                Log.w(TAG, "No s'han pogut esborrar els fitxers de la foto ${f.id}", e)
            }
            return
        }
        val carpeta = FotosRepositoriRoom.carpeta(context)
        val gran = File(carpeta, f.fitxer)
        val petita = File(File(carpeta, "miniatures"), f.miniatura)
        // Si el fitxer ja no hi és (s'ha perdut), es puja la fila igualment: les dades valen més que la imatge.
        if (gran.exists()) servidor.pujaFitxer(rutaFoto(usuari, f.fitxer), withContext(Dispatchers.IO) { gran.readBytes() })
        if (petita.exists()) servidor.pujaFitxer(rutaMiniatura(usuari, f.miniatura), withContext(Dispatchers.IO) { petita.readBytes() })
    }

    companion object {
        private const val TAG = "Sincronitzador"
        private const val PAGINA = 500
        private const val MARGE_S = 120L
        const val T_DESCOBERTS = "municipis_descoberts"
        const val T_COMPLETADES = "missions_completades"
        const val T_MOVIMENTS = "moviments_punts"
        const val T_PROPIES = "missions_propies"
        const val T_FOTOS = "fotos"
        const val T_SEGELLS = "segells"
        const val T_SACS = "sacs"
        const val T_AJUNTAMENTS = "ajuntaments"
        const val T_MISSIONS_AJUNTAMENT = "missions_ajuntament"
        const val T_AVANTATGES = "avantatges"
        private const val BUCKET_AJUNTAMENTS = "descobreix-ajuntaments"
        private val TAULES = listOf(T_DESCOBERTS, T_COMPLETADES, T_MOVIMENTS, T_PROPIES, T_FOTOS, T_SEGELLS, T_SACS)
    }
}
