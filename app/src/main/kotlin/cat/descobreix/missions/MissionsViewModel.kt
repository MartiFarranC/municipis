package cat.descobreix.missions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.descobreix.domain.Joc
import cat.descobreix.joc.model.CodiIne
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ResumMunicipi(
    val codi: CodiIne,
    val nom: String,
    val comarca: String,
    val fetes: Int,
    val total: Int,
    val punts: Int,
    val puntsPossibles: Int,
) {
    val complet: Boolean get() = total > 0 && fetes == total
}

data class MissionsEstat(
    val carregant: Boolean = true,
    val municipis: List<ResumMunicipi> = emptyList(),
    val missionsFetes: Int = 0,
    val saldo: Int = 0,
)

@HiltViewModel
class MissionsViewModel @Inject constructor(joc: Joc) : ViewModel() {
    val estat: StateFlow<MissionsEstat> = joc.progres.map { p ->
        val d = joc.dades()
        val municipis = p.descoberts.asReversed().map { codi ->
            val m = d.geografia.municipi(codi)
            val missions = d.missions.de(codi)
            ResumMunicipi(
                codi = codi,
                nom = m.nom,
                comarca = d.geografia.comarca(m.comarca).nom,
                fetes = missions.count { it.id in p.completades },
                total = missions.size,
                punts = d.regles.puntsGuanyatsA(codi, p.completades.keys),
                puntsPossibles = d.regles.puntsPossibles(codi),
            )
        }.sortedBy { it.complet }
        MissionsEstat(false, municipis, p.completades.size, p.saldo)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MissionsEstat())
}
