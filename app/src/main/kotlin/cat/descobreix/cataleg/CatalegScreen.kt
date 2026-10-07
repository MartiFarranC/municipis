package cat.descobreix.cataleg

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cat.descobreix.R
import cat.descobreix.joc.model.CodiIne
import cat.descobreix.ui.components.BotoIcona
import cat.descobreix.ui.components.BotoPrincipal
import cat.descobreix.ui.components.BotoSecundari
import cat.descobreix.ui.components.Carregant
import cat.descobreix.ui.components.Icones
import cat.descobreix.ui.components.ImatgeLocal
import cat.descobreix.ui.theme.ChakraPetch
import cat.descobreix.ui.theme.Colors

/**
 * El catàleg de cartells, com un àlbum de cromos: cada municipi té una casella, i la foto del cartell d'entrada s'hi
 * posa quan es fa. Els municipis descoberts sense foto surten amb el nom i el marc buit; els altres, tapats.
 */
@Composable
fun CatalegScreen(onEnrere: () -> Unit, onObreMunicipi: (CodiIne) -> Unit, viewModel: CatalegViewModel = hiltViewModel()) {
    val estat by viewModel.estat.collectAsStateWithLifecycle()
    if (estat.carregant) {
        Carregant(Modifier.fillMaxSize())
        return
    }
    var oberta by rememberSaveable { mutableStateOf<String?>(null) }
    Box(Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            GridCells.Adaptive(104.dp),
            Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        BotoIcona(Icones.Enrere, stringResource(R.string.enrere), onEnrere)
                        Text(stringResource(R.string.cataleg), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
                    }
                    Text(
                        stringResource(R.string.cataleg_recompte, estat.cromos, estat.total),
                        style = MaterialTheme.typography.bodyMedium.copy(color = Colors.TextSecundari),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for ((o, text) in listOf(OrdreCataleg.COMARQUES to R.string.cataleg_per_comarques, OrdreCataleg.ALFABETIC to R.string.cataleg_alfabetic)) {
                            FilterChip(
                                selected = estat.ordre == o,
                                onClick = { viewModel.canviaOrdre(o) },
                                label = { Text(stringResource(text)) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Colors.Disponible1, selectedLabelColor = Colors.Ambre),
                            )
                        }
                    }
                }
            }
            for (g in estat.grups) {
                if (g.nom != null) {
                    item(key = "c_" + g.nom, span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp, bottom = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(g.nom, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
                            Text(
                                stringResource(R.string.progres_de, g.cromos, g.caselles.size),
                                fontFamily = ChakraPetch,
                                fontWeight = FontWeight.Bold,
                                color = if (g.cromos == g.caselles.size) Colors.Ambre else Colors.TextSecundari,
                            )
                        }
                    }
                }
                items(g.caselles, key = { it.codi }) { c -> CasellaCataleg(c) { oberta = c.codi } }
            }
        }
        val c = oberta?.let { codi -> estat.grups.asSequence().flatMap { it.caselles }.firstOrNull { it.codi == codi } }
        val cromo = c?.cromo
        if (c != null && cromo != null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Colors.Fons.copy(alpha = .96f))
                    .clickable(remember { MutableInteractionSource() }, indication = null) { oberta = null },
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    Modifier
                        .systemBarsPadding()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(c.nom, style = MaterialTheme.typography.titleLarge)
                    ImatgeLocal(
                        cromo.fitxer,
                        stringResource(R.string.cataleg_cartell_de, c.nom),
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.FillWidth,
                        midaMaxima = 2048,
                    )
                    BotoPrincipal(stringResource(R.string.cataleg_ves_al_municipi), {
                        oberta = null
                        onObreMunicipi(c.codi)
                    })
                    BotoSecundari(stringResource(R.string.tanca), { oberta = null })
                }
            }
        }
    }
}

@Composable
private fun CasellaCataleg(c: Casella, onObre: () -> Unit) {
    val forma = RoundedCornerShape(8.dp)
    val base = Modifier
        .fillMaxWidth()
        .aspectRatio(1.5f)
        .clip(forma)
    val cromo = c.cromo
    when {
        cromo != null -> ImatgeLocal(
            cromo.miniatura,
            stringResource(R.string.cataleg_cartell_de, c.nom),
            base.clickable(role = Role.Image, onClick = onObre),
            midaMaxima = 320,
        )
        c.descobert -> {
            val sense = stringResource(R.string.cataleg_sense_foto, c.nom)
            Box(
                base
                    .background(Colors.Superficie)
                    .drawBehind {
                        drawRoundRect(
                            Colors.Linia,
                            cornerRadius = CornerRadius(8.dp.toPx()),
                            style = Stroke(1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))),
                        )
                    }
                    .semantics(mergeDescendants = true) { contentDescription = sense }
                    .padding(6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(c.nom, style = MaterialTheme.typography.labelSmall.copy(color = Colors.TextSecundari), textAlign = TextAlign.Center, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
        }
        else -> {
            val tapat = stringResource(R.string.cataleg_tapat)
            Box(
                base
                    .background(Colors.Boira)
                    .border(1.dp, Colors.VoraBoira, forma)
                    .semantics { contentDescription = tapat },
                contentAlignment = Alignment.Center,
            ) {
                Text("?", fontFamily = ChakraPetch, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Colors.VoraBoira)
            }
        }
    }
}
