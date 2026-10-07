package cat.descobreix.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.descobreix.R
import cat.descobreix.domain.MedallesNoves
import cat.descobreix.joc.progressio.Medalla
import cat.descobreix.joc.progressio.NivellMedalla
import cat.descobreix.joc.progressio.TipusMedalla
import cat.descobreix.ui.components.BotoPrincipal
import cat.descobreix.ui.components.DibuixMedalla
import cat.descobreix.ui.components.IconaBarretina
import cat.descobreix.ui.components.SardanaPremi
import cat.descobreix.ui.components.Silueta
import cat.descobreix.ui.theme.ChakraPetch
import cat.descobreix.ui.theme.Colors
import cat.descobreix.ui.theme.LocalReduirAnimacions

/** El nom d'una medalla: la comarca, o la fita («100 municipis»). */
@Composable
fun titolMedalla(m: Medalla, nomComarca: String?): String = when (m.tipus) {
    TipusMedalla.COMARCA -> nomComarca.orEmpty()
    TipusMedalla.MUNICIPIS -> plural(R.plurals.medalla_municipis, m.objectiu, m.objectiu)
    TipusMedalla.CAPITALS -> stringResource(R.string.medalla_capitals)
    TipusMedalla.CARTELLS -> plural(R.plurals.medalla_cartells, m.objectiu, m.objectiu)
}

/** El text de la banda de la medalla, en majúscules. */
@Composable
fun etiquetaMedalla(m: Medalla, nomComarca: String?): String = when (m.tipus) {
    TipusMedalla.CAPITALS -> stringResource(R.string.medalla_capitals_etiqueta)
    else -> titolMedalla(m, nomComarca).uppercase()
}

@Composable
fun nomNivell(n: NivellMedalla): String = stringResource(
    when (n) {
        NivellMedalla.BRONZE -> R.string.medalla_bronze
        NivellMedalla.PLATA -> R.string.medalla_plata
        NivellMedalla.OR -> R.string.medalla_or
    },
)

/** Què falta per al nivell següent (o «Aconseguit» si ja no n'hi ha més). */
@Composable
fun progresMedalla(m: Medalla): String = when {
    m.completa -> stringResource(R.string.aconseguit)
    m.tipus != TipusMedalla.COMARCA -> stringResource(R.string.progres_de, m.actual, m.necessari)
    m.nivell == null -> stringResource(R.string.medalla_cap_a_bronze, m.actual, m.necessari)
    m.nivell == NivellMedalla.BRONZE -> stringResource(R.string.medalla_cap_a_plata, m.actual, m.necessari)
    else -> stringResource(R.string.medalla_cap_a_or, m.actual, m.necessari)
}

/** Una medalla de la vitrina: el dibuix, el nom i el progrés. */
@Composable
fun TargetaMedalla(m: Medalla, nomComarca: String?, silueta: Silueta?, modifier: Modifier = Modifier) {
    val titol = titolMedalla(m, nomComarca)
    val nivell = m.nivell
    val estat = if (nivell != null && m.tipus == TipusMedalla.COMARCA) nomNivell(nivell) else null
    val progres = progresMedalla(m)
    val descripcio = listOfNotNull(titol, estat, progres).joinToString(". ")
    Column(
        modifier
            .width(104.dp)
            .semantics(mergeDescendants = true) { contentDescription = descripcio },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        DibuixMedalla(m, etiquetaMedalla(m, nomComarca), silueta, Modifier.size(88.dp))
        Text(titol, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center, maxLines = 2)
        Text(
            listOfNotNull(estat, progres.takeUnless { m.completa && estat != null }).joinToString(" · "),
            style = MaterialTheme.typography.bodySmall.copy(color = if (m.aconseguida) Colors.Ambre else Colors.TextSecundari),
            textAlign = TextAlign.Center,
        )
    }
}

/** Celebració quan es guanyen medalles: la sardana fa festa, surten les medalles i els punts que donen. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CelebracioMedalles(noves: MedallesNoves, nomsComarques: Map<String, String>, siluetes: Map<String, Silueta>, onTanca: () -> Unit) {
    val reduir = LocalReduirAnimacions.current
    val compte = remember(noves) { Animatable(if (reduir) noves.punts.toFloat() else 0f) }
    LaunchedEffect(noves) { compte.animateTo(noves.punts.toFloat(), tween(1000)) }
    Box(
        Modifier
            .fillMaxSize()
            .background(Colors.Fons.copy(alpha = .94f))
            // Absorbeix els tocs perquè no arribin a la pantalla de sota.
            .clickable(remember { MutableInteractionSource() }, indication = null, onClick = {}),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .systemBarsPadding()
                .padding(24.dp)
                .widthIn(max = 360.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SardanaPremi(Modifier.size(150.dp))
            Text(
                plural(R.plurals.celebracio_medalles, noves.medalles.size, noves.medalles.size),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (m in noves.medalles) {
                    val nom = m.comarca?.let { nomsComarques[it] }
                    val nivell = m.nivell
                    Column(
                        Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Colors.Superficie)
                            .border(1.dp, Colors.Linia, RoundedCornerShape(16.dp))
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        DibuixMedalla(m, etiquetaMedalla(m, nom), m.comarca?.let { siluetes[it] }, Modifier.size(if (noves.medalles.size > 2) 84.dp else 120.dp))
                        val titol = titolMedalla(m, nom)
                        Text(
                            if (nivell != null && m.tipus == TipusMedalla.COMARCA) stringResource(R.string.medalla_nivell_de, titol, nomNivell(nivell)) else titol,
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            if (noves.punts > 0) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.celebracio_punts, compte.value.toInt()),
                        fontFamily = ChakraPetch,
                        fontWeight = FontWeight.Bold,
                        fontSize = 40.sp,
                        color = Colors.Ambre,
                    )
                    IconaBarretina(Modifier.size(38.dp))
                }
            }
            BotoPrincipal(stringResource(R.string.continua), onTanca, Modifier.padding(top = 8.dp))
        }
    }
}
