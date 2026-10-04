package cat.descobreix.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cat.descobreix.R
import cat.descobreix.data.ranquing.CriteriRanquing
import cat.descobreix.data.ranquing.FilaRanquing
import cat.descobreix.data.ranquing.LIMIT_RANQUING
import cat.descobreix.ui.components.Avatar
import cat.descobreix.ui.components.BotoSecundari
import cat.descobreix.ui.components.Carregant
import cat.descobreix.ui.plural
import cat.descobreix.ui.theme.Colors
import cat.descobreix.ui.theme.Secundari

/** Pestanya del rànquing al perfil (secció 4, pantalla 7): per punts o per municipis, general o entre amics. */
@Composable
fun RanquingPestanya(viewModel: RanquingViewModel = hiltViewModel()) {
    val estat by viewModel.estat.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Selector(
                opcions = listOf(CriteriRanquing.PUNTS to R.string.ranquing_punts, CriteriRanquing.MUNICIPIS to R.string.ranquing_municipis),
                triat = estat.criteri,
                onTria = viewModel::canviaCriteri,
            )
            Selector(
                opcions = listOf(false to R.string.ranquing_general, true to R.string.ranquing_amics),
                triat = estat.nomesAmics,
                onTria = viewModel::canviaAmbit,
            )
        }

        when {
            estat.carregant -> Carregant(Modifier.fillMaxSize())
            estat.error -> Missatge(stringResource(R.string.ranquing_error)) {
                BotoSecundari(stringResource(R.string.torna_a_provar), viewModel::actualitza)
            }
            else -> LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                itemsIndexed(estat.files, key = { _, f -> f.usuariId }) { i, f ->
                    // La fila de l'usuari, quan no és entre les primeres, va separada de la resta.
                    if (f.socJo && i == LIMIT_RANQUING) {
                        Text("···", Modifier.fillMaxWidth(), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                    }
                    FilaClassificacio(f, estat.criteri)
                }
                if (estat.senseAmics) {
                    item { Missatge(stringResource(R.string.ranquing_sense_amics)) {} }
                }
                item {
                    Text(
                        stringResource(R.string.ranquing_nota),
                        Modifier.padding(top = 12.dp),
                        style = MaterialTheme.typography.bodySmall.copy(color = Colors.TextSecundari),
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> Selector(opcions: List<Pair<T, Int>>, triat: T, onTria: (T) -> Unit) {
    val colors = SegmentedButtonDefaults.colors(
        activeContainerColor = Colors.Secundari.copy(alpha = 0.18f),
        activeContentColor = Colors.Secundari,
        activeBorderColor = Colors.Secundari,
        inactiveContainerColor = Colors.Superficie,
        inactiveContentColor = Colors.TextSecundari,
        inactiveBorderColor = Colors.Linia,
    )
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        opcions.forEachIndexed { i, (valor, text) ->
            SegmentedButton(
                selected = valor == triat,
                onClick = { onTria(valor) },
                shape = SegmentedButtonDefaults.itemShape(i, opcions.size),
                colors = colors,
                modifier = Modifier.heightIn(min = 48.dp),
            ) { Text(stringResource(text)) }
        }
    }
}

@Composable
private fun FilaClassificacio(f: FilaRanquing, criteri: CriteriRanquing) {
    val valor = when (criteri) {
        CriteriRanquing.PUNTS -> stringResource(R.string.punts_curt, f.punts)
        CriteriRanquing.MUNICIPIS -> plural(R.plurals.ranquing_n_municipis, f.municipis.toInt(), f.municipis)
    }
    val nom = if (f.socJo) stringResource(R.string.ranquing_tu, f.nomUsuari) else f.nomUsuari
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (f.socJo) Colors.Disponible1 else Colors.Superficie)
            .then(if (f.socJo) Modifier.border(1.dp, Colors.Ambre, RoundedCornerShape(12.dp)) else Modifier)
            .semantics(mergeDescendants = true) {}
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "${f.posicio}",
            Modifier.width(32.dp),
            style = MaterialTheme.typography.titleMedium.copy(color = if (f.posicio <= 3) Colors.Ambre else Colors.TextSecundari),
            textAlign = TextAlign.End,
        )
        Avatar(f.foto, f.nomUsuari, 40.dp)
        Text(nom, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(valor, style = MaterialTheme.typography.titleMedium.copy(color = if (f.socJo) Colors.Ambre else Colors.Text))
    }
}

@Composable
private fun Missatge(text: String, accio: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium.copy(color = Colors.TextSecundari), textAlign = TextAlign.Center)
        accio()
    }
}
