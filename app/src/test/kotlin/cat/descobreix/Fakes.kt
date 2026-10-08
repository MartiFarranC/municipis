package cat.descobreix

import cat.descobreix.data.assets.Dades
import cat.descobreix.data.assets.FontDadesJoc
import cat.descobreix.data.assets.construeixDades
import cat.descobreix.data.repositori.Ajuntament
import cat.descobreix.data.repositori.AjuntamentsRepositori
import cat.descobreix.data.repositori.Avantatge
import cat.descobreix.data.repositori.FotosRepositori
import cat.descobreix.data.repositori.MissionsPropiesRepositori
import cat.descobreix.data.repositori.CameraMapa
import cat.descobreix.data.repositori.PreferenciesRepositori
import cat.descobreix.data.repositori.ProgresRepositori
import cat.descobreix.data.repositori.SegellsRepositori
import cat.descobreix.data.repositori.SacsRepositori
import cat.descobreix.data.repositori.TapesPassaport
import cat.descobreix.data.ubicacio.ServeiUbicacio
import cat.descobreix.domain.ClauObjecte
import cat.descobreix.domain.Foto
import cat.descobreix.domain.MissioCompletada
import cat.descobreix.domain.MissioPropia
import cat.descobreix.domain.Progres
import cat.descobreix.domain.Sac
import cat.descobreix.domain.Segell
import cat.descobreix.joc.dades.FormatLimits
import cat.descobreix.joc.dades.FormatMapa
import cat.descobreix.joc.dades.GeometriaMapa
import cat.descobreix.joc.geo.Localitzador
import cat.descobreix.joc.model.CodiIne
import cat.descobreix.joc.model.Visibilitat
import cat.descobreix.joc.progressio.ContingutSac
import cat.descobreix.joc.progressio.MedallaGuanyada
import cat.descobreix.joc.progressio.SegellPosat
import cat.descobreix.joc.regles.MissioAjuntament
import cat.descobreix.joc.regles.Ubicacio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.RandomAccessFile

/** Dades reals dels assets de l'app, llegides des del disc. */
object DadesDeProva : FontDadesJoc {
    private val carpeta = File("src/main/assets/dades")

    val dades: Dades by lazy {
        val limits = File(carpeta, "limits.bin")
        construeixDades(
            File(carpeta, "configuracio_joc.json").readText(),
            File(carpeta, "municipis.json").readText(),
            File(carpeta, "missions.json").readText(),
        ) { geografia, config ->
            val bytes = limits.readBytes()
            val n = FormatLimits.llegeixTotal(bytes.copyOfRange(0, FormatLimits.MIDA_CAPCALERA_FIXA))
            val index = FormatLimits.llegeixIndex(bytes.copyOfRange(0, FormatLimits.midaCapcalera(n)), bytes.size)
            Localitzador(index, geografia.municipis.map { it.codi }, config.gps.margeFronteraMetres) { _, offset, mida ->
                RandomAccessFile(limits, "r").use { f ->
                    f.seek(offset.toLong())
                    ByteArray(mida).also { f.readFully(it) }
                }
            }
        }
    }

    private val geometria: GeometriaMapa by lazy { File(carpeta, "mapa.bin").inputStream().use { FormatMapa.llegeix(it) } }

    override suspend fun obte(): Dades = dades

    override suspend fun mapa(): GeometriaMapa = geometria

    fun codi(nom: String): CodiIne = dades.geografia.municipis.single { it.nom == nom }.codi

    /** Un punt (lat, lon) ben endins del municipi: el de l'etiqueta del mapa. */
    fun puntDins(nom: String): Pair<Double, Double> {
        val m = geometria.municipis[dades.geografia.index(codi(nom))]
        return geometria.projeccio.lat(m.etiquetaY.toDouble()) to geometria.projeccio.lon(m.etiquetaX.toDouble())
    }
}

class ProgresEnMemoria : ProgresRepositori {
    private val estat = MutableStateFlow(Progres.BUIT)
    override val progres: Flow<Progres> = estat

    override suspend fun progresAra(): Progres = estat.value

    override suspend fun iniciaPartida(codi: CodiIne) {
        check(estat.value.inici == null)
        estat.value = estat.value.copy(inici = codi, descoberts = listOf(codi))
    }

    override suspend fun desbloqueja(codi: CodiIne, cost: Int) {
        val p = estat.value
        check(codi !in p.descoberts && p.saldo >= cost)
        estat.value = p.copy(descoberts = p.descoberts + codi, puntsGastats = p.puntsGastats + cost)
    }

    override suspend fun completaMissio(missioId: String, codi: CodiIne, punts: Int, bonus: Int, ubicacio: Ubicacio?, fotoId: String?) {
        val p = estat.value
        check(missioId !in p.completades)
        estat.value = p.copy(
            completades = p.completades + (missioId to MissioCompletada(missioId, codi, punts, bonus, 0)),
            puntsGuanyats = p.puntsGuanyats + punts + bonus,
        )
    }

    private val medalles = mutableSetOf<String>()

    fun sumaPunts(punts: Int) {
        estat.value = estat.value.copy(puntsGuanyats = estat.value.puntsGuanyats + punts)
    }

    override suspend fun atorgaMedalles(guanyades: List<MedallaGuanyada>): List<MedallaGuanyada> {
        val noves = guanyades.filter { medalles.add(it.id) }
        estat.value = estat.value.copy(puntsGuanyats = estat.value.puntsGuanyats + noves.sumOf { it.punts })
        return noves
    }
}

class MissionsPropiesEnMemoria : MissionsPropiesRepositori {
    private val llista = MutableStateFlow<List<MissioPropia>>(emptyList())

    override fun de(codi: CodiIne): Flow<List<MissioPropia>> = llista.map { l -> l.filter { it.codiIne == codi } }

    override suspend fun afegeix(codi: CodiIne, titol: String, descripcio: String?) {
        llista.value = llista.value + MissioPropia("p${llista.value.size}", codi, titol, descripcio, false, 0)
    }

    override suspend fun canviaCompletada(id: String, completada: Boolean) {
        llista.value = llista.value.map { if (it.id == id) it.copy(completada = completada) else it }
    }

    override suspend fun esborra(id: String) {
        llista.value = llista.value.filterNot { it.id == id }
    }
}

class FotosEnMemoria : FotosRepositori {
    private val llista = MutableStateFlow<List<Foto>>(emptyList())
    override val totes: Flow<List<Foto>> = llista

    override fun de(codi: CodiIne): Flow<List<Foto>> = llista.map { l -> l.filter { it.codiIne == codi } }

    override fun foto(id: String): Flow<Foto?> = llista.map { l -> l.firstOrNull { it.id == id } }

    override suspend fun desa(codi: CodiIne, jpeg: ByteArray, rotacioGraus: Int, ubicacio: Ubicacio?, missioId: String?, esCromo: Boolean): Foto {
        val f = Foto("f${llista.value.size}", codi, "", "", ubicacio?.lat, ubicacio?.lon, Visibilitat.PRIVADA, llista.value.none { it.codiIne == codi }, missioId, 0, esCromo)
        llista.value = llista.value + f
        return f
    }

    override suspend fun canviaVisibilitat(id: String, visibilitat: Visibilitat) {
        llista.value = llista.value.map { if (it.id == id) it.copy(visibilitat = visibilitat) else it }
    }

    override suspend fun fesPortada(id: String) {
        val codi = llista.value.first { it.id == id }.codiIne
        llista.value = llista.value.map { if (it.codiIne == codi) it.copy(esPortada = it.id == id) else it }
    }

    override suspend fun esborra(id: String) {
        llista.value = llista.value.filterNot { it.id == id }
    }
}

class UbicacioFixa(var ubicacio: Ubicacio?) : ServeiUbicacio {
    override fun tePermis(): Boolean = true

    override suspend fun ubicacioActual(): Ubicacio? = ubicacio
}

class SegellsEnMemoria : SegellsRepositori {
    private val llista = MutableStateFlow<List<Segell>>(emptyList())
    override val segells: Flow<List<Segell>> = llista

    override suspend fun segellsAra(): List<Segell> = llista.value

    override suspend fun posa(segell: SegellPosat) {
        check(llista.value.none { it.posat.codi == segell.codi })
        llista.value = llista.value + Segell(segell, 0)
    }
}

class PreferenciesEnMemoria : PreferenciesRepositori {
    private val camera = MutableStateFlow<CameraMapa?>(null)
    private val tapa = MutableStateFlow(TapesPassaport.GRANAT)
    private val color = MutableStateFlow<String?>(null)
    private val animacio = MutableStateFlow<String?>(null)
    override val cameraMapa: Flow<CameraMapa?> = camera
    override suspend fun desaCameraMapa(camera: CameraMapa) {
        this.camera.value = camera
    }

    override val tapaPassaport: Flow<String> = tapa
    override suspend fun desaTapaPassaport(tapa: String) {
        this.tapa.value = tapa
    }

    override val colorApp: Flow<String?> = color
    override suspend fun desaColorApp(color: String?) {
        this.color.value = color
    }

    override val animacioCarrega: Flow<String?> = animacio
    override suspend fun desaAnimacioCarrega(animacio: String?) {
        this.animacio.value = animacio
    }
}

class SacsEnMemoria(private val progres: ProgresEnMemoria) : SacsRepositori {
    private val llista = MutableStateFlow<List<Sac>>(emptyList())
    override val sacs: Flow<List<Sac>> = llista

    override suspend fun sacsAra(): List<Sac> = llista.value

    override suspend fun afegeix(origens: List<String>): List<String> {
        val nous = origens.distinct().filter { o -> llista.value.none { it.origen == o } }
        llista.value = llista.value + nous.map { Sac(it, 0, false, null, null) }
        return nous
    }

    override suspend fun obre(origen: String, contingut: ContingutSac) {
        val sac = checkNotNull(llista.value.firstOrNull { it.origen == origen })
        check(!sac.obert)
        val obert = when (contingut) {
            is ContingutSac.Nou -> sac.copy(obert = true, objecte = ClauObjecte(contingut.objecte.tipus, contingut.objecte.id))
            is ContingutSac.Punts -> sac.copy(obert = true, punts = contingut.punts).also { progres.sumaPunts(contingut.punts) }
        }
        llista.value = llista.value.map { if (it.origen == origen) obert else it }
    }
}


class AjuntamentsEnMemoria(var llista: List<MissioAjuntament> = emptyList()) : AjuntamentsRepositori {
    override val ajuntaments: Flow<Map<CodiIne, Ajuntament>> = flowOf(emptyMap())
    override val missions: Flow<List<MissioAjuntament>> get() = flowOf(llista)
    override val avantatges: Flow<List<Avantatge>> = flowOf(emptyList())

    override suspend fun missionsAra(): List<MissioAjuntament> = llista

    override fun segell(codi: CodiIne): File? = null
}
