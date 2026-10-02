package cat.descobreix.data.ubicacio

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import cat.descobreix.joc.regles.Ubicacio
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/** Ubicació actual de l'usuari. Només es demana en primer pla, quan cal. */
interface ServeiUbicacio {
    fun tePermis(): Boolean

    /** Retorna la ubicació actual, o null si no es pot obtenir. */
    suspend fun ubicacioActual(): Ubicacio?
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

    private companion object {
        const val EDAT_MAXIMA_MS = 10_000L
        const val DURADA_MAXIMA_MS = 30_000L
    }
}
