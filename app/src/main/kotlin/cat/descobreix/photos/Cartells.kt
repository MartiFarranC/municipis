package cat.descobreix.photos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import cat.descobreix.joc.cartell.Cartell
import cat.descobreix.joc.cartell.Requadre
import com.googlecode.tesseract.android.TessBaseAPI
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject
import kotlin.math.max

/** Llegeix el text d'una foto, al mòbil i sense connexió. És una interfície perquè els tests en puguin fer servir una altra. */
interface LectorCartell {
    suspend fun llegeix(foto: Bitmap): String
}

/** Llegeix amb Tesseract i el model de català (assets/tessdata). No envia res enlloc. */
class LectorTesseract @Inject constructor(@ApplicationContext private val context: Context) : LectorCartell {
    private val mutex = Mutex()

    override suspend fun llegeix(foto: Bitmap): String = mutex.withLock {
        withContext(Dispatchers.Default) {
            val carpeta = File(context.filesDir, "ocr")
            val model = File(carpeta, "tessdata/$IDIOMA.traineddata")
            if (!model.exists()) {
                model.parentFile?.mkdirs()
                context.assets.open("tessdata/$IDIOMA.traineddata").use { entrada -> model.outputStream().use { entrada.copyTo(it) } }
            }
            val api = TessBaseAPI()
            try {
                check(api.init(carpeta.absolutePath, IDIOMA)) { "No s'ha pogut carregar el model de text" }
                api.setImage(foto)
                api.getUTF8Text().orEmpty()
            } finally {
                api.recycle()
            }
        }
    }

    private companion object {
        const val IDIOMA = "cat"
    }
}

/**
 * Retalla la foto del cartell: la descodifica (reduïda a [costatMaxim]), la gira dreta i en deixa només la part de
 * dins del [requadre], que l'usuari ha ajustat sobre la vista de la càmera de mida [vistaAmplada] × [vistaAlcada].
 */
fun retallaCartell(jpeg: ByteArray, rotacioGraus: Int, requadre: Requadre, vistaAmplada: Int, vistaAlcada: Int, costatMaxim: Int): Bitmap {
    val mides = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size, mides)
    var mostra = 1
    while (max(mides.outWidth, mides.outHeight) / (mostra * 2) >= costatMaxim) mostra *= 2
    val original = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.size, BitmapFactory.Options().apply { inSampleSize = mostra })
        ?: error("No s'ha pogut descodificar la foto")
    val dreta = if (rotacioGraus == 0) {
        original
    } else {
        Bitmap.createBitmap(original, 0, 0, original.width, original.height, Matrix().apply { postRotate(rotacioGraus.toFloat()) }, true)
            .also { original.recycle() }
    }
    val r = Cartell.retall(requadre, vistaAmplada, vistaAlcada, dreta.width, dreta.height)
    if (r.amplada < 1 || r.alcada < 1) return dreta
    return Bitmap.createBitmap(dreta, r.esquerra, r.dalt, r.amplada, r.alcada).also { if (it !== dreta) dreta.recycle() }
}

fun Bitmap.aJpeg(qualitat: Int): ByteArray = ByteArrayOutputStream().also { compress(Bitmap.CompressFormat.JPEG, qualitat, it) }.toByteArray()
