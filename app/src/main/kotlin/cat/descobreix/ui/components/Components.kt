package cat.descobreix.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cat.descobreix.R
import cat.descobreix.ui.theme.ChakraPetch
import cat.descobreix.ui.theme.Colors

@Composable
fun BotoPrincipal(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    carregant: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !carregant,
        modifier = modifier.fillMaxWidth().heightIn(min = 56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Colors.Ambre,
            contentColor = Colors.TintaAmbre,
            disabledContainerColor = Colors.Superficie2,
            disabledContentColor = Colors.TextSecundari,
        ),
    ) {
        if (carregant) {
            CircularProgressIndicator(Modifier.size(22.dp), color = Colors.TintaAmbre, strokeWidth = 2.dp)
        } else {
            Text(text, fontFamily = ChakraPetch, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium.copy(color = Color.Unspecified))
        }
    }
}

@Composable
fun BotoSecundari(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icona: ImageVector? = null,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Colors.Linia),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Colors.Blau, disabledContentColor = Colors.TextSecundari),
    ) {
        if (icona != null) {
            Icon(icona, contentDescription = null, modifier = Modifier.size(18.dp))
            Box(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge.copy(color = Color.Unspecified))
    }
}

/** Botó d'icona quadrat (48 dp) amb fons de superfície, com els del disseny. */
@Composable
fun BotoIcona(
    icona: ImageVector,
    descripcio: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Colors.Text,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Colors.Superficie2)
            .border(1.dp, Colors.Linia, RoundedCornerShape(12.dp)),
        colors = IconButtonDefaults.iconButtonColors(contentColor = tint),
    ) {
        Icon(icona, contentDescription = descripcio, modifier = Modifier.size(22.dp))
    }
}

@Composable
fun Targeta(
    modifier: Modifier = Modifier,
    fons: Color = Colors.Superficie,
    vora: Color = Colors.Linia,
    content: @Composable () -> Unit,
) {
    Box(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(fons)
            .border(1.dp, vora, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) { content() }
}

@Composable
fun BarraProgres(
    progres: Float,
    modifier: Modifier = Modifier,
    color: Color = Colors.Ambre,
    alcada: Int = 8,
) {
    val p = progres.coerceIn(0f, 1f)
    Box(
        modifier
            .fillMaxWidth()
            .height(alcada.dp)
            .clip(RoundedCornerShape((alcada / 2).dp))
            .background(Colors.Superficie2)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(p, 0f..1f) },
    ) {
        Box(
            Modifier
                .fillMaxWidth(p)
                .height(alcada.dp)
                .background(color),
        )
    }
}

/** Petita xifra amb etiqueta, per a les estadístiques. */
@Composable
fun Xifra(valor: String, etiqueta: String, modifier: Modifier = Modifier, color: Color = Colors.Text) {
    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Colors.Superficie)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(valor, style = MaterialTheme.typography.headlineSmall.copy(color = color))
        Text(etiqueta, style = MaterialTheme.typography.bodySmall, maxLines = 1)
    }
}

@Composable
fun FilaTitol(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

@Composable
fun Carregant(modifier: Modifier = Modifier) {
    val text = stringResource(R.string.carregant)
    Box(modifier.semantics { contentDescription = text }, contentAlignment = Alignment.Center) {
        SardanaCarregant(Modifier.size(160.dp))
    }
}

/** La pantalla d'obertura: el fanal s'encén i la rotllana balla mentre l'app es prepara. */
@Composable
fun Obertura(modifier: Modifier = Modifier) {
    Column(
        modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Logo(stringResource(R.string.logo_descripcio), Modifier.size(150.dp), encen = true)
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
        SardanaCarregant(Modifier.size(120.dp))
    }
}
