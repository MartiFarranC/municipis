package cat.descobreix.photos

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import cat.descobreix.R
import cat.descobreix.data.repositori.AjuntamentsRepositori
import cat.descobreix.data.ubicacio.ServeiUbicacio
import cat.descobreix.domain.Joc
import cat.descobreix.joc.geo.Localitzacio
import cat.descobreix.joc.model.CodiIne
import cat.descobreix.joc.model.Missio
import cat.descobreix.joc.regles.MissioAjuntament
import cat.descobreix.joc.regles.ResultatProva
import cat.descobreix.joc.regles.Ubicacio
import cat.descobreix.ui.Celebracio
import cat.descobreix.ui.DialegMissatge
import cat.descobreix.ui.DialegTriaMunicipi
import cat.descobreix.ui.Missatge
import cat.descobreix.ui.components.BotoIcona
import cat.descobreix.ui.components.BotoPrincipal
import cat.descobreix.ui.components.Icones
import cat.descobreix.ui.missatgeDe
import cat.descobreix.ui.rememberPermisCamera
import cat.descobreix.ui.rememberPermisUbicacio
import cat.descobreix.ui.theme.Colors
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.ReaderException
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import javax.inject.Inject

data class QrEstat(
    val nom: String = "",
    val missio: Missio? = null,
    val processant: Boolean = false,
    val candidats: List<Pair<CodiIne, String>>? = null,
    val missatge: Missatge? = null,
    /** La missió s'ha fet: es celebra i la pantalla es tanca. */
    val feta: Missatge? = null,
)

/**
 * Llegeix el codi QR d'un punt de segellat d'un ajuntament (requisits.md, secció 9.6). Si és el de la missió i l'usuari
 * és dins del municipi, la missió es completa.
 */
@HiltViewModel
class LectorQrViewModel @Inject constructor(
    private val joc: Joc,
    private val ajuntaments: AjuntamentsRepositori,
    private val ubicacio: ServeiUbicacio,
    estatDesat: SavedStateHandle,
) : ViewModel() {
    private val codi: CodiIne = checkNotNull(estatDesat["codi"])
    private val missioId: String = checkNotNull(estatDesat["missio"])

    private val _estat = MutableStateFlow(QrEstat())
    val estat: StateFlow<QrEstat> = _estat.asStateFlow()

    private var origen: MissioAjuntament? = null
    private var pendent: Pair<Ubicacio, Localitzacio>? = null

    init {
        viewModelScope.launch {
            val d = joc.dades()
            origen = ajuntaments.missionsAra().firstOrNull { it.idMissio == missioId }
            _estat.update { it.copy(nom = d.geografia.municipi(codi).nom, missio = joc.missio(missioId)) }
        }
    }

    /** S'ha llegit un codi QR. Mentre se'n comprova un, els altres no es tenen en compte. */
    fun llegit(text: String) {
        val e = _estat.value
        if (e.processant || e.missatge != null || e.candidats != null || e.feta != null) return
        val o = origen ?: return
        _estat.update { it.copy(processant = true) }
        viewModelScope.launch {
            val d = joc.dades()
            if (!d.ajuntaments.qrValid(o, text)) {
                _estat.update { it.copy(processant = false, missatge = Missatge.QrNoValid) }
                return@launch
            }
            val u = ubicacio.ubicacioActual()
            if (u == null) {
                _estat.update { it.copy(processant = false, missatge = Missatge.SenseUbicacio) }
                return@launch
            }
            val loc = withContext(Dispatchers.Default) { d.localitzador.localitza(u.lat, u.lon, u.precisioMetres) }
            valida(u, loc, null)
        }
    }

    fun triaCandidat(municipi: CodiIne) {
        val (u, loc) = pendent ?: return
        pendent = null
        _estat.update { it.copy(candidats = null) }
        viewModelScope.launch { valida(u, loc, municipi) }
    }

    fun tancaCandidats() {
        pendent = null
        _estat.update { it.copy(candidats = null, processant = false) }
    }

    fun tancaMissatge() = _estat.update { it.copy(missatge = null) }

    fun sensePermisUbicacio() = _estat.update { it.copy(missatge = Missatge.SensePermisUbicacio) }

    private suspend fun valida(u: Ubicacio, loc: Localitzacio, triat: CodiIne?) {
        val d = joc.dades()
        val missio = _estat.value.missio ?: return
        val descoberts = joc.progres.first().conjuntDescoberts
        when (val r = d.validador.validaFoto(codi, loc, descoberts, triat)) {
            ResultatProva.Valida -> {
                val punts = joc.completaMissio(missio, u, null)
                _estat.update { it.copy(processant = false, feta = Missatge.PuntsGuanyats(punts.missio, punts.bonus)) }
            }
            is ResultatProva.CalTriarMunicipi -> {
                pendent = u to loc
                _estat.update { it.copy(candidats = r.candidats.map { c -> c to d.geografia.municipi(c).nom }) }
            }
            else -> {
                val m = joc.missatgeDe(r)
                _estat.update { it.copy(processant = false, missatge = m) }
            }
        }
    }
}

@Composable
fun LectorQrScreen(onTanca: () -> Unit, viewModel: LectorQrViewModel = hiltViewModel()) {
    val estat by viewModel.estat.collectAsStateWithLifecycle()
    var permisCamera by remember { mutableStateOf<Boolean?>(null) }
    var missatgePermis by remember { mutableStateOf<Missatge?>(null) }

    val demanaUbicacio = rememberPermisUbicacio(onConcedit = {}, onDenegat = viewModel::sensePermisUbicacio)
    val demanaCamera = rememberPermisCamera(
        onConcedit = {
            permisCamera = true
            // La ubicació també cal per validar la missió: es demana ara, en el moment en què cal.
            demanaUbicacio()
        },
        onDenegat = {
            permisCamera = false
            missatgePermis = Missatge.SensePermisCamera
        },
    )
    LaunchedEffect(Unit) { demanaCamera() }

    Box(
        Modifier
            .fillMaxSize()
            .background(Colors.Fons),
    ) {
        if (permisCamera == true) {
            VistaQr(viewModel::llegit, Modifier.fillMaxSize())
            // On s'ha de posar el codi.
            Box(
                Modifier
                    .align(Alignment.Center)
                    .size(240.dp)
                    .border(3.dp, Colors.Ambre, RoundedCornerShape(16.dp)),
            )
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
                Text(
                    stringResource(R.string.qr_text, estat.missio?.titol.orEmpty()),
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
                permisCamera == false -> BotoPrincipal(stringResource(R.string.torna_a_demanar_permis), demanaCamera)
            }
        }
    }

    estat.candidats?.let { DialegTriaMunicipi(it, viewModel::triaCandidat, viewModel::tancaCandidats) }
    estat.missatge?.let { DialegMissatge(it, viewModel::tancaMissatge) }
    missatgePermis?.let { DialegMissatge(it) { missatgePermis = null } }
    estat.feta?.let { Celebracio(it, onTanca) }
}

/** La càmera, que va passant les imatges al lector de QR. */
@Composable
private fun VistaQr(onLlegit: (String) -> Unit, modifier: Modifier) {
    val context = LocalContext.current
    val cicle = LocalLifecycleOwner.current
    val llegit by rememberUpdatedState(onLlegit)
    val vista = remember { PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } }
    DisposableEffect(cicle) {
        val fil = Executors.newSingleThreadExecutor()
        val futur = ProcessCameraProvider.getInstance(context)
        var proveidor: ProcessCameraProvider? = null
        futur.addListener({
            val p = futur.get()
            proveidor = p
            val previa = Preview.Builder().build().also { it.setSurfaceProvider(vista.surfaceProvider) }
            val analisi = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            val lector = LectorCodisQr()
            analisi.setAnalyzer(fil) { imatge ->
                val text = imatge.use { lector.llegeix(it) }
                if (text != null) ContextCompat.getMainExecutor(context).execute { llegit(text) }
            }
            p.unbindAll()
            p.bindToLifecycle(cicle, CameraSelector.DEFAULT_BACK_CAMERA, previa, analisi)
        }, ContextCompat.getMainExecutor(context))
        onDispose {
            proveidor?.unbindAll()
            fil.shutdown()
        }
    }
    AndroidView(factory = { vista }, modifier = modifier)
}

/** Llegeix codis QR de les imatges de la càmera amb ZXing, sense connexió. */
private class LectorCodisQr {
    private val lector = QRCodeReader()
    private val pistes = mapOf(DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE))

    /** El text del codi QR de la imatge, o null si no n'hi ha cap (o no es pot llegir). */
    fun llegeix(imatge: ImageProxy): String? {
        // El primer pla (Y) del format YUV_420_888 és la lluminositat, que és tot el que cal.
        val pla = imatge.planes[0]
        val buffer = pla.buffer
        val bytes = ByteArray(buffer.remaining()).also { buffer.get(it) }
        val font = PlanarYUVLuminanceSource(bytes, pla.rowStride, imatge.height, 0, 0, imatge.width, imatge.height, false)
        return try {
            lector.decode(BinaryBitmap(HybridBinarizer(font)), pistes).text
        } catch (e: ReaderException) {
            null
        } finally {
            lector.reset()
        }
    }
}
