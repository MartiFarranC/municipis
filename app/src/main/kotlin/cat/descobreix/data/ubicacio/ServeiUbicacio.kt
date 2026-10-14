package cat.descobreix.data.ubicacio

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import cat.descobreix.joc.regles.Ubicacio
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/** Ubicació actual de l'usuari. Només es demana en primer pla, quan cal. */
interface ServeiUbicacio {
    fun tePermis(): Boolean

    /** Retorna la ubicació actual, o null si no es pot obtenir. */
    suspend fun ubicacioActual(): Ubicacio?

    /** La ubicació cada cop que canvia, mentre es recull el flux. Buit si no hi ha permís. */
    fun seguiment(): Flow<Ubicacio>
}

class ServeiUbicacioFused @Inject constructor(
    @ApplicationContext private val context: Context,
) : ServeiUbicacio {
    private val client by lazy { LocationServices.getFusedLocationProviderClient(context) }

    override fun tePermis(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    override suspend fun ubicacioActual(): Ubicacio? {
        if (!tePermis()) return null
        val peticio = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdateAgeMillis(EDAT_MAXIMA_MS)
            .setDurationMillis(DURADA_MAXIMA_MS)
            .build()
        val cancel = CancellationTokenSource()
        val loc = try {
            client.getCurrentLocation(peticio, cancel.token).await()
        } catch (e: CancellationException) {
            cancel.cancel()
            throw e
        } catch (e: Exception) {
            null
        } ?: return null
        val precisio = if (loc.hasAccuracy()) loc.accuracy.toDouble() else Double.MAX_VALUE
        return Ubicacio(loc.latitude, loc.longitude, precisio)
    }

    @SuppressLint("MissingPermission")
    override fun seguiment(): Flow<Ubicacio> {
        if (!tePermis()) return emptyFlow()
        return callbackFlow {
            val peticio = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, INTERVAL_SEGUIMENT_MS)
                .setMinUpdateIntervalMillis(INTERVAL_MINIM_MS)
                .build()
            val callback = object : LocationCallback() {
                override fun onLocationResult(r: LocationResult) {
                    val loc = r.lastLocation ?: return
                    val precisio = if (loc.hasAccuracy()) loc.accuracy.toDouble() else Double.MAX_VALUE
                    trySend(Ubicacio(loc.latitude, loc.longitude, precisio))
                }
            }
            try {
                client.requestLocationUpdates(peticio, callback, Looper.getMainLooper()).await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                close()
            }
            awaitClose { client.removeLocationUpdates(callback) }
        }
    }

    private companion object {
        const val EDAT_MAXIMA_MS = 10_000L
        const val DURADA_MAXIMA_MS = 30_000L
        const val INTERVAL_SEGUIMENT_MS = 10_000L
        const val INTERVAL_MINIM_MS = 3_000L
    }
}
