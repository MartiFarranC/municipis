package cat.descobreix.photos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cat.descobreix.R
import cat.descobreix.joc.model.Visibilitat
import cat.descobreix.ui.components.BotoIcona
import cat.descobreix.ui.components.BotoSecundari
import cat.descobreix.ui.components.Carregant
import cat.descobreix.ui.components.Icones
import cat.descobreix.ui.components.ImatgeLocal
import cat.descobreix.ui.theme.Colors
import cat.descobreix.ui.theme.Secundari
import java.text.DateFormat
import java.util.Date

@Composable
fun FotoScreen(onEnrere: () -> Unit, viewModel: FotoViewModel = hiltViewModel()) {
    val estat by viewModel.estat.collectAsStateWithLifecycle()
    LaunchedEffect(estat.esborrada) { if (estat.esborrada) onEnrere() }
    val foto = estat.foto
    if (estat.carregant || foto == null) {
        Carregant(Modifier.fillMaxSize())
        return
    }
    var confirmant by rememberSaveable { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Colors.Fons)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BotoIcona(Icones.Enrere, stringResource(R.string.enrere), onEnrere)
            Column {
                Text(estat.nomMunicipi, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
                Text(DateFormat.getDateInstance(DateFormat.LONG).format(Date(foto.creatEl)), style = MaterialTheme.typography.bodySmall)
            }
        }
        Box(Modifier.fillMaxWidth().heightIn(min = 240.dp)) {
            ImatgeLocal(
                foto.fitxer,
                stringResource(R.string.foto_de, estat.nomMunicipi),
                Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth,
                midaMaxima = 2048,
            )
        }

        Text(stringResource(R.string.visibilitat), style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
        Text(stringResource(R.string.visibilitat_text), style = MaterialTheme.typography.bodySmall)
        Column(Modifier.selectableGroup()) {
            for (v in Visibilitat.entries) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .selectable(selected = foto.visibilitat == v, role = Role.RadioButton) { viewModel.canviaVisibilitat(v) },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = foto.visibilitat == v,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(selectedColor = Colors.Secundari),
                    )
                    Text(
                        stringResource(
                            when (v) {
                                Visibilitat.PRIVADA -> R.string.visibilitat_privada
                                Visibilitat.AMICS -> R.string.visibilitat_amics
                                Visibilitat.PUBLICA -> R.string.visibilitat_publica
                            },
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }

        if (!foto.esPortada) {
            BotoSecundari(stringResource(R.string.fes_portada), viewModel::fesPortada, icona = Icones.Estrella)
        } else {
            Text(stringResource(R.string.es_portada), style = MaterialTheme.typography.bodyMedium.copy(color = Colors.Ambre))
        }
        BotoSecundari(stringResource(R.string.esborra_foto), { confirmant = true }, icona = Icones.Esborrar)
    }

    if (confirmant) {
        AlertDialog(
            onDismissRequest = { confirmant = false },
            containerColor = Colors.Superficie,
            title = { Text(stringResource(R.string.esborra_foto)) },
            text = { Text(stringResource(R.string.esborra_foto_text)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmant = false
                    viewModel.esborra()
                }) { Text(stringResource(R.string.esborra)) }
            },
            dismissButton = { TextButton(onClick = { confirmant = false }) { Text(stringResource(R.string.cancela)) } },
        )
    }
}
