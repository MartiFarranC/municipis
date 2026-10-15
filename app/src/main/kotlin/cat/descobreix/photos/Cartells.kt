package cat.descobreix.photos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.util.Log
import cat.descobreix.BuildConfig
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
    /** Llegeix la [foto] de diverses maneres fins que una lectura és bona per a [accepta]. Diu si n'hi ha hagut cap. */
    suspend fun troba(foto: Bitmap, accepta: (String) -> Boolean): Boolean
}

/**
 * Llegeix amb Tesseract i el model de català (assets/tessdata). No envia res enlloc. Una sola lectura falla sovint amb
 * els cartells (lletres molt grosses, la vora vermella, el fons del carrer), així que es prova en grisos a dues mides i
 * amb diverses maneres de trobar el text, i s'atura a la primera que serveix.
 */
class LectorTesseract @Inject constructor(@ApplicationContext private val context: Context) : LectorCartell {
    private val mutex = Mutex()

    override suspend fun troba(foto: Bitmap, accepta: (String) -> Boolean): Boolean = mutex.withLock {
        withContext(Dispatchers.Default) {
            val carpeta = File(context.filesDir, "ocr")
            val model = File(carpeta, "tessdata/$IDIOMA.traineddata")
            if (!model.exists()) {
                model.parentFile?.mkdirs()
                context.assets.open("tessdata/$IDIOMA.traineddata").use { entrada -> model.outputStream().use { entrada.copyTo(it) } }
            }
            val api = TessBaseAPI()
            val imatges = MIDES.map { grisos(foto, it) }
            try {
                check(api.init(carpeta.absolutePath, IDIOMA)) { "No s'ha pogut carregar el model de text" }
                imatges.any { imatge ->
                    MANERES.any { manera ->
                        api.pageSegMode = manera
                        api.setImage(imatge)
                        val text = api.getUTF8Text().orEmpty()
                        val bona = accepta(text)
                        if (BuildConfig.DEBUG) apunta(carpeta, "${imatge.width}×${imatge.height}, manera $manera, ${if (bona) "BONA" else "no"}: $text")
                        bona
                    }
                }
            } finally {
                api.recycle()
                imatges.forEach { if (it !== foto) it.recycle() }
            }
        }
    }

    private companion object {
        const val IDIOMA = "cat"

        /** El costat llarg de cada prova, en píxels: amb lletres molt grosses, la petita va millor. */
        val MIDES = listOf(1600, 800)

        val MANERES = listOf(
            TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK,
            TessBaseAPI.PageSegMode.PSM_SPARSE_TEXT,
            TessBaseAPI.PageSegMode.PSM_AUTO,
        )
    }
}

/**
 * Només a la versió de proves: deixa la lectura al registre i a `files/ocr/lectures.txt` (les últimes), per veure-la
 * després amb `adb shell run-as cat.descobreix cat files/ocr/lectures.txt` encara que el mòbil no estigués connectat.
 */
private fun apunta(carpeta: File, lectura: String) {
    Log.d("Cartell", lectura)
    val fitxer = File(carpeta, "lectures.txt")
    val linies = (if (fitxer.exists()) fitxer.readLines() else emptyList()) + "${java.time.LocalDateTime.now()} $lectura".replace("\n", " ⏎ ")
    fitxer.writeText(linies.takeLast(200).joinToString("\n", postfix = "\n"))
}

/** La [foto] en grisos i reduïda perquè el costat llarg no passi de [costatMaxim]. */
private fun grisos(foto: Bitmap, costatMaxim: Int): Bitmap {
    val escala = minOf(1f, costatMaxim.toFloat() / max(foto.width, foto.height))
    val amplada = (foto.width * escala).toInt().coerceAtLeast(1)
    val alcada = (foto.height * escala).toInt().coerceAtLeast(1)
    val resultat = Bitmap.createBitmap(amplada, alcada, Bitmap.Config.ARGB_8888)
    val pintura = Paint(Paint.FILTER_BITMAP_FLAG).apply { colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) }) }
    Canvas(resultat).drawBitmap(foto, null, Rect(0, 0, amplada, alcada), pintura)
    return resultat
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
