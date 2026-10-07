package cat.descobreix.data.repositori

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.room.withTransaction
import cat.descobreix.data.assets.FontDadesJoc
import cat.descobreix.data.db.BaseDades
import cat.descobreix.data.db.FotoEntity
import cat.descobreix.domain.Foto
import cat.descobreix.joc.model.CodiIne
import cat.descobreix.joc.model.Visibilitat
import cat.descobreix.joc.regles.Ubicacio
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import kotlin.math.max

class FotosRepositoriRoom @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: BaseDades,
    private val dadesJoc: FontDadesJoc,
    private val rellotge: Rellotge,
) : FotosRepositori {
    private val dao = db.fotos()
    private val carpeta: File get() = File(context.filesDir, CARPETA).apply { mkdirs() }
    private val carpetaMiniatures: File get() = File(carpeta, "miniatures").apply { mkdirs() }

    override val totes: Flow<List<Foto>> = dao.totes().map { l -> l.map { it.aFoto() } }

    override fun de(codi: CodiIne): Flow<List<Foto>> = dao.de(codi).map { l -> l.map { it.aFoto() } }

    override fun foto(id: String): Flow<Foto?> = dao.perId(id).map { it?.aFoto() }

    override suspend fun desa(codi: CodiIne, jpeg: ByteArray, rotacioGraus: Int, ubicacio: Ubicacio?, missioId: String?, esCromo: Boolean): Foto {
        val config = dadesJoc.obte().config.fotos
        val id = uuid()
        val fitxer = "$id.jpg"
        withContext(Dispatchers.IO) {
            val original = descodifica(jpeg, config.costatLlargMaxim)
            val gran = escala(original, config.costatLlargMaxim, rotacioGraus)
            File(carpeta, fitxer).outputStream().use { gran.compress(Bitmap.CompressFormat.JPEG, config.qualitatJpeg, it) }
            val petita = escala(gran, config.costatMiniatura, 0)
            File(carpetaMiniatures, fitxer).outputStream().use { petita.compress(Bitmap.CompressFormat.JPEG, config.qualitatJpeg, it) }
            if (petita !== gran) petita.recycle()
            if (gran !== original) gran.recycle()
            original.recycle()
        }
        val ara = rellotge.ara()
        val entitat = db.withTransaction {
            val esPrimera = dao.totesAra().none { it.codiIne == codi }
            FotoEntity(
                id = id, codiIne = codi, fitxer = fitxer, miniatura = fitxer,
                lat = ubicacio?.lat, lon = ubicacio?.lon, visibilitat = Visibilitat.PRIVADA.name,
                esPortada = esPrimera, missioId = missioId, creatEl = ara, modificatEl = ara, esCromo = esCromo,
            ).also { dao.insereix(it) }
        }
        return entitat.aFoto()
    }

    override suspend fun canviaVisibilitat(id: String, visibilitat: Visibilitat) =
        dao.canviaVisibilitat(id, visibilitat.name, rellotge.ara())

    override suspend fun fesPortada(id: String) {
        val f = dao.perIdAra(id) ?: return
        dao.fesPortada(f.codiIne, id, rellotge.ara())
    }

    override suspend fun esborra(id: String) {
        val f = dao.perIdAra(id) ?: return
        db.withTransaction {
            dao.esborra(id)
            if (f.esPortada) {
                dao.totesAra().firstOrNull { it.codiIne == f.codiIne }?.let { dao.fesPortada(f.codiIne, it.id, rellotge.ara()) }
            }
        }
        withContext(Dispatchers.IO) {
            File(carpeta, f.fitxer).delete()
            File(carpetaMiniatures, f.miniatura).delete()
        }
    }

    private fun FotoEntity.aFoto() = Foto(
        id = id,
        codiIne = codiIne,
        fitxer = File(carpeta, fitxer).absolutePath,
        miniatura = File(carpetaMiniatures, miniatura).absolutePath,
        lat = lat,
        lon = lon,
        visibilitat = Visibilitat.valueOf(visibilitat),
        esPortada = esPortada,
        missioId = missioId,
        creatEl = creatEl,
        esCromo = esCromo,
    )

    /** Descodifica reduint ja la mida, per no carregar en memòria fotos de molts megapíxels. */
    private fun descodifica(jpeg: ByteArray, costatMaxim: Int): Bitmap {
        val mides = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size, mides)
        var mostra = 1
        while (max(mides.outWidth, mides.outHeight) / (mostra * 2) >= costatMaxim) mostra *= 2
        val opcions = BitmapFactory.Options().apply { inSampleSize = mostra }
        return BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size, opcions)
            ?: error("No s'ha pogut descodificar la foto")
    }

    private fun escala(b: Bitmap, costatMaxim: Int, rotacioGraus: Int): Bitmap {
        val llarg = max(b.width, b.height)
        val factor = if (llarg > costatMaxim) costatMaxim.toFloat() / llarg else 1f
        if (factor == 1f && rotacioGraus == 0) return b
        val m = Matrix().apply {
            postScale(factor, factor)
            postRotate(rotacioGraus.toFloat())
        }
        return Bitmap.createBitmap(b, 0, 0, b.width, b.height, m, true)
    }

    companion object {
        const val CARPETA = "fotos"

        fun carpeta(context: Context): File = File(context.filesDir, CARPETA)
    }
}
