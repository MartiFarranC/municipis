package cat.descobreix.map

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.descobreix.data.assets.Dades
import cat.descobreix.data.repositori.CameraMapa as CameraDesada
import cat.descobreix.data.repositori.PreferenciesRepositori
import cat.descobreix.data.ubicacio.ServeiUbicacio
import cat.descobreix.domain.Joc
import cat.descobreix.domain.Progres
import cat.descobreix.joc.dades.GeometriaMapa
import cat.descobreix.joc.geo.Geometria
import cat.descobreix.joc.geo.Localitzacio
import cat.descobreix.joc.model.CodiIne
import cat.descobreix.joc.model.EstatMunicipi
import cat.descobreix.joc.progressio.Nivell
import cat.descobreix.joc.regles.ResultatDesbloqueig
import cat.descobreix.ui.Missatge
import cat.descobreix.ui.missatgeBloquejat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Ordre perquè la càmera es mogui a un punt del mapa. [id] fa que dues ordres iguals es distingeixin. */
data class OrdreCamera(val x: Float, val y: Float, val zoom: Float?, val id: Long)

data class Seleccio(
    val index: Int,
    val codi: CodiIne,
    val nom: String,
    val comarca: String,
    val estat: EstatMunicipi,
    val missions: Int,
    val missionsFetes: Int,
    val cost: Int,
    val potDesbloquejar: Boolean,
    val falten: Int,
    /** Un veí descobert, si el municipi és disponible. */
    val veiDe: String?,
    /** A quants municipis és del territori descobert, si és a la boira. */
    val distancia: Int?,
)

data class OpcioCerca(val index: Int, val nom: String, val comarca: String)

data class MapaEstat(
    val geometria: GeometriaMapa? = null,
    val camins: CaminsMapa? = null,
    val noms: List<String> = emptyList(),
    val estats: List<EstatMunicipi> = emptyList(),
    val inici: Int? = null,
    val descoberts: Int = 0,
    val total: Int = 0,
    val saldo: Int = 0,
    val nivell: Nivell? = null,
    val seleccio: Seleccio? = null,
    val cercant: Boolean = false,
    val textCerca: String = "",
    val resultatsCerca: List<OpcioCerca> = emptyList(),
    val buscantUbicacio: Boolean = false,
    val candidats: List<Pair<CodiIne, String>>? = null,
    val missatge: Missatge? = null,
    val avis: Missatge? = null,
    val ordre: OrdreCamera? = null,
    val cameraInicial: CameraDesada? = null,
    val desbloquejant: Boolean = false,
)

@HiltViewModel
class MapaViewModel @Inject constructor(
    private val joc: Joc,
    private val preferencies: PreferenciesRepositori,
    private val ubicacio: ServeiUbicacio,
    estatDesat: SavedStateHandle,
) : ViewModel() {
    private val _estat = MutableStateFlow(MapaEstat())
    val estat: StateFlow<MapaEstat> = _estat.asStateFlow()

    private val centreInicial: String? = estatDesat["centre"]
    private var dades: Dades? = null
    private var progres: Progres = Progres.BUIT
    private var comptadorOrdres = 0L

    init {
        viewModelScope.launch {
            val d = joc.dades()
            dades = d
            val (geometria, camins) = withContext(Dispatchers.Default) {
                val g = joc.mapa()
                g to CaminsMapa.construeix(g)
            }
            val camera = preferencies.cameraMapa.first()
            _estat.update {
                it.copy(
                    geometria = geometria,
                    camins = camins,
                    noms = d.geografia.municipis.map { m -> m.nom },
                    total = d.geografia.total,
                    cameraInicial = camera,
                )
            }
            centreInicial?.let { centraEn(it, ZOOM_MUNICIPI) }
            joc.progres.collect { p ->
                progres = p
                actualitza()
            }
        }
    }

    private fun actualitza() {
        val d = dades ?: return
        val descoberts = progres.conjuntDescoberts
        val estats = d.geografia.municipis.map { d.regles.estat(it.codi, descoberts) }
        _estat.update {
            it.copy(
                estats = estats,
                inici = progres.inici?.let { c -> d.geografia.index(c) },
                descoberts = descoberts.size,
                saldo = progres.saldo,
                nivell = d.nivells.nivell(progres.puntsGuanyats),
                seleccio = it.seleccio?.let { s -> seleccio(s.index) },
            )
        }
        if (_estat.value.cameraInicial == null && centreInicial == null && _estat.value.ordre == null) {
            progres.inici?.let { centraEn(it, ZOOM_MUNICIPI) }
        }
    }

    private fun seleccio(index: Int): Seleccio? {
        val d = dades ?: return null
        val m = d.geografia.municipis[index]
        val descoberts = progres.conjuntDescoberts
        val estat = d.regles.estat(m.codi, descoberts)
        val missions = d.missions.de(m.codi)
        val cost = d.regles.cost(descoberts.size)
        return Seleccio(
            index = index,
            codi = m.codi,
            nom = m.nom,
            comarca = d.geografia.comarca(m.comarca).nom,
            estat = estat,
            missions = missions.size,
            missionsFetes = missions.count { it.id in progres.completades },
            cost = cost,
            potDesbloquejar = estat == EstatMunicipi.DISPONIBLE && progres.saldo >= cost,
            falten = maxOf(0, cost - progres.saldo),
            veiDe = if (estat == EstatMunicipi.DISPONIBLE) {
                m.veins.firstOrNull { it in descoberts }?.let { d.geografia.municipi(it).nom }
            } else {
                null
            },
            distancia = if (estat == EstatMunicipi.BOIRA) d.regles.graf.distancies(descoberts)[m.codi] else null,
        )
    }

    /** Toc al mapa, en coordenades del mapa. */
    fun toca(x: Float, y: Float) {
        val g = _estat.value.geometria ?: return
        val index = g.municipis.indices.firstOrNull { i ->
            val m = g.municipis[i]
            m.requadre.conte(x.toDouble(), y.toDouble()) &&
                m.detall.any { Geometria.dinsPoligon(x.toDouble(), y.toDouble(), it.anells) }
        }
        _estat.update { it.copy(seleccio = index?.let(::seleccio)) }
    }

    fun treuSeleccio() = _estat.update { it.copy(seleccio = null) }

    fun obreCerca(obert: Boolean) = _estat.update { it.copy(cercant = obert, textCerca = "", resultatsCerca = emptyList()) }

    fun canviaCerca(text: String) {
        val d = dades ?: return
        val resultats = d.cercador.cerca(text, LIMIT_CERCA).map {
            OpcioCerca(d.geografia.index(it.codi), it.nom, d.geografia.comarca(it.comarca).nom)
        }
        _estat.update { it.copy(textCerca = text, resultatsCerca = resultats) }
    }

    fun triaResultat(opcio: OpcioCerca) {
        val d = dades ?: return
        _estat.update { it.copy(cercant = false, seleccio = seleccio(opcio.index)) }
        centraEn(d.geografia.municipis[opcio.index].codi, ZOOM_MUNICIPI)
    }

    /** Torna al municipi d'inici. */
    fun centraAlTerritori() {
        progres.inici?.let { centraEn(it, ZOOM_MUNICIPI / 2) }
    }

    fun desbloqueja() {
        val s = _estat.value.seleccio ?: return
        if (_estat.value.desbloquejant) return
        _estat.update { it.copy(desbloquejant = true) }
        viewModelScope.launch {
            val r = joc.desbloqueja(s.codi)
            _estat.update {
                it.copy(
                    desbloquejant = false,
                    avis = if (r is ResultatDesbloqueig.Permes) Missatge.Desbloquejat(s.nom) else null,
                    missatge = if (r is ResultatDesbloqueig.Permes) null else joc.missatgeBloquejat(s.codi),
                )
            }
        }
    }

    fun sensePermis() = _estat.update { it.copy(missatge = Missatge.SensePermisUbicacio) }

    /** "On sóc?": localitza l'usuari i, si és en un municipi bloquejat, li explica què li falta. */
    fun onSoc() {
        if (_estat.value.buscantUbicacio) return
        _estat.update { it.copy(buscantUbicacio = true) }
        viewModelScope.launch {
            val u = ubicacio.ubicacioActual()
            val d = dades
            if (u == null || d == null) {
                _estat.update { it.copy(buscantUbicacio = false, missatge = Missatge.SenseUbicacio) }
                return@launch
            }
            val loc = withContext(Dispatchers.Default) { d.localitzador.localitza(u.lat, u.lon, u.precisioMetres) }
            _estat.update { it.copy(buscantUbicacio = false) }
            when (loc) {
                is Localitzacio.Dins -> mostraUbicacio(loc.codi)
                is Localitzacio.Dubte -> _estat.update {
                    it.copy(candidats = loc.candidats.map { c -> c to d.geografia.municipi(c).nom })
                }
                Localitzacio.Fora -> _estat.update { it.copy(missatge = Missatge.ForaDeCatalunya) }
            }
            val g = _estat.value.geometria ?: return@launch
            if (loc !is Localitzacio.Fora) {
                ordena(g.projeccio.x(u.lon).toFloat(), g.projeccio.y(u.lat).toFloat(), ZOOM_MUNICIPI)
            }
        }
    }

    fun triaCandidat(codi: CodiIne) {
        _estat.update { it.copy(candidats = null) }
        viewModelScope.launch { mostraUbicacio(codi) }
    }

    fun tancaCandidats() = _estat.update { it.copy(candidats = null) }

    private suspend fun mostraUbicacio(codi: CodiIne) {
        val d = dades ?: return
        _estat.update { it.copy(seleccio = seleccio(d.geografia.index(codi))) }
        if (codi !in progres.conjuntDescoberts) {
            val m = joc.missatgeBloquejat(codi)
            _estat.update { it.copy(missatge = m) }
        }
    }

    fun tancaMissatge() = _estat.update { it.copy(missatge = null) }

    fun tancaAvis() = _estat.update { it.copy(avis = null) }

    fun desaCamera(x: Float, y: Float, escala: Float) {
        viewModelScope.launch { preferencies.desaCameraMapa(CameraDesada(x, y, escala)) }
    }

    private fun centraEn(codi: CodiIne, zoom: Float) {
        val d = dades ?: return
        val g = _estat.value.geometria ?: return
        val m = g.municipis[d.geografia.index(codi)]
        ordena(m.etiquetaX.toFloat(), m.etiquetaY.toFloat(), zoom)
    }

    private fun ordena(x: Float, y: Float, zoom: Float?) {
        _estat.update { it.copy(ordre = OrdreCamera(x, y, zoom, ++comptadorOrdres)) }
    }

    private companion object {
        const val ZOOM_MUNICIPI = 12f
        const val LIMIT_CERCA = 8
    }
}
