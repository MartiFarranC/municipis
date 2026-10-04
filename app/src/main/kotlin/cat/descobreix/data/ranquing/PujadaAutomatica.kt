package cat.descobreix.data.ranquing

import android.util.Log
import cat.descobreix.data.compte.EstatCompte
import cat.descobreix.data.compte.ServeiCompte
import cat.descobreix.domain.Joc
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Puja el progrés quan l'usuari té el compte llest i cada vegada que descobreix un municipi o
 * completa una missió. Si falla (per exemple, sense connexió), es torna a provar amb el canvi
 * següent o en obrir el rànquing.
 */
@Singleton
class PujadaAutomatica @Inject constructor(
    private val compte: ServeiCompte,
    private val joc: Joc,
    private val ranquing: ServeiRanquing,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @OptIn(FlowPreview::class)
    fun comenca() {
        scope.launch {
            combine(
                compte.estat.map { it is EstatCompte.Llest }.distinctUntilChanged(),
                joc.progres.map { it.descoberts.size to it.completades.size }.distinctUntilChanged(),
            ) { llest, mida -> llest to mida }
                .filter { (llest, _) -> llest }
                .debounce(ESPERA_MS)
                .collectLatest {
                    try {
                        ranquing.pujaProgres()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.w(TAG, "No s'ha pogut pujar el progrés", e)
                    }
                }
        }
    }

    private companion object {
        const val TAG = "Pujada"
        const val ESPERA_MS = 2_000L
    }
}
