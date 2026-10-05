package cat.descobreix.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cat.descobreix.R
import cat.descobreix.joc.model.CodiIne
import cat.descobreix.ui.DialegMissatge
import cat.descobreix.ui.DialegTriaMunicipi
import cat.descobreix.ui.components.BotoPrincipal
import cat.descobreix.ui.components.BotoSecundari
import cat.descobreix.ui.components.Icones
import cat.descobreix.ui.components.Logo
import cat.descobreix.ui.rememberPermisUbicacio
import cat.descobreix.ui.theme.Colors

@Composable
fun OnboardingScreen(
    onComencat: (CodiIne) -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val estat by viewModel.estat.collectAsStateWithLifecycle()
    val demanaUbicacio = rememberPermisUbicacio(
        onConcedit = viewModel::faServirUbicacio,
        onDenegat = viewModel::sensePermis,
    )

    // Tot el contingut es pot desplaçar (en pantalles petites amb el teclat obert no hi cap);
    // el botó per començar queda sempre a sota.
    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LazyColumn(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Logo(null, Modifier.size(52.dp), encen = true)
                    Text(stringResource(R.string.app_name).uppercase(), style = MaterialTheme.typography.labelSmall.copy(color = Colors.Ambre))
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.onboarding_titol), style = MaterialTheme.typography.displayLarge)
                    Text(stringResource(R.string.onboarding_text), style = MaterialTheme.typography.bodyLarge.copy(color = Colors.TextSecundari))
                }
            }
            item {
                OutlinedTextField(
                    value = estat.text,
                    onValueChange = viewModel::canviaText,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.onboarding_cerca)) },
                    leadingIcon = { Icon(Icones.Cercar, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Colors.Ambre,
                        unfocusedBorderColor = Colors.Linia,
                        focusedContainerColor = Colors.Superficie,
                        unfocusedContainerColor = Colors.Superficie,
                        focusedLabelColor = Colors.Ambre,
                        cursorColor = Colors.Ambre,
                    ),
                )
            }
            if (estat.resultats.isNotEmpty()) {
                item {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Colors.Superficie)
                            .border(1.dp, Colors.Linia, RoundedCornerShape(16.dp)),
                    ) {
                        estat.resultats.forEachIndexed { i, opcio ->
                            if (i > 0) HorizontalDivider(color = Colors.Linia)
                            FilaOpcio(opcio, triat = opcio.codi == estat.seleccionat?.codi) { viewModel.tria(opcio) }
                        }
                    }
                }
            }
            item {
                BotoSecundari(
                    text = stringResource(if (estat.buscantUbicacio) R.string.buscant_ubicacio else R.string.onboarding_ubicacio),
                    onClick = demanaUbicacio,
                    icona = Icones.Ubicacio,
                    enabled = !estat.buscantUbicacio,
                )
            }
        }
        estat.seleccionat?.let {
            Text(
                stringResource(R.string.onboarding_triat, it.nom),
                style = MaterialTheme.typography.bodyMedium.copy(color = Colors.Ambre),
            )
        }
        BotoPrincipal(
            text = stringResource(R.string.onboarding_comencar),
            onClick = { viewModel.comenca(onComencat) },
            enabled = estat.seleccionat != null,
            carregant = estat.comencant,
        )
    }

    estat.candidats?.let { c ->
        DialegTriaMunicipi(
            candidats = c.map { it.codi to it.nom },
            onTria = { codi -> c.firstOrNull { it.codi == codi }?.let(viewModel::tria) },
            onCancela = viewModel::tancaCandidats,
        )
    }
    estat.missatge?.let { DialegMissatge(it, viewModel::tancaMissatge) }
}

@Composable
private fun FilaOpcio(opcio: OpcioMunicipi, triat: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .background(if (triat) Colors.Disponible1 else Colors.Superficie)
            .selectable(selected = triat, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(opcio.nom, style = MaterialTheme.typography.bodyLarge)
            Text(opcio.comarca, style = MaterialTheme.typography.bodySmall)
        }
        if (triat) Icon(Icones.Fet, contentDescription = null, tint = Colors.Ambre, modifier = Modifier.size(20.dp))
    }
}
