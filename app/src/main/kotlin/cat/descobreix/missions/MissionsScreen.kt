package cat.descobreix.missions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cat.descobreix.R
import cat.descobreix.joc.model.CodiIne
import cat.descobreix.ui.components.BarraProgres
import cat.descobreix.ui.components.Carregant
import cat.descobreix.ui.components.Icones
import cat.descobreix.ui.theme.Colors

@Composable
fun MissionsScreen(
    onObreMunicipi: (CodiIne) -> Unit,
    viewModel: MissionsViewModel = hiltViewModel(),
) {
    val estat by viewModel.estat.collectAsStateWithLifecycle()
    if (estat.carregant) {
        Carregant(Modifier.fillMaxSize())
        return
    }
    LazyColumn(
        Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                Text(stringResource(R.string.pestanya_missions), style = MaterialTheme.typography.displaySmall, modifier = Modifier.semantics { heading() })
                Text(stringResource(R.string.missions_resum, estat.missionsFetes, estat.saldo), style = MaterialTheme.typography.bodyMedium.copy(color = Colors.TextSecundari))
            }
        }
        items(estat.municipis, key = { it.codi }) { m ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (m.complet) Colors.FonsCompletada else Colors.Superficie)
                    .border(1.dp, if (m.complet) Colors.Disponible2 else Colors.Linia, RoundedCornerShape(14.dp))
                    .clickable(role = Role.Button) { onObreMunicipi(m.codi) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(m.nom, style = MaterialTheme.typography.titleLarge)
                    Text(
                        stringResource(R.string.missions_municipi, m.comarca, m.fetes, m.total, m.punts, m.puntsPossibles),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    BarraProgres(if (m.puntsPossibles == 0) 0f else m.punts.toFloat() / m.puntsPossibles, alcada = 6)
                }
                if (m.complet) {
                    Icon(Icones.Fet, contentDescription = stringResource(R.string.completat), tint = Colors.Ambre, modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}
