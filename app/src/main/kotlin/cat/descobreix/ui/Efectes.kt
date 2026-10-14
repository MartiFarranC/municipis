package cat.descobreix.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.staticCompositionLocalOf
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sin
import kotlin.random.Random

/**
 * Els sons i les vibracions de l'app (docs/disseny.md). Els sons no són fitxers: es fabriquen aquí, amb ones i soroll,
 * com al prototip. Es poden apagar des del perfil («So i vibració»).
 */
class Efectes(context: Context) {
    enum class So { TOC, XIM, GARGOT, SEGELL, MONEDA, SACSEIG, OBRE, CROMO }

    /** Si fa sons i vibra. El posa MainActivity a partir de les preferències. */
    @Volatile
    var actiu: Boolean = true

    private val vibrador: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        context.getSystemService(Vibrator::class.java)
    }
    private val fils = Executors.newCachedThreadPool()
    private val mostres = HashMap<So, ShortArray>()

    fun fes(so: So) {
        if (!actiu) return
        vibra(VIBRACIONS.getValue(so))
        fils.execute { sona(so) }
    }

    private fun vibra(patro: LongArray) {
        val v = vibrador ?: return
        if (!v.hasVibrator()) return
        runCatching {
            if (patro.size == 1) {
                v.vibrate(VibrationEffect.createOneShot(patro[0], VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                v.vibrate(VibrationEffect.createWaveform(longArrayOf(0) + patro, -1))
            }
        }
    }

    private fun sona(so: So) {
        val dades = synchronized(mostres) { mostres.getOrPut(so) { fabrica(so) } }
        runCatching {
            val pista = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(MOSTREIG)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                )
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(dades.size * 2)
                .build()
            pista.write(dades, 0, dades.size)
            pista.play()
            Thread.sleep(dades.size * 1000L / MOSTREIG + 60)
            pista.release()
        }
    }

    companion object {
        private const val MOSTREIG = 44100

        private val VIBRACIONS = mapOf(
            So.TOC to longArrayOf(8),
            So.XIM to longArrayOf(15, 40, 25),
            So.GARGOT to longArrayOf(12),
            So.SEGELL to longArrayOf(35),
            So.MONEDA to longArrayOf(6),
            So.SACSEIG to longArrayOf(25),
            So.OBRE to longArrayOf(40),
            So.CROMO to longArrayOf(20, 40, 30),
        )

        /** Fabrica el so (mostres de 16 bits) a partir d'ones i soroll, com els sons del prototip. */
        internal fun fabrica(so: So): ShortArray {
            val b = Buffer()
            when (so) {
                So.TOC -> b.to(240.0, 0.0, .07, 110.0, .3)
                So.XIM -> {
                    b.to(660.0, 0.0, .14, null, .22, triangular = true)
                    b.to(990.0, .11, .22, null, .2, triangular = true)
                    b.to(1320.0, .22, .3, null, .12, triangular = true)
                }
                So.GARGOT -> b.soroll(0.0, .22, .25, aguts = true)
                So.SEGELL -> {
                    b.to(95.0, 0.0, .16, 55.0, .5)
                    b.soroll(0.0, .05, .2, aguts = false)
                }
                So.MONEDA -> {
                    b.to(1200.0, 0.0, .06, null, .08)
                    b.to(1600.0, .06, .1, null, .08)
                }
                So.SACSEIG -> b.soroll(0.0, .12, .18, aguts = false)
                So.OBRE -> b.soroll(0.0, .25, .3, aguts = false)
                So.CROMO -> {
                    b.to(660.0, 0.0, .12, null, .2, triangular = true)
                    b.to(990.0, .1, .18, null, .18, triangular = true)
                }
            }
            return b.mostres()
        }
    }

    /** Un tros de so que es va omplint. */
    internal class Buffer {
        private var dades = DoubleArray(0)

        private fun allarga(fins: Int) {
            if (fins > dades.size) dades = dades.copyOf(fins)
        }

        /** Una nota de freqüència f (o que llisca fins a f2) que s'apaga de pressa. */
        fun to(f: Double, inici: Double, durada: Double, f2: Double?, volum: Double, triangular: Boolean = false) {
            val i0 = (inici * MOSTREIG).toInt()
            val n = (durada * MOSTREIG).toInt()
            allarga(i0 + n)
            var fase = 0.0
            for (k in 0 until n) {
                val t = k.toDouble() / n
                val freq = if (f2 == null) f else f * exp(ln(f2 / f) * t)
                fase += 2 * PI * freq / MOSTREIG
                val ona = if (triangular) 2 / PI * kotlin.math.asin(sin(fase)) else sin(fase)
                dades[i0 + k] += ona * volum * exp(-5.0 * t)
            }
        }

        /** Soroll: aguts (paper que es ratlla) o greus (roba, un cop). */
        fun soroll(inici: Double, durada: Double, volum: Double, aguts: Boolean) {
            val i0 = (inici * MOSTREIG).toInt()
            val n = (durada * MOSTREIG).toInt()
            allarga(i0 + n)
            val r = Random(7)
            var ant = 0.0
            for (k in 0 until n) {
                val t = k.toDouble() / n
                val blanc = r.nextDouble() * 2 - 1
                // Un filtre d'un pol: passa-baixos per als greus; la diferència, per als aguts.
                ant += (blanc - ant) * (if (aguts) .9 else .08)
                val v = if (aguts) blanc - ant else ant * 3
                val envolupant = if (t < .1) t / .1 else 1 - (t - .1) / .9
                dades[i0 + k] += v * volum * envolupant
            }
        }

        fun mostres(): ShortArray = ShortArray(dades.size) { (dades[it].coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort() }
    }
}

/** Els efectes de l'app; null a les previsualitzacions i als tests, on no sona res. */
val LocalEfectes = staticCompositionLocalOf<Efectes?> { null }
