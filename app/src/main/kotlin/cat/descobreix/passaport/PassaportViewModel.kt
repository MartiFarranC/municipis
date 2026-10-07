package cat.descobreix.passaport

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.descobreix.data.repositori.PreferenciesRepositori
import cat.descobreix.data.repositori.SegellsRepositori
import cat.descobreix.data.repositori.TapaPassaport
import cat.descobreix.domain.Joc
import cat.descobreix.domain.Segell
import cat.descobreix.joc.model.CodiIne
import cat.descobreix.joc.progressio.Passaport
import cat.descobreix.joc.progressio.SegellPosat
import cat.descobreix.ui.components.AMPLADA_SEGELL
import cat.descobreix.ui.components.PROPORCIO_PAGINA
import cat.descobreix.ui.components.PROPORCIO_SEGELL
import cat.descobreix.ui.components.SegellDibuix
import cat.descobreix.ui.components.Silueta
import cat.descobreix.ui.components.siluetaMunicipi
import cat.descobreix.ui.components.siluetesComarques
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.random.Random

/** La data d'un segell, com la d'un segell de veritat: 07·10·2026. */
fun dataSegell(ms: Long): String = SimpleDateFormat("dd·MM·yyyy", Locale.ROOT).format(Date(ms))

/** Una pàgina del passaport d'una comarca. */
data class PaginaComarca(val comarca: String, val nom: String, val numero: Int, val segells: List<SegellDibuix>, val recompte: Int, val total: Int)

/** Un municipi amb el check-in fet però sense segell (per exemple, de quan encara no hi havia passaport). */
data class Pendent(val codi: CodiIne, val nom: String)

data class PassaportEstat(
    val carregant: Boolean = true,
    val tapa: TapaPassaport = TapaPassaport.GRANAT,
    val pagines: List<PaginaComarca> = emptyList(),
    val pendents: List<Pendent> = emptyList(),
    val siluetesComarques: Map<String, Silueta> = emptyMap(),
    val siluetaCatalunya: List<Silueta> = emptyList(),
    val segells: Int = 0,
    val total: Int = 0,
)

/** El passaport sencer: la tapa, les pàgines de cada comarca i els segells que falten per posar. */
@HiltViewModel
class PassaportViewModel @Inject constructor(
    private val joc: Joc,
    segells: SegellsRepositori,
    private val preferencies: PreferenciesRepositori,
) : ViewModel() {
    private val _estat = MutableStateFlow(PassaportEstat())
    val estat: StateFlow<PassaportEstat> = _estat.asStateFlow()

    init {
        viewModelScope.launch {
            val d = joc.dades()
            val mapa = joc.mapa()
            val perComarca = withContext(Dispatchers.Default) { siluetesComarques(mapa, d.geografia) }
            val index = d.geografia.municipis.withIndex().associate { (i, m) -> m.codi to i }
            // Les siluetes dels municipis es fan a mesura que calen (només les dels que tenen segell).
            val siluetes = mutableMapOf<CodiIne, Silueta>()
            val noms = d.geografia.comarques.associate { it.codi to it.nom }
            val totals = d.geografia.municipis.groupingBy { it.comarca }.eachCount()
            combine(segells.segells, joc.progres, preferencies.tapaPassaport) { s, p, t -> Triple(s, p, t) }.collect { (s, p, t) ->
                val ambSegell = s.map { it.posat.codi }.toSet()
                val falten = ambSegell - siluetes.keys
                if (falten.isNotEmpty()) {
                    siluetes += withContext(Dispatchers.Default) { falten.associateWith { siluetaMunicipi(mapa, index.getValue(it)) } }
                }
                val checkins = p.completades.values
                    .filter { d.missions.missio(it.missioId)?.clau == CLAU_CHECKIN }
                    .map { it.codiIne }
                    .filter { it !in ambSegell }
                    .distinct()
                val pagines = s.groupBy { it.posat.comarca }.toSortedMap(compareBy { noms[it].orEmpty() }).flatMap { (comarca, ss) ->
                    ss.groupBy { it.posat.pagina }.toSortedMap().map { (pagina, sp) ->
                        PaginaComarca(comarca, noms[comarca].orEmpty(), pagina + 1, sp.map { dibuix(it, d.geografia.municipi(it.posat.codi).nom, siluetes) }, ss.size, totals[comarca] ?: 0)
                    }
                }
                _estat.update {
                    it.copy(
                        carregant = false,
                        tapa = t,
                        pagines = pagines,
                        pendents = checkins.map { c -> Pendent(c, d.geografia.municipi(c).nom) },
                        siluetesComarques = perComarca,
                        siluetaCatalunya = perComarca.values.toList(),
                        segells = s.size,
                        total = d.geografia.total,
                    )
                }
            }
        }
    }

    fun triaTapa(t: TapaPassaport) {
        viewModelScope.launch { preferencies.desaTapaPassaport(t) }
    }

    companion object {
        /** Clau de la missió genèrica de check-in: en fer-la amb el GPS, el municipi guanya el seu segell. */
        const val CLAU_CHECKIN = "checkin"
    }
}

internal fun dibuix(s: Segell, nom: String, siluetes: Map<CodiIne, Silueta>) = SegellDibuix(s.posat, nom, dataSegell(s.creatEl), siluetes[s.posat.codi])

enum class FaseSegellar {
    /** La tapa s'està obrint. */
    OBRINT,

    /** Esperant que l'usuari toqui on vol el segell. */
    TRIANT,

    /** El segell ja és a la pàgina: l'usuari l'accepta o el repeteix. */
    DECIDINT,

    /** Desat. */
    FET,
}

data class SegellarEstat(
    val carregant: Boolean = true,
    val codi: CodiIne = "",
    val nom: String = "",
    val comarca: String = "",
    val nomComarca: String = "",
    val tapa: TapaPassaport = TapaPassaport.GRANAT,
    val pagina: Int = 0,
    val total: Int = 0,
    /** Segells que ja són a la pàgina on va el nou. */
    val segells: List<SegellDibuix> = emptyList(),
    val segellsComarca: Int = 0,
    val siluetaComarca: Silueta? = null,
    val siluetaMunicipi: Silueta? = null,
    val siluetaCatalunya: List<Silueta> = emptyList(),
    /** El segell que l'usuari acaba de posar i encara no ha acceptat. */
    val provisional: SegellPosat? = null,
    val fase: FaseSegellar = FaseSegellar.OBRINT,
    /** El municipi ja tenia segell: no se'n pot posar un altre. */
    val jaTeSegell: Boolean = false,
)

/** Posar el segell d'un municipi: el passaport s'obre, l'usuari toca on el vol i l'accepta o el repeteix. */
@HiltViewModel
class SegellarViewModel @Inject constructor(
    private val joc: Joc,
    private val segells: SegellsRepositori,
    private val preferencies: PreferenciesRepositori,
    estatDesat: SavedStateHandle,
) : ViewModel() {
    val codi: CodiIne = checkNotNull(estatDesat["codi"])

    private val _estat = MutableStateFlow(SegellarEstat(codi = codi))
    val estat: StateFlow<SegellarEstat> = _estat.asStateFlow()

    private val atzar = Random(System.nanoTime())
    private lateinit var passaport: Passaport

    init {
        viewModelScope.launch {
            val d = joc.dades()
            passaport = Passaport(d.config.passaport)
            val mapa = joc.mapa()
            val municipi = d.geografia.municipi(codi)
            val tots = segells.segellsAra()
            val deComarca = tots.filter { it.posat.comarca == municipi.comarca }
            val pagina = passaport.paginaNova(deComarca.size)
            val (siluetes, comarques) = withContext(Dispatchers.Default) {
                val index = d.geografia.municipis.withIndex().associate { (i, m) -> m.codi to i }
                val necessaris = deComarca.filter { it.posat.pagina == pagina }.map { it.posat.codi } + codi
                necessaris.associateWith { siluetaMunicipi(mapa, index.getValue(it)) } to siluetesComarques(mapa, d.geografia)
            }
            _estat.update {
                it.copy(
                    carregant = false,
                    nom = municipi.nom,
                    comarca = municipi.comarca,
                    nomComarca = d.geografia.comarques.first { c -> c.codi == municipi.comarca }.nom,
                    tapa = preferencies.tapaPassaport.first(),
                    pagina = pagina,
                    total = d.geografia.municipisDeComarca(municipi.comarca).size,
                    segells = deComarca.filter { s -> s.posat.pagina == pagina }.map { s -> dibuix(s, d.geografia.municipi(s.posat.codi).nom, siluetes) },
                    segellsComarca = deComarca.size,
                    siluetaComarca = comarques[municipi.comarca],
                    siluetaMunicipi = siluetes[codi],
                    siluetaCatalunya = comarques.values.toList(),
                    jaTeSegell = tots.any { s -> s.posat.codi == codi },
                )
            }
        }
    }

    fun obert() = _estat.update { if (it.fase == FaseSegellar.OBRINT) it.copy(fase = FaseSegellar.TRIANT) else it }

    /** L'usuari ha tocat la pàgina a ([x], [y]), en fracció de l'amplada i l'alçada. */
    fun toca(x: Float, y: Float) {
        val e = _estat.value
        if (e.fase != FaseSegellar.TRIANT || e.jaTeSegell || e.carregant) return
        val alcadaSegell = AMPLADA_SEGELL * PROPORCIO_SEGELL * PROPORCIO_PAGINA
        val (lx, ly) = passaport.limita(x, y, AMPLADA_SEGELL, alcadaSegell)
        val (gir, tinta) = passaport.estil(atzar)
        _estat.update { it.copy(provisional = SegellPosat(codi, e.comarca, e.pagina, lx, ly, gir, tinta), fase = FaseSegellar.DECIDINT) }
    }

    fun repeteix() = _estat.update { it.copy(provisional = null, fase = FaseSegellar.TRIANT) }

    fun accepta() {
        val s = _estat.value.provisional ?: return
        viewModelScope.launch {
            segells.posa(s)
            _estat.update { it.copy(fase = FaseSegellar.FET) }
        }
    }
}
