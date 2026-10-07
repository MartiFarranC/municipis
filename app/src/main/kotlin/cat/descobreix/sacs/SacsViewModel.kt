package cat.descobreix.sacs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.descobreix.data.repositori.PreferenciesRepositori
import cat.descobreix.data.repositori.TapesPassaport
import cat.descobreix.domain.Joc
import cat.descobreix.joc.config.ConfiguracioJoc.Objecte
import cat.descobreix.joc.config.ConfiguracioJoc.TipusObjecte
import cat.descobreix.joc.progressio.ContingutSac
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** El que ha sortit del sac que s'acaba d'obrir. */
sealed interface Sortit {
    data class Nou(val objecte: Objecte) : Sortit

    data class Punts(val punts: Int) : Sortit
}

data class SacsEstat(
    val carregant: Boolean = true,
    /** Els sacs per obrir (els seus orígens), del més antic al més nou. */
    val perObrir: List<String> = emptyList(),
    /** Tot el que es pot tenir, en l'ordre de la configuració. */
    val tot: List<Objecte> = emptyList(),
    /** El que es té. */
    val tinc: Set<Objecte> = emptySet(),
    /** Quantes coses hi ha als sacs i quantes n'han sortit. */
    val delsSacs: Int = 0,
    val sortides: Int = 0,
    val color: String? = null,
    val animacio: String? = null,
    val tapa: String = TapesPassaport.GRANAT,
    /** El sac que s'està obrint: null si no se n'obre cap. */
    val obrint: String? = null,
    /** El que n'ha sortit, quan ja s'ha obert. */
    val sortit: Sortit? = null,
)

/** Els sacs per obrir i la col·lecció: el que es té i el que es fa servir (el color, l'animació i la tapa). */
@HiltViewModel
class SacsViewModel @Inject constructor(
    private val joc: Joc,
    private val preferencies: PreferenciesRepositori,
) : ViewModel() {
    private val _estat = MutableStateFlow(SacsEstat())
    val estat: StateFlow<SacsEstat> = _estat.asStateFlow()

    init {
        viewModelScope.launch {
            val s = joc.dades().sacs
            val tot = s.inicials + s.delsSacs
            val prefs = combine(preferencies.colorApp, preferencies.animacioCarrega, preferencies.tapaPassaport) { c, a, t -> Triple(c, a, t) }
            combine(joc.sacs, joc.colleccio, prefs) { sacs, tinc, p -> Triple(sacs, tinc, p) }.collect { (sacs, tinc, p) ->
                val tincSet = tinc.toSet()
                _estat.update {
                    it.copy(
                        carregant = false,
                        perObrir = sacs.filter { sac -> !sac.obert }.map { sac -> sac.origen },
                        tot = tot,
                        tinc = tincSet,
                        delsSacs = s.delsSacs.size,
                        sortides = s.delsSacs.count { o -> o in tincSet },
                        color = p.first,
                        animacio = p.second,
                        tapa = p.third,
                    )
                }
            }
        }
    }

    /** Comença a obrir el primer sac que hi ha per obrir: es mostra tancat fins que l'usuari el toca. */
    fun preparaSac() = _estat.update { e -> if (e.obrint == null) e.copy(obrint = e.perObrir.firstOrNull(), sortit = null) else e }

    /** L'usuari ha tocat el sac: s'obre i es desa el que n'ha sortit. */
    fun obre() {
        val origen = _estat.value.obrint ?: return
        if (_estat.value.sortit != null) return
        viewModelScope.launch {
            val s = joc.dades().sacs
            val sortit = when (val c = joc.obreSac(origen)) {
                // La raresa és la de la configuració.
                is ContingutSac.Nou -> Sortit.Nou((s.inicials + s.delsSacs).first { it.tipus == c.objecte.tipus && it.id == c.objecte.id })
                is ContingutSac.Punts -> Sortit.Punts(c.punts)
            }
            _estat.update { it.copy(sortit = sortit) }
        }
    }

    fun tancaSac() = _estat.update { it.copy(obrint = null, sortit = null) }

    /** Fa servir una cosa de la col·lecció: el color, l'animació de càrrega o la tapa del passaport. Els emojis no es trien. */
    fun faServir(o: Objecte) {
        viewModelScope.launch {
            when (o.tipus) {
                TipusObjecte.COLOR -> preferencies.desaColorApp(o.id)
                TipusObjecte.ANIMACIO -> preferencies.desaAnimacioCarrega(o.id)
                TipusObjecte.PORTADA -> preferencies.desaTapaPassaport(o.id)
                TipusObjecte.EMOJI -> Unit
            }
        }
    }

    /** Torna al color de sempre (l'ambre). */
    fun colorDeSempre() = viewModelScope.launch { preferencies.desaColorApp(null) }

    /** Torna a l'animació de sempre (la sardana). */
    fun animacioDeSempre() = viewModelScope.launch { preferencies.desaAnimacioCarrega(null) }
}
