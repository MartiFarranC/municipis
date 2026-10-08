package cat.descobreix.municipality

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.descobreix.data.repositori.AjuntamentsRepositori
import cat.descobreix.data.repositori.Ajuntament
import cat.descobreix.data.repositori.Avantatge
import cat.descobreix.data.repositori.FotosRepositori
import cat.descobreix.data.repositori.MissionsPropiesRepositori
import cat.descobreix.data.repositori.SegellsRepositori
import cat.descobreix.data.ubicacio.ServeiUbicacio
import cat.descobreix.domain.Foto
import cat.descobreix.domain.Joc
import cat.descobreix.domain.MissioPropia
import cat.descobreix.domain.Progres
import cat.descobreix.joc.geo.Localitzacio
import cat.descobreix.joc.model.CodiIne
import cat.descobreix.joc.model.EstatMunicipi
import cat.descobreix.joc.model.Missio
import cat.descobreix.joc.regles.DisponibilitatOficial
import cat.descobreix.joc.regles.MissioAjuntament
import cat.descobreix.joc.regles.ResultatDesbloqueig
import cat.descobreix.joc.regles.ResultatProva
import cat.descobreix.joc.regles.Ubicacio
import cat.descobreix.passaport.PassaportViewModel
import cat.descobreix.ui.Missatge
import cat.descobreix.ui.missatgeBloquejat
import cat.descobreix.ui.missatgeDe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class FilaMissio(val missio: Missio, val completada: Boolean)

/** Una missió oficial d'un ajuntament: la [missio] per fer-la i si es pot fer avui. */
data class FilaOficial(
    val missio: Missio,
    val origen: MissioAjuntament,
    val completada: Boolean,
    val disponibilitat: DisponibilitatOficial,
)

data class FilaVei(val codi: CodiIne, val nom: String, val estat: EstatMunicipi)

data class MunicipiEstat(
    val carregant: Boolean = true,
    val codi: CodiIne = "",
    val nom: String = "",
    val comarca: String = "",
    val esInici: Boolean = false,
    val estat: EstatMunicipi = EstatMunicipi.BOIRA,
    val missions: List<FilaMissio> = emptyList(),
    val propies: List<MissioPropia> = emptyList(),
    val fotos: List<Foto> = emptyList(),
    val veins: List<FilaVei> = emptyList(),
    val veiDescobert: String? = null,
    val puntsGuanyats: Int = 0,
    val puntsPossibles: Int = 0,
    val bonus: Int = 0,
    val cost: Int = 0,
    val saldo: Int = 0,
    val distancia: Int? = null,
    val provant: String? = null,
    val candidats: List<Pair<CodiIne, String>>? = null,
    val missatge: Missatge? = null,
    val avis: Missatge? = null,
    val desbloquejant: Boolean = false,
    /** Si l'ajuntament col·labora, el que hi ha posat (secció 9.6). */
    val ajuntament: Ajuntament? = null,
    val oficials: List<FilaOficial> = emptyList(),
    val avantatges: List<Avantatge> = emptyList(),
    /** S'acaba de fer el check-in amb el GPS i el municipi encara no té segell: toca posar-lo al passaport. */
    val segellPendent: CodiIne? = null,
) {
    val missionsFetes: Int get() = missions.count { it.completada }
    val portada: Foto? get() = fotos.firstOrNull { it.esPortada } ?: fotos.firstOrNull()
    val potDesbloquejar: Boolean get() = estat == EstatMunicipi.DISPONIBLE && saldo >= cost
}

@HiltViewModel
class MunicipiViewModel @Inject constructor(
    private val joc: Joc,
    private val propies: MissionsPropiesRepositori,
    fotos: FotosRepositori,
    private val ubicacio: ServeiUbicacio,
    private val segells: SegellsRepositori,
    private val ajuntaments: AjuntamentsRepositori,
    estatDesat: SavedStateHandle,
) : ViewModel() {
    val codi: CodiIne = checkNotNull(estatDesat["codi"])

    private val _estat = MutableStateFlow(MunicipiEstat(codi = codi))
    val estat: StateFlow<MunicipiEstat> = _estat.asStateFlow()

    private var progres: Progres = Progres.BUIT

    /** Prova pendent de saber en quin municipi és l'usuari. */
    private var pendent: Triple<Missio, Ubicacio, Localitzacio>? = null

    init {
        viewModelScope.launch {
            val d = joc.dades()
            val m = d.geografia.municipi(codi)
            combine(joc.progres, propies.de(codi), fotos.de(codi)) { p, pr, f -> Triple(p, pr, f) }
                .collect { (p, pr, f) ->
                    progres = p
                    val descoberts = p.conjuntDescoberts
                    val estatM = d.regles.estat(codi, descoberts)
                    _estat.update {
                        it.copy(
                            carregant = false,
                            nom = m.nom,
                            comarca = d.geografia.comarca(m.comarca).nom,
                            esInici = p.inici == codi,
                            estat = estatM,
                            missions = d.missions.de(codi).map { mi -> FilaMissio(mi, mi.id in p.completades) },
                            propies = pr,
                            fotos = f,
                            veins = m.veins.map { v ->
                                FilaVei(v, d.geografia.municipi(v).nom, d.regles.estat(v, descoberts))
                            }.sortedBy { v -> v.nom },
                            veiDescobert = m.veins.firstOrNull { v -> v in descoberts }?.let { v -> d.geografia.municipi(v).nom },
                            puntsGuanyats = d.regles.puntsGuanyatsA(codi, p.completades.keys),
                            puntsPossibles = d.regles.puntsPossibles(codi),
                            bonus = d.config.punts.bonusTotesLesMissions,
                            cost = d.regles.cost(descoberts.size),
                            saldo = p.saldo,
                            distancia = if (estatM == EstatMunicipi.BOIRA) d.regles.graf.distancies(descoberts)[codi] else null,
                        )
                    }
                }
        }
        viewModelScope.launch {
            val d = joc.dades()
            combine(joc.progres, ajuntaments.ajuntaments, ajuntaments.missions, ajuntaments.avantatges) { p, aj, ms, av ->
                val avui = Joc.avui()
                val completades = p.completades.keys
                val oficials = d.ajuntaments.missionsDe(codi, ms, avui, completades).map { m ->
                    FilaOficial(d.ajuntaments.comMissio(m), m, m.idMissio in completades, d.ajuntaments.disponibilitat(m, avui))
                }
                Triple(aj[codi], oficials, av.filter { it.codi == codi && (it.validFins == null || it.validFins >= avui) })
            }.collect { (aj, oficials, av) ->
                _estat.update { it.copy(ajuntament = aj, oficials = oficials, avantatges = av) }
            }
        }
    }

    fun desbloqueja() {
        if (_estat.value.desbloquejant) return
        _estat.update { it.copy(desbloquejant = true) }
        viewModelScope.launch {
            val r = joc.desbloqueja(codi)
            _estat.update {
                it.copy(
                    desbloquejant = false,
                    avis = if (r is ResultatDesbloqueig.Permes) Missatge.Desbloquejat(it.nom) else null,
                    missatge = if (r is ResultatDesbloqueig.Permes) null else joc.missatgeBloquejat(codi),
                )
            }
        }
    }

    fun sensePermis() = _estat.update { it.copy(missatge = Missatge.SensePermisUbicacio) }

    /** Prova amb GPS: es comprova on és l'usuari i, si tot quadra, es completa la missió. */
    fun provaGps(missio: Missio) {
        if (_estat.value.provant != null) return
        _estat.update { it.copy(provant = missio.id) }
        viewModelScope.launch {
            val u = ubicacio.ubicacioActual()
            if (u == null) {
                _estat.update { it.copy(provant = null, missatge = Missatge.SenseUbicacio) }
                return@launch
            }
            val d = joc.dades()
            val loc = withContext(Dispatchers.Default) { d.localitzador.localitza(u.lat, u.lon, u.precisioMetres) }
            valida(missio, u, loc, null)
        }
    }

    fun triaCandidat(municipi: CodiIne) {
        val (missio, u, loc) = pendent ?: return
        pendent = null
        _estat.update { it.copy(candidats = null) }
        viewModelScope.launch { valida(missio, u, loc, municipi) }
    }

    fun tancaCandidats() {
        pendent = null
        _estat.update { it.copy(candidats = null, provant = null) }
    }

    private suspend fun valida(missio: Missio, u: Ubicacio, loc: Localitzacio, triat: CodiIne?) {
        val d = joc.dades()
        val r = d.validador.validaGps(missio, u, loc, progres.conjuntDescoberts, triat)
        when (r) {
            ResultatProva.Valida -> {
                val punts = joc.completaMissio(missio, u, null)
                val segell = missio.clau == PassaportViewModel.CLAU_CHECKIN && segells.segellsAra().none { it.posat.codi == missio.municipi }
                _estat.update {
                    it.copy(provant = null, avis = Missatge.PuntsGuanyats(punts.missio, punts.bonus), segellPendent = if (segell) missio.municipi else it.segellPendent)
                }
            }
            is ResultatProva.CalTriarMunicipi -> {
                pendent = Triple(missio, u, loc)
                _estat.update { it.copy(candidats = r.candidats.map { c -> c to d.geografia.municipi(c).nom }) }
            }
            else -> {
                val m = joc.missatgeDe(r)
                _estat.update { it.copy(provant = null, missatge = m) }
            }
        }
    }

    fun afegeixPropia(titol: String, descripcio: String) {
        if (titol.isBlank()) return
        viewModelScope.launch { propies.afegeix(codi, titol, descripcio) }
    }

    fun canviaPropia(id: String, completada: Boolean) {
        viewModelScope.launch { propies.canviaCompletada(id, completada) }
    }

    fun esborraPropia(id: String) {
        viewModelScope.launch { propies.esborra(id) }
    }

    fun tancaMissatge() = _estat.update { it.copy(missatge = null) }

    fun tancaAvis() = _estat.update { it.copy(avis = null) }

    fun segellObert() = _estat.update { it.copy(segellPendent = null) }
}
