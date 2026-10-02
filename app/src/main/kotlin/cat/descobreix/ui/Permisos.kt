package cat.descobreix.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Retorna una funció que demana el permís d'ubicació (només en el moment en què cal)
 * i després crida [onConcedit] o [onDenegat].
 */
@Composable
fun rememberPermisUbicacio(onConcedit: () -> Unit, onDenegat: () -> Unit): () -> Unit {
    val context = LocalContext.current
    val concedit by rememberUpdatedState(onConcedit)
    val denegat by rememberUpdatedState(onDenegat)
    val llancador = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { r ->
        if (r.values.any { it }) concedit() else denegat()
    }
    return {
        val te = listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            .any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
        if (te) {
            concedit()
        } else {
            llancador.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }
}

@Composable
fun rememberPermisCamera(onConcedit: () -> Unit, onDenegat: () -> Unit): () -> Unit {
    val context = LocalContext.current
    val concedit by rememberUpdatedState(onConcedit)
    val denegat by rememberUpdatedState(onDenegat)
    val llancador = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) concedit() else denegat()
    }
    return {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            concedit()
        } else {
            llancador.launch(Manifest.permission.CAMERA)
        }
    }
}
