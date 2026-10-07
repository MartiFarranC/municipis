package cat.descobreix.data.sincronitzacio

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import cat.descobreix.data.compte.EstatCompte
import cat.descobreix.data.compte.ServeiCompte
import cat.descobreix.data.db.BaseDades
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** El treball de WorkManager que sincronitza. Les dependències les treu de Hilt (no cal una fàbrica de treballs). */
class TreballSincronitzacio(context: Context, parametres: WorkerParameters) : CoroutineWorker(context, parametres) {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Dependencies {
        fun sincronitzador(): Sincronitzador

        fun compte(): ServeiCompte
    }

    override suspend fun doWork(): Result {
        val d = EntryPointAccessors.fromApplication(applicationContext, Dependencies::class.java)
        // Només amb un compte llest (amb perfil): les files del servidor en depenen.
        val estat = withTimeoutOrNull(20_000) { d.compte().estat.first { it !is EstatCompte.Carregant } }
        if (estat !is EstatCompte.Llest) return Result.success()
        return try {
            d.sincronitzador().sincronitza()
            Result.success()
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.w("Sincronitzacio", "No s'ha pogut sincronitzar", e)
            if (runAttemptCount < 5) Result.retry() else Result.failure()
        }
    }
}

/**
 * Decideix quan es sincronitza: cada poques hores i, amb connexió, poc després de cada canvi al mòbil i en entrar
 * amb un compte.
 */
@Singleton
class PlanificadorSincronitzacio @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: BaseDades,
    private val compte: ServeiCompte,
) {
    private val ambit = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val ambXarxa = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    fun comenca() {
        val wm = try {
            WorkManager.getInstance(context)
        } catch (e: IllegalStateException) {
            // Sense WorkManager (per exemple, en alguns tests), no hi ha sincronització.
            return
        }
        wm.enqueueUniquePeriodicWork(
            PERIODIC,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<TreballSincronitzacio>(6, TimeUnit.HOURS).setConstraints(ambXarxa).build(),
        )
        ambit.launch {
            @OptIn(kotlinx.coroutines.FlowPreview::class)
            combine(compte.estat, db.sincronitzacio().quantsPendents()) { e, n -> e to n }
                .filter { (e, _) -> e is EstatCompte.Llest }
                .debounce(5_000)
                .collect { ara(wm) }
        }
    }

    private fun ara(wm: WorkManager) {
        wm.enqueueUniqueWork(
            ARA,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            OneTimeWorkRequestBuilder<TreballSincronitzacio>()
                .setConstraints(ambXarxa)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build(),
        )
    }

    private companion object {
        const val PERIODIC = "sincronitzacio_periodica"
        const val ARA = "sincronitzacio_ara"
    }
}
