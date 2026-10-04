package cat.descobreix.data.compte

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.core.graphics.scale
import cat.descobreix.data.assets.FontDadesJoc
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min

/**
 * Prepara la foto de perfil: la retalla quadrada pel centre i la redueix a la mida de les
 * miniatures (configuracio_joc.json, `fotos.costatMiniatura`).
 */
class PreparaFotoPerfil @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dadesJoc: FontDadesJoc,
) {
    suspend fun jpeg(uri: Uri): ByteArray {
        val config = dadesJoc.obte().config.fotos
        return withContext(Dispatchers.IO) {
            val original = descodifica(uri, config.costatMiniatura)
            val costat = min(original.width, original.height)
            val quadrada = Bitmap.createBitmap(original, (original.width - costat) / 2, (original.height - costat) / 2, costat, costat)
            val petita = if (costat > config.costatMiniatura) {
                quadrada.scale(config.costatMiniatura, config.costatMiniatura)
            } else {
                quadrada
            }
            val sortida = ByteArrayOutputStream()
            petita.compress(Bitmap.CompressFormat.JPEG, config.qualitatJpeg, sortida)
            sortida.toByteArray()
        }
    }

    /** ImageDecoder ja gira la imatge segons l'EXIF; a Android 8 es fa servir BitmapFactory. */
    private fun descodifica(uri: Uri, midaMinima: Int): Bitmap {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val font = ImageDecoder.createSource(context.contentResolver, uri)
            return ImageDecoder.decodeBitmap(font) { decoder, info, _ ->
                val curt = min(info.size.width, info.size.height)
                if (curt > midaMinima * 2) {
                    val factor = curt / (midaMinima * 2)
                    decoder.setTargetSize(info.size.width / factor, info.size.height / factor)
                }
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        }
        val mides = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, mides) }
        var mostra = 1
        while (min(mides.outWidth, mides.outHeight) / (mostra * 2) >= midaMinima * 2) mostra *= 2
        return context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = max(1, mostra) })
        } ?: throw ExcepcioCompte(ErrorCompte.DESCONEGUT)
    }
}

/**
 * Fotos de perfil del servidor, guardades a la memòria cau del mòbil. Cada foto nova té una ruta
 * diferent, així que una ruta guardada no caduca mai.
 */
@Singleton
class FotosPerfilRemotes @Inject constructor(
    private val supabase: SupabaseClient,
    @ApplicationContext private val context: Context,
) {
    private val carpeta: File get() = File(context.cacheDir, "avatars").apply { mkdirs() }

    /** El fitxer local de la foto, o null si no s'ha pogut baixar. */
    suspend fun fitxer(ruta: String): File? = withContext(Dispatchers.IO) {
        val local = File(carpeta, ruta.replace('/', '_'))
        if (local.exists()) return@withContext local
        runCatching {
            val bytes = supabase.storage.from(BUCKET).downloadAuthenticated(ruta)
            val temporal = File(carpeta, "${local.name}.tmp")
            temporal.writeBytes(bytes)
            temporal.renameTo(local)
            local
        }.getOrNull()
    }

    /** Guarda una foto que s'acaba de pujar, perquè no calgui baixar-la. */
    suspend fun guarda(ruta: String, jpeg: ByteArray) = withContext(Dispatchers.IO) {
        runCatching { File(carpeta, ruta.replace('/', '_')).writeBytes(jpeg) }
        Unit
    }

    private companion object {
        const val BUCKET = "descobreix-avatars"
    }
}
