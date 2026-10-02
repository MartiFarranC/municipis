package cat.descobreix.joc

import cat.descobreix.joc.config.ConfiguracioJoc
import cat.descobreix.joc.dades.FormatLimits
import cat.descobreix.joc.dades.FormatMapa
import cat.descobreix.joc.dades.Geografia
import cat.descobreix.joc.dades.Missions
import cat.descobreix.joc.geo.Localitzador
import cat.descobreix.joc.regles.GrafVeins
import cat.descobreix.joc.regles.ReglesJoc
import java.io.File
import java.io.RandomAccessFile

/** Dades reals del repositori: la configuració i els assets generats per als scripts. */
object Repositori {
    private val arrel = File(System.getProperty("arrel.repositori") ?: "..")
    private val assets = File(arrel, "app/src/main/assets/dades")

    fun fitxer(cami: String): File = File(arrel, cami)

    val config: ConfiguracioJoc by lazy { ConfiguracioJoc.llegeix(fitxer("dades/configuracio_joc.json").readText()) }
    val geografia: Geografia by lazy { Geografia.llegeix(File(assets, "municipis.json").readText()) }
    val missions: Missions by lazy { Missions.llegeix(File(assets, "missions.json").readText()) }
    val graf: GrafVeins by lazy { GrafVeins(geografia.municipis) }
    val regles: ReglesJoc by lazy { ReglesJoc(config, graf, missions) }
    val mapa by lazy { File(assets, "mapa.bin").inputStream().use { FormatMapa.llegeix(it) } }

    val localitzador: Localitzador by lazy {
        val fitxer = File(assets, "limits.bin")
        val bytes = fitxer.readBytes()
        val n = FormatLimits.llegeixTotal(bytes.copyOfRange(0, FormatLimits.MIDA_CAPCALERA_FIXA))
        val index = FormatLimits.llegeixIndex(bytes.copyOfRange(0, FormatLimits.midaCapcalera(n)), bytes.size)
        Localitzador(index, geografia.municipis.map { it.codi }, config.gps.margeFronteraMetres) { _, offset, mida ->
            RandomAccessFile(fitxer, "r").use { f ->
                f.seek(offset.toLong())
                ByteArray(mida).also { f.readFully(it) }
            }
        }
    }

    fun codi(nom: String): String = geografia.municipis.single { it.nom == nom }.codi
}
