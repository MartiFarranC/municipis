package cat.descobreix.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cat.descobreix.R
import cat.descobreix.gent.CompteSocial
import cat.descobreix.ui.components.BotoSecundari
import cat.descobreix.ui.components.Icones
import cat.descobreix.ui.theme.Colors

/** El perfil de l'Espectador, que no juga: el nom, el compte, la gent que segueix i sortir. */
@Composable
fun PerfilEspectadorScreen(onObreSeguits: () -> Unit, onObreSobre: () -> Unit, viewModel: PerfilViewModel = hiltViewModel()) {
    val estat by viewModel.estat.collectAsStateWithLifecycle()
    var confirmantSortir by rememberSaveable { mutableStateOf(false) }
    LazyColumn(
        Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Column {
                Text(estat.nomUsuari.orEmpty(), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
                Text(stringResource(R.string.perfil_espectador), style = MaterialTheme.typography.bodyMedium.copy(color = Colors.TextSecundari))
            }
        }
        item { CompteSocial(onObreSeguits) }
        item { BotoSecundari(stringResource(R.string.sobre_app), onObreSobre, icona = Icones.Info) }
        item { BotoSecundari(stringResource(R.string.compte_surt), { confirmantSortir = true }, icona = Icones.Perfil, enabled = !estat.treballant) }
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
}
