package cat.descobreix.photos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.descobreix.data.repositori.FotosRepositori
import cat.descobreix.data.ubicacio.ServeiUbicacio
import cat.descobreix.domain.Joc
import cat.descobreix.joc.cartell.Cartell
import cat.descobreix.joc.cartell.Requadre
import cat.descobreix.joc.geo.Localitzacio
import cat.descobreix.joc.model.CodiIne
import cat.descobreix.joc.model.Missio
import cat.descobreix.joc.progressio.Medalles
import cat.descobreix.joc.regles.ResultatProva
import cat.descobreix.joc.regles.Ubicacio
import cat.descobreix.ui.Missatge
import cat.descobreix.ui.missatgeDe
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

data class CameraEstat(
    val nom: String = "",
    val missio: Missio? = null,
    val processant: Boolean = false,
    val candidats: List<Pair<CodiIne, String>>? = null,
    val missatge: Missatge? = null,
    /** Quan la foto s'ha desat: el missatge de confirmació. La pantalla es tanca. */
    val desada: Missatge? = null,
    /** És la foto del cartell: es fa amb el requadre i es comprova que hi diu el nom del municipi. */
    val esCartell: Boolean = false,
)

/** El requadre que l'usuari ha ajustat sobre la vista de la càmera, amb la mida de la vista en píxels. */
data class RequadreVista(val requadre: Requadre, val amplada: Int, val alcada: Int)

/** Fa fotos per a un municipi (i, si n'hi ha, per a una missió amb prova de foto). */
@HiltViewModel
class CameraViewModel @Inject constructor(
    private val joc: Joc,
    private val fotos: FotosRepositori,
    private val ubicacio: ServeiUbicacio,
    private val lector: LectorCartell,
    estatDesat: SavedStateHandle,
) : ViewModel() {
    private val codi: CodiIne = checkNotNull(estatDesat["codi"])
    private val missioId: String? = estatDesat["missio"]

    private val _estat = MutableStateFlow(CameraEstat())
    val estat: StateFlow<CameraEstat> = _estat.asStateFlow()

    private var pendent: Pendent? = null

    private class Pendent(val jpeg: ByteArray, val rotacio: Int, val ubicacio: Ubicacio, val localitzacio: Localitzacio, val requadre: RequadreVista?)

    init {
        viewModelScope.launch {
            val d = joc.dades()
            val missio = missioId?.let { id -> joc.missio(id) }
            _estat.update { it.copy(nom = d.geografia.municipi(codi).nom, missio = missio, esCartell = missio?.clau == Medalles.CLAU_CARTELL) }
        }
    }

    fun error() = _estat.update { it.copy(processant = false, missatge = Missatge.Error) }

    fun sensePermisUbicacio() = _estat.update { it.copy(missatge = Missatge.SensePermisUbicacio) }

    fun comencaCaptura() = _estat.update { it.copy(processant = true) }

    /** La foto s'acaba de fer: es comprova que l'usuari és dins del municipi i es desa. */
    fun fotoFeta(jpeg: ByteArray, rotacio: Int, requadre: RequadreVista? = null) {
        _estat.update { it.copy(processant = true) }
        viewModelScope.launch {
            val u = ubicacio.ubicacioActual()
            if (u == null) {
                _estat.update { it.copy(processant = false, missatge = Missatge.SenseUbicacio) }
                return@launch
            }
            val d = joc.dades()
            val loc = withContext(Dispatchers.Default) { d.localitzador.localitza(u.lat, u.lon, u.precisioMetres) }
            valida(Pendent(jpeg, rotacio, u, loc, requadre), null)
        }
    }

    fun triaCandidat(municipi: CodiIne) {
        val p = pendent ?: return
        pendent = null
        _estat.update { it.copy(candidats = null) }
        viewModelScope.launch { valida(p, municipi) }
    }

    fun tancaCandidats() {
        pendent = null
        _estat.update { it.copy(candidats = null, processant = false) }
    }

    fun tancaMissatge() = _estat.update { it.copy(missatge = null) }

    private suspend fun valida(p: Pendent, triat: CodiIne?) {
        val d = joc.dades()
        val descoberts = joc.progres.first().conjuntDescoberts
        when (val r = d.validador.validaFoto(codi, p.localitzacio, descoberts, triat)) {
            ResultatProva.Valida -> {
                try {
                    val missio = _estat.value.missio
                    val requadre = p.requadre
                    if (_estat.value.esCartell && missio != null && requadre != null) {
                        desaCartell(p, missio, requadre)
                        return
                    }
                    val foto = fotos.desa(codi, p.jpeg, p.rotacio, p.ubicacio, missio?.id)
                    val punts = missio?.let { joc.completaMissio(it, p.ubicacio, foto.id) }
                    if (missio == null) joc.fotoDesada()
                    _estat.update { it.copy(processant = false, desada = Missatge.FotoDesada(punts?.missio ?: 0, punts?.bonus ?: 0)) }
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    error()
                }
            }
            is ResultatProva.CalTriarMunicipi -> {
                pendent = p
                _estat.update { it.copy(candidats = r.candidats.map { c -> c to d.geografia.municipi(c).nom }) }
            }
            else -> {
                val m = joc.missatgeDe(r)
                _estat.update { it.copy(processant = false, missatge = m) }
            }
        }
    }

    /**
     * La foto del cartell: es retalla pel requadre, es comprova que s'hi llegeix el nom del municipi i es desa com a
     * cromo del catàleg. Si la missió ja estava feta (es repeteix la foto), no dona punts.
     */
    private suspend fun desaCartell(p: Pendent, missio: Missio, requadre: RequadreVista) {
        val d = joc.dades()
        val config = d.config.fotos
        val nom = d.geografia.municipi(codi).nom
        val retall = withContext(Dispatchers.Default) {
            retallaCartell(p.jpeg, p.rotacio, requadre.requadre, requadre.amplada, requadre.alcada, config.costatLlargMaxim)
        }
        try {
            if (!Cartell.diuElNom(lector.llegeix(retall), nom)) {
                _estat.update { it.copy(processant = false, missatge = Missatge.CartellNoLlegit(nom)) }
                return
            }
            val jpeg = withContext(Dispatchers.Default) { retall.aJpeg(config.qualitatJpeg) }
            val foto = fotos.desa(codi, jpeg, 0, p.ubicacio, missio.id, esCromo = true)
            val jaFeta = missio.id in joc.progres.first().completades
            val punts = if (jaFeta) null else joc.completaMissio(missio, p.ubicacio, foto.id)
            _estat.update { it.copy(processant = false, desada = if (punts == null) Missatge.CromoNou else Missatge.FotoDesada(punts.missio, punts.bonus)) }
        } finally {
            retall.recycle()
        }
    }
}
