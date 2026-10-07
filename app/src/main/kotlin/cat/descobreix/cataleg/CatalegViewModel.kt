package cat.descobreix.cataleg

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.descobreix.data.repositori.FotosRepositori
import cat.descobreix.domain.Foto
import cat.descobreix.domain.Joc
import cat.descobreix.joc.model.CodiIne
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.Collator
import java.util.Locale
import javax.inject.Inject

/** Una casella del catàleg: el cromo (la foto del cartell), o buida si encara no n'hi ha. */
data class Casella(val codi: CodiIne, val nom: String, val descobert: Boolean, val cromo: Foto?)

/** Un grup de caselles: una comarca (amb quants cromos té) o, en ordre alfabètic, tots els municipis. */
data class GrupCataleg(val nom: String?, val cromos: Int, val caselles: List<Casella>)

enum class OrdreCataleg { COMARQUES, ALFABETIC }

data class CatalegEstat(
    val carregant: Boolean = true,
    val ordre: OrdreCataleg = OrdreCataleg.COMARQUES,
    val grups: List<GrupCataleg> = emptyList(),
    val cromos: Int = 0,
    val total: Int = 0,
)

/** El catàleg de cartells: els 947 municipis, cadascun amb la foto del seu cartell d'entrada quan es fa. */
@HiltViewModel
class CatalegViewModel @Inject constructor(
    private val joc: Joc,
    fotos: FotosRepositori,
) : ViewModel() {
    private val _estat = MutableStateFlow(CatalegEstat())
    val estat: StateFlow<CatalegEstat> = _estat.asStateFlow()
    private val ordre = MutableStateFlow(OrdreCataleg.COMARQUES)
    private val collator = Collator.getInstance(Locale.forLanguageTag("ca"))

    init {
        viewModelScope.launch {
            val g = joc.dades().geografia
            combine(joc.progres, fotos.totes, ordre) { p, f, o -> Triple(p, f, o) }.collect { (p, f, o) ->
                // El cromo de cada municipi és la foto del cartell més nova (les fotos venen de la més nova a la més antiga).
                val cromos = f.filter { it.esCromo }.groupBy { it.codiIne }.mapValues { (_, l) -> l.maxBy { it.creatEl } }
                val descoberts = p.conjuntDescoberts
                fun casella(m: cat.descobreix.joc.model.Municipi) = Casella(m.codi, m.nom, m.codi in descoberts, cromos[m.codi])
                val grups = when (o) {
                    OrdreCataleg.COMARQUES -> g.comarques.sortedWith(compareBy(collator) { it.nom }).map { c ->
                        val cs = g.municipisDeComarca(c.codi).sortedWith(compareBy(collator) { it.nom }).map(::casella)
                        GrupCataleg(c.nom, cs.count { it.cromo != null }, cs)
                    }
                    OrdreCataleg.ALFABETIC -> {
                        val cs = g.municipis.sortedWith(compareBy(collator) { it.nom }).map(::casella)
                        listOf(GrupCataleg(null, cs.count { it.cromo != null }, cs))
                    }
                }
                _estat.update { it.copy(carregant = false, ordre = o, grups = grups, cromos = cromos.size, total = g.total) }
            }
        }
    }

    fun canviaOrdre(o: OrdreCataleg) {
        ordre.value = o
    }
}
