package cat.descobreix.photos

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cat.descobreix.R
import cat.descobreix.municipality.titolMissio
import cat.descobreix.ui.DialegMissatge
import cat.descobreix.ui.DialegTriaMunicipi
import cat.descobreix.ui.Missatge
import cat.descobreix.ui.components.BotoIcona
import cat.descobreix.ui.components.BotoPrincipal
import cat.descobreix.ui.components.Icones
import cat.descobreix.ui.rememberPermisCamera
import cat.descobreix.ui.rememberPermisUbicacio
import cat.descobreix.ui.textDe
import cat.descobreix.ui.theme.Colors

/** Càmera de l'app. Les fotos només es poden fer des d'aquí (no s'accepten fotos de la galeria). */
@Composable
fun CameraScreen(onTanca: () -> Unit, viewModel: CameraViewModel = hiltViewModel()) {
    val estat by viewModel.estat.collectAsStateWithLifecycle()
    var permisCamera by remember { mutableStateOf<Boolean?>(null) }
    var missatgePermis by remember { mutableStateOf<Missatge?>(null) }

    val demanaUbicacio = rememberPermisUbicacio(onConcedit = {}, onDenegat = viewModel::sensePermisUbicacio)
    val demanaCamera = rememberPermisCamera(
        onConcedit = {
            permisCamera = true
            // La ubicació també cal per validar la foto: es demana ara, en el moment en què cal.
            demanaUbicacio()
        },
        onDenegat = {
            permisCamera = false
            missatgePermis = Missatge.SensePermisCamera
        },
    )
    LaunchedEffect(Unit) { demanaCamera() }

    val desada = estat.desada
    if (desada != null) {
        val text = textDe(desada)
        val context = LocalContext.current
        LaunchedEffect(desada) {
            android.widget.Toast.makeText(context, text, android.widget.Toast.LENGTH_LONG).show()
            onTanca()
        }
    }

    val captura = remember { ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build() }
    val context = LocalContext.current

    Box(
        Modifier
            .fillMaxSize()
            .background(Colors.Fons),
    ) {
        if (permisCamera == true) {
            VistaPrevia(captura, Modifier.fillMaxSize())
        }
        Column(
            Modifier
                .fillMaxWidth()
                .systemBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BotoIcona(Icones.Tancar, stringResource(R.string.tanca), onTanca)
            Column(
                Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Colors.Fons.copy(alpha = 0.85f))
                    .padding(12.dp),
            ) {
                Text(estat.nom, style = MaterialTheme.typography.titleLarge)
                val m = estat.missio
                Text(
                    if (m != null) stringResource(R.string.camera_missio, titolMissio(m)) else stringResource(R.string.camera_text),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .systemBarsPadding()
                .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            when {
                estat.processant -> CircularProgressIndicator(color = Colors.Ambre)
                permisCamera == true -> {
                    val descripcio = stringResource(R.string.fes_foto)
                    Box(
                        Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .border(4.dp, Colors.Ambre, CircleShape)
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(Colors.Text)
                            .clickable(role = Role.Button) {
                                viewModel.comencaCaptura()
                                fesFoto(context, captura, viewModel::fotoFeta, viewModel::error)
                            }
                            .semantics { contentDescription = descripcio },
                    )
                }
                permisCamera == false -> BotoPrincipal(stringResource(R.string.torna_a_demanar_permis), demanaCamera)
            }
        }
    }

    estat.candidats?.let { DialegTriaMunicipi(it, viewModel::triaCandidat, viewModel::tancaCandidats) }
    estat.missatge?.let { DialegMissatge(it, viewModel::tancaMissatge) }
    missatgePermis?.let { DialegMissatge(it) { missatgePermis = null } }
}

@Composable
private fun VistaPrevia(captura: ImageCapture, modifier: Modifier) {
    val context = LocalContext.current
    val cicle = LocalLifecycleOwner.current
    val vista = remember { PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } }
    DisposableEffect(cicle) {
        val futur = ProcessCameraProvider.getInstance(context)
        var proveidor: ProcessCameraProvider? = null
        futur.addListener({
            val p = futur.get()
            proveidor = p
            val previa = Preview.Builder().build().also { it.setSurfaceProvider(vista.surfaceProvider) }
            p.unbindAll()
            p.bindToLifecycle(cicle, CameraSelector.DEFAULT_BACK_CAMERA, previa, captura)
        }, ContextCompat.getMainExecutor(context))
        onDispose { proveidor?.unbindAll() }
    }
    AndroidView(factory = { vista }, modifier = modifier)
}

private fun fesFoto(
    context: Context,
    captura: ImageCapture,
    onFeta: (ByteArray, Int) -> Unit,
    alFallar: () -> Unit,
) {
    captura.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val rotacio = image.imageInfo.rotationDegrees
                val buffer = image.planes[0].buffer
                val bytes = ByteArray(buffer.remaining()).also { buffer.get(it) }
                image.close()
                onFeta(bytes, rotacio)
            }

            override fun onError(exception: ImageCaptureException) = alFallar()
        },
    )
}
