package cat.descobreix.data.assets

import android.content.Context
import android.content.res.AssetManager
import cat.descobreix.joc.cerca.Cercador
import cat.descobreix.joc.config.ConfiguracioJoc
import cat.descobreix.joc.dades.FormatLimits
import cat.descobreix.joc.dades.FormatMapa
import cat.descobreix.joc.dades.Geografia
import cat.descobreix.joc.dades.GeometriaMapa
import cat.descobreix.joc.dades.Missions
import cat.descobreix.joc.geo.Localitzador
import cat.descobreix.joc.progressio.Assoliments
import cat.descobreix.joc.progressio.Nivells
import cat.descobreix.joc.regles.GrafVeins
import cat.descobreix.joc.regles.ReglesJoc
import cat.descobreix.joc.regles.ValidadorProves
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import java.io.DataInputStream
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/** Dades estàtiques del joc, generades pels scripts i incloses als assets. */
class Dades(
    val config: ConfiguracioJoc,
    val geografia: Geografia,
    val missions: Missions,
    val regles: ReglesJoc,
    val validador: ValidadorProves,
    val cercador: Cercador,
    val nivells: Nivells,
    val assoliments: Assoliments,
    val localitzador: Localitzador,
)

/** D'on surten les dades estàtiques. És una interfície perquè els tests en puguin fer servir d'altres. */
interface FontDadesJoc {
    suspend fun obte(): Dades

    /** La geometria del mapa és gran: només es carrega quan es mostra el mapa. */
    suspend fun mapa(): GeometriaMapa
}

/** Construeix les dades a partir del contingut dels fitxers generats pels scripts. */
fun construeixDades(config: String, municipis: String, missions: String, localitzador: (Geografia, ConfiguracioJoc) -> Localitzador): Dades {
    val c = ConfiguracioJoc.llegeix(config)
    val geografia = Geografia.llegeix(municipis)
    val m = Missions.llegeix(missions)
    val graf = GrafVeins(geografia.municipis)
    return Dades(
        config = c,
        geografia = geografia,
        missions = m,
        regles = ReglesJoc(c, graf, m),
        validador = ValidadorProves(c),
        cercador = Cercador(geografia.municipis),
        nivells = Nivells(c.nivells),
        assoliments = Assoliments(c.assoliments, geografia),
        localitzador = localitzador(geografia, c),
    )
}

/** Carrega els assets una sola vegada, fora del fil principal. */
@Singleton
class DadesJoc @Inject constructor(@ApplicationContext private val context: Context) : FontDadesJoc {
    private val ambit = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val assets: AssetManager get() = context.assets

    private val dades: Deferred<Dades> = ambit.async(start = CoroutineStart.LAZY) { carrega() }
    private val mapa: Deferred<GeometriaMapa> = ambit.async(start = CoroutineStart.LAZY) {
        assets.open(MAPA).use { FormatMapa.llegeix(it) }
    }

    override suspend fun obte(): Dades = dades.await()

    override suspend fun mapa(): GeometriaMapa = mapa.await()

    private fun text(cami: String): String = assets.open(cami).bufferedReader().use { it.readText() }

    private fun carrega(): Dades = construeixDades(text(CONFIG), text(MUNICIPIS), text(MISSIONS), ::creaLocalitzador)

    private fun creaLocalitzador(geografia: Geografia, config: ConfiguracioJoc): Localitzador {
        val midaFitxer = midaAsset(LIMITS)
        val capcalera = assets.open(LIMITS).use { entrada ->
            val fixa = llegeix(entrada, FormatLimits.MIDA_CAPCALERA_FIXA)
            val n = FormatLimits.llegeixTotal(fixa)
            fixa + llegeix(entrada, FormatLimits.midaCapcalera(n) - FormatLimits.MIDA_CAPCALERA_FIXA)
        }
        val index = FormatLimits.llegeixIndex(capcalera, midaFitxer)
        return Localitzador(index, geografia.municipis.map { it.codi }, config.gps.margeFronteraMetres) { _, offset, mida ->
            assets.open(LIMITS).use { entrada ->
                salta(entrada, offset.toLong())
                llegeix(entrada, mida)
            }
        }
    }

    private fun midaAsset(cami: String): Int = try {
        assets.openFd(cami).use { it.length.toInt() }
    } catch (e: IOException) {
        // Si l'asset està comprimit no es pot obrir com a descriptor: es compta llegint-lo.
        assets.open(cami).use { entrada ->
            var total = 0
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val n = entrada.read(buffer)
                if (n < 0) break
                total += n
            }
            total
        }
    }

    private fun llegeix(entrada: InputStream, mida: Int): ByteArray =
        ByteArray(mida).also { DataInputStream(entrada).readFully(it) }

    private fun salta(entrada: InputStream, bytes: Long) {
        var falten = bytes
        while (falten > 0) {
            val saltats = entrada.skip(falten)
            if (saltats <= 0) {
                if (entrada.read() < 0) throw IOException("Fi inesperat de $LIMITS")
                falten--
            } else {
                falten -= saltats
            }
        }
    }

    private companion object {
        const val CONFIG = "dades/configuracio_joc.json"
        const val MUNICIPIS = "dades/municipis.json"
        const val MISSIONS = "dades/missions.json"
        const val MAPA = "dades/mapa.bin"
        const val LIMITS = "dades/limits.bin"
    }
}
