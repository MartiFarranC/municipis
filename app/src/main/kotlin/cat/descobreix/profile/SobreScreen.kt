package cat.descobreix.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import cat.descobreix.R
import cat.descobreix.ui.components.BotoIcona
import cat.descobreix.ui.components.Icones
import cat.descobreix.ui.theme.Colors

/** "Sobre l'app": atribucions de les fonts de dades i de les tipografies. */
@Composable
fun SobreScreen(onEnrere: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Colors.Fons)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BotoIcona(Icones.Enrere, stringResource(R.string.enrere), onEnrere)
            Text(stringResource(R.string.sobre_app), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
        }
        Text(stringResource(R.string.sobre_text), style = MaterialTheme.typography.bodyLarge)
        Apartat(R.string.atribucio_limits_titol, R.string.atribucio_limits)
        Apartat(R.string.atribucio_wikidata_titol, R.string.atribucio_wikidata)
        Apartat(R.string.atribucio_osm_titol, R.string.atribucio_osm)
        Apartat(R.string.atribucio_fonts_titol, R.string.atribucio_fonts)
        Apartat(R.string.privacitat_titol, R.string.privacitat)
    }
}

@Composable
private fun Apartat(titol: Int, text: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(titol), style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
        Text(stringResource(text), style = MaterialTheme.typography.bodyMedium.copy(color = Colors.TextSecundari))
    }
}
