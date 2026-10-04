package cat.descobreix.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.descobreix.data.compte.FotosPerfilRemotes
import cat.descobreix.ui.theme.Colors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.io.File

@EntryPoint
@InstallIn(SingletonComponent::class)
interface FotosPerfilEntryPoint {
    fun fotosPerfil(): FotosPerfilRemotes
}

/**
 * Foto de perfil rodona. Si l'usuari no en té (o encara no s'ha baixat), mostra la inicial del nom.
 * És decorativa: el nom sempre es mostra al costat.
 */
@Composable
fun Avatar(ruta: String?, nom: String, mida: Dp, modifier: Modifier = Modifier, vora: Boolean = false) {
    val context = LocalContext.current
    val previsualitzacio = LocalInspectionMode.current
    val fitxer by produceState<File?>(null, ruta) {
        value = if (ruta == null || previsualitzacio) null else fotosPerfil(context).fitxer(ruta)
    }
    Box(
        modifier
            .size(mida)
            .clip(CircleShape)
            .background(Colors.Superficie2)
            .then(if (vora) Modifier.border(2.dp, Colors.Ambre, CircleShape) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        val f = fitxer
        if (f != null) {
            ImatgeLocal(f.path, descripcio = null, modifier = Modifier.fillMaxSize(), midaMaxima = 320)
        } else {
            Text(
                nom.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(color = Colors.TextSecundari, fontSize = (mida.value * 0.4f).sp),
            )
        }
    }
}

private fun fotosPerfil(context: Context): FotosPerfilRemotes =
    EntryPointAccessors.fromApplication(context.applicationContext, FotosPerfilEntryPoint::class.java).fotosPerfil()
