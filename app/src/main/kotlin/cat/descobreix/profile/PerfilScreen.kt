package cat.descobreix.profile

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cat.descobreix.R
import cat.descobreix.gent.CompteSocial
import cat.descobreix.ui.TargetaMedalla
import cat.descobreix.ui.components.Avatar
import cat.descobreix.ui.components.BarraProgres
import cat.descobreix.ui.components.BotoSecundari
import cat.descobreix.ui.components.Carregant
import cat.descobreix.ui.components.Icones
import cat.descobreix.ui.components.ImatgeLocal
import cat.descobreix.ui.components.Xifra
import cat.descobreix.ui.plural
import cat.descobreix.ui.rememberPermisCamera
import cat.descobreix.ui.theme.Colors
import java.io.File

/** El perfil té dues pestanyes: el perfil i el rànquing (docs/requisits.md, secció 4). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    onObreFoto: (String) -> Unit,
    onObreSobre: () -> Unit,
    onObrePassaport: () -> Unit,
    onObreSacs: () -> Unit,
    onObreCataleg: () -> Unit,
    onObreSeguits: () -> Unit,
) {
    var pestanya by rememberSaveable { mutableIntStateOf(0) }
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        PrimaryTabRow(selectedTabIndex = pestanya, containerColor = Colors.Fons, contentColor = Colors.Text) {
            listOf(R.string.pestanya_perfil, R.string.pestanya_ranquing).forEachIndexed { i, text ->
                Tab(
                    selected = pestanya == i,
                    onClick = { pestanya = i },
                    text = { Text(stringResource(text), style = MaterialTheme.typography.titleSmall) },
                    selectedContentColor = Colors.Text,
                    unselectedContentColor = Colors.TextSecundari,
                )
            }
        }
        when (pestanya) {
            0 -> PestanyaPerfil(onObreFoto, onObreSobre, onObrePassaport, onObreSacs, onObreCataleg, onObreSeguits)
            else -> RanquingPestanya()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PestanyaPerfil(
    onObreFoto: (String) -> Unit,
    onObreSobre: () -> Unit,
    onObrePassaport: () -> Unit,
    onObreSacs: () -> Unit,
    onObreCataleg: () -> Unit,
    onObreSeguits: () -> Unit,
    viewModel: PerfilViewModel = hiltViewModel(),
) {
    val estat by viewModel.estat.collectAsStateWithLifecycle()
    if (estat.carregant) {
        Carregant(Modifier.fillMaxSize())
        return
    }
    var confirmantEsborrar by rememberSaveable { mutableStateOf(false) }
    var confirmantSortir by rememberSaveable { mutableStateOf(false) }
    var triantFoto by rememberSaveable { mutableStateOf(false) }
    var senseCamera by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.canviaFoto(uri)
    }
    val fotoCamera = remember(context) { fitxerFotoCamera(context) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) viewModel.canviaFoto(fotoCamera)
    }
    val demanaCamera = rememberPermisCamera(onConcedit = { camera.launch(fotoCamera) }, onDenegat = { senseCamera = true })
    val exportador = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) viewModel.exporta(uri)
    }
    val nomExportacio = stringResource(R.string.nom_fitxer_exportacio)

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item { Capcalera(estat, onFoto = { triantFoto = true }) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Xifra("${estat.descoberts}", stringResource(R.string.stat_municipis), Modifier.weight(1f), Colors.AmbreText)
                Xifra("${estat.missionsFetes}", stringResource(R.string.stat_missions), Modifier.weight(1f))
                Xifra("${estat.fotos}", stringResource(R.string.stat_fotos), Modifier.weight(1f))
                Xifra("${estat.comarquesCompletes}/${estat.comarques.size}", stringResource(R.string.stat_comarques), Modifier.weight(1f))
            }
        }
        item {
            Text(
                plural(R.plurals.punts_resum, estat.saldo, estat.saldo, estat.puntsGuanyats),
                style = MaterialTheme.typography.bodyMedium.copy(color = Colors.TextSecundari),
            )
        }

        item { Titol(stringResource(R.string.album)) }
        if (estat.album.isEmpty()) {
            item { Text(stringResource(R.string.album_buit), style = MaterialTheme.typography.bodyMedium.copy(color = Colors.TextSecundari)) }
        }
        items(estat.album, key = { it.nom }) { grup ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(grup.nom, style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (f in grup.fotos) {
                        ImatgeLocal(
                            f.miniatura,
                            stringResource(R.string.foto_de, grup.nom),
                            Modifier
                                .width(104.dp)
                                .aspectRatio(1f)
                                .clip(RectangleShape)
                                .clickable(role = Role.Image) { onObreFoto(f.id) },
                            midaMaxima = 320,
                        )
                    }
                }
            }
        }

        item { BotoSecundari(stringResource(R.string.passaport), onObrePassaport, icona = Icones.Mapa) }
        item { BotoSecundari(stringResource(R.string.cataleg), onObreCataleg, icona = Icones.Camera) }
        item { BotoSecundari(stringResource(R.string.sacs_titol), onObreSacs, icona = Icones.Sac) }
        item { CompteSocial(onObreSeguits) }

        item { Titol(stringResource(R.string.medalles)) }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                for (m in estat.medalles) {
                    TargetaMedalla(m, m.comarca?.let { estat.nomsComarques[it] }, m.comarca?.let { estat.siluetes[it] })
                }
            }
        }

        item { Titol(stringResource(R.string.comarques)) }
        items(estat.comarques, key = { it.codi }) { c ->
            Column(Modifier.semantics(mergeDescendants = true) {}, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(c.nom, style = MaterialTheme.typography.bodyMedium)
                    Text("${c.descoberts}/${c.total}", style = MaterialTheme.typography.bodySmall)
                }
                BarraProgres(c.descoberts.toFloat() / c.total, alcada = 6, color = if (c.completa) Colors.Ambre else Colors.AmbreFosc)
            }
        }

        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .toggleable(estat.soIVibracio, role = Role.Switch, onValueChange = viewModel::canviaSoIVibracio)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.so_i_vibracio), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.so_i_vibracio_text), style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = estat.soIVibracio,
                    onCheckedChange = null,
                    colors = SwitchDefaults.colors(checkedTrackColor = Colors.Ambre, checkedThumbColor = Colors.TintaAmbre),
                )
            }
        }

        item { Titol(stringResource(R.string.les_teves_dades)) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.les_teves_dades_text), style = MaterialTheme.typography.bodySmall)
                BotoSecundari(stringResource(R.string.exporta), { exportador.launch(nomExportacio) }, icona = Icones.Exportar, enabled = !estat.treballant)
                BotoSecundari(stringResource(R.string.esborra_tot), { confirmantEsborrar = true }, icona = Icones.Esborrar, enabled = !estat.treballant)
                BotoSecundari(stringResource(R.string.sobre_app), onObreSobre, icona = Icones.Info)
            }
        }

        estat.nomUsuari?.let { nom ->
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.perfil_sessio, nom), style = MaterialTheme.typography.bodySmall)
                    BotoSecundari(stringResource(R.string.compte_surt), { confirmantSortir = true }, icona = Icones.Perfil, enabled = !estat.treballant)
                }
            }
        }
    }

    if (triantFoto) {
        AlertDialog(
            onDismissRequest = { triantFoto = false },
            containerColor = Colors.Superficie,
            title = { Text(stringResource(R.string.foto_perfil)) },
            text = {
                Column {
                    OpcioFoto(stringResource(R.string.foto_perfil_galeria)) {
                        triantFoto = false
                        galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                    OpcioFoto(stringResource(R.string.foto_perfil_camera)) {
                        triantFoto = false
                        demanaCamera()
                    }
                    if (estat.foto != null) {
                        OpcioFoto(stringResource(R.string.foto_perfil_treu)) {
                            triantFoto = false
                            viewModel.treuFoto()
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { triantFoto = false }) { Text(stringResource(R.string.cancela)) } },
        )
    }
    if (senseCamera) {
        AlertDialog(
            onDismissRequest = { senseCamera = false },
            containerColor = Colors.Superficie,
            text = { Text(stringResource(R.string.missatge_sense_permis_camera)) },
            confirmButton = { TextButton(onClick = { senseCamera = false }) { Text(stringResource(R.string.entesos)) } },
        )
    }
    if (estat.errorFoto) {
        AlertDialog(
            onDismissRequest = viewModel::tancaErrorFoto,
            containerColor = Colors.Superficie,
            text = { Text(stringResource(R.string.foto_perfil_error)) },
            confirmButton = { TextButton(onClick = viewModel::tancaErrorFoto) { Text(stringResource(R.string.entesos)) } },
        )
    }
    if (confirmantEsborrar) {
        AlertDialog(
            onDismissRequest = { confirmantEsborrar = false },
            containerColor = Colors.Superficie,
            title = { Text(stringResource(R.string.esborra_tot)) },
            text = { Text(stringResource(R.string.esborra_tot_text)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmantEsborrar = false
                    viewModel.esborraTot()
                }) { Text(stringResource(R.string.esborra)) }
            },
            dismissButton = { TextButton(onClick = { confirmantEsborrar = false }) { Text(stringResource(R.string.cancela)) } },
        )
    }
    if (confirmantSortir) {
        AlertDialog(
            onDismissRequest = { confirmantSortir = false },
            containerColor = Colors.Superficie,
            title = { Text(stringResource(R.string.compte_surt)) },
            text = { Text(stringResource(R.string.perfil_surt_text)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmantSortir = false
                    viewModel.surt()
                }) { Text(stringResource(R.string.compte_surt)) }
            },
            dismissButton = { TextButton(onClick = { confirmantSortir = false }) { Text(stringResource(R.string.cancela)) } },
        )
    }
    if (estat.errorEsborrant) {
        AlertDialog(
            onDismissRequest = viewModel::tancaErrorEsborrant,
            containerColor = Colors.Superficie,
            text = { Text(stringResource(R.string.compte_error_sense_connexio)) },
            confirmButton = { TextButton(onClick = viewModel::tancaErrorEsborrant) { Text(stringResource(R.string.entesos)) } },
        )
    }
    estat.exportat?.let { ok ->
        AlertDialog(
            onDismissRequest = viewModel::tancaExportacio,
            containerColor = Colors.Superficie,
            text = { Text(stringResource(if (ok) R.string.exportacio_feta else R.string.exportacio_error)) },
            confirmButton = { TextButton(onClick = viewModel::tancaExportacio) { Text(stringResource(R.string.entesos)) } },
        )
    }
}

@Composable
private fun Titol(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
}

/** La foto de perfil (tocar-la la canvia), el nom i el nivell. */
@Composable
private fun Capcalera(estat: PerfilEstat, onFoto: () -> Unit) {
    val nivell = estat.nivell ?: return
    val nom = estat.nomUsuari.orEmpty()
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(
            Modifier
                .size(72.dp)
                .clickable(enabled = !estat.canviantFoto, onClickLabel = stringResource(R.string.foto_perfil_canvia), role = Role.Button, onClick = onFoto),
            contentAlignment = Alignment.Center,
        ) {
            Avatar(estat.foto, nom, 72.dp, vora = true)
            if (estat.canviantFoto) {
                CircularProgressIndicator(Modifier.size(32.dp), color = Colors.AmbreText)
            } else {
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .size(24.dp)
                        .background(Colors.Ambre)
                        .border(2.dp, Colors.Text, RectangleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icones.Camera, contentDescription = null, tint = Colors.TintaAmbre, modifier = Modifier.size(14.dp))
                }
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (nom.isNotEmpty()) {
                Text(nom, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
            }
            Text(stringResource(R.string.nivell, nivell.numero), style = MaterialTheme.typography.titleMedium.copy(color = Colors.AmbreText))
            Text(plural(R.plurals.punts_per_al_nivell, nivell.faltenPerAlSeguent, nivell.faltenPerAlSeguent, nivell.numero + 1), style = MaterialTheme.typography.bodySmall)
            BarraProgres(nivell.progres, alcada = 6)
        }
    }
}

@Composable
private fun OpcioFoto(text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
        Text(text, Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodyLarge)
    }
}

/** On l'app de càmera desa la foto de perfil (res/xml/fitxers_compartits.xml). */
private fun fitxerFotoCamera(context: Context): Uri {
    val fitxer = File(File(context.cacheDir, "foto_perfil").apply { mkdirs() }, "camera.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fitxers", fitxer)
}
