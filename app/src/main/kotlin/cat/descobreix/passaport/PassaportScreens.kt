package cat.descobreix.passaport

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cat.descobreix.R
import cat.descobreix.joc.model.CodiIne
import cat.descobreix.sacs.nomPortada
import cat.descobreix.sacs.portada
import cat.descobreix.ui.components.AMPLADA_SEGELL
import cat.descobreix.ui.components.BotoPrincipal
import cat.descobreix.ui.components.BotoSecundari
import cat.descobreix.ui.components.Carregant
import cat.descobreix.ui.components.ColorsPassaport
import cat.descobreix.ui.components.DibuixSegell
import cat.descobreix.ui.components.Icones
import cat.descobreix.ui.components.PROPORCIO_SEGELL
import cat.descobreix.ui.components.PaginaPassaport
import cat.descobreix.ui.components.Silueta
import cat.descobreix.ui.components.TapaDelPassaport
import cat.descobreix.ui.components.rememberSegellPropi
import cat.descobreix.ui.theme.Colors
import cat.descobreix.ui.theme.LocalReduirAnimacions
import kotlin.math.cos
import kotlin.math.sin

/** El passaport: la tapa (es tria el color), les pàgines de cada comarca i els segells pendents. */
@Composable
fun PassaportScreen(
    onEnrere: () -> Unit,
    onSegella: (CodiIne) -> Unit,
    viewModel: PassaportViewModel = hiltViewModel(),
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
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BotoSecundari(stringResource(R.string.enrere), onEnrere, icona = Icones.Enrere)
            }
        }
        item {
            Text(stringResource(R.string.passaport), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
            Text(
                stringResource(R.string.passaport_segellats, estat.segells, estat.total),
                style = MaterialTheme.typography.bodyMedium.copy(color = Colors.TextSecundari),
            )
        }
        item {
            TapaDelPassaport(
                estat.tapa,
                estat.siluetaCatalunya,
                stringResource(R.string.passaport_tapa_titol),
                stringResource(R.string.passaport_tapa_subtitol),
                Modifier
                    .fillMaxWidth(.6f)
                    .padding(top = 4.dp),
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.passaport_color_tapa), style = MaterialTheme.typography.titleMedium)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (t in estat.tapes) OpcioTapa(t, t == estat.tapa) { viewModel.triaTapa(t) }
                }
            }
        }
        if (estat.pendents.isNotEmpty()) {
            item { Text(stringResource(R.string.passaport_pendents), style = MaterialTheme.typography.titleMedium) }
            items(estat.pendents, key = { "p" + it.codi }) { p ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Colors.Superficie)
                        .border(1.dp, Colors.Linia, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(p.nom, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    BotoPrincipal(stringResource(R.string.passaport_segella), { onSegella(p.codi) })
                }
            }
        }
        if (estat.pagines.isEmpty()) {
            item { Text(stringResource(R.string.passaport_buit), style = MaterialTheme.typography.bodyMedium.copy(color = Colors.TextSecundari)) }
        }
        items(estat.pagines, key = { it.comarca + "_" + it.numero }) { p ->
            PaginaPassaport(
                p.nom,
                stringResource(R.string.progres_de, p.recompte, p.total),
                stringResource(R.string.passaport_pagina, p.numero),
                estat.siluetesComarques[p.comarca],
                p.segells,
                Modifier.widthIn(max = 440.dp),
            )
        }
    }
}

@Composable
private fun OpcioTapa(t: String, triada: Boolean, onTria: () -> Unit) {
    val p = portada(t)
    val nom = nomPortada(t)
    Column(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.RadioButton, onClickLabel = nom, onClick = onTria)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            Modifier
                .size(44.dp, 60.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(p.fons)
                .border(if (triada) 3.dp else 1.dp, if (triada) Colors.Ambre else p.daurat.copy(alpha = .5f), RoundedCornerShape(6.dp)),
        ) {
            p.dibuix?.let { Image(painterResource(it), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
        }
        Text(
            nom,
            style = MaterialTheme.typography.labelMedium.copy(color = if (triada) Colors.Ambre else Colors.Text),
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.width(72.dp),
        )
    }
}

/**
 * Posar un segell: el passaport s'obre per la pàgina de la comarca, l'usuari toca on vol el segell,
 * el segell hi cau sense tampó (pica, fa una onada i esquitxa) i l'usuari l'accepta o el repeteix.
 */
@Composable
fun SegellarScreen(onFet: () -> Unit, viewModel: SegellarViewModel = hiltViewModel()) {
    val estat by viewModel.estat.collectAsStateWithLifecycle()
    val reduir = LocalReduirAnimacions.current
    if (estat.carregant) {
        Carregant(Modifier.fillMaxSize())
        return
    }
    LaunchedEffect(estat.fase) { if (estat.fase == FaseSegellar.FET) onFet() }

    val tapa = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        if (!reduir) tapa.animateTo(-165f, tween(1100, delayMillis = 350, easing = CubicBezierEasing(.5f, 0f, .3f, 1f)))
        viewModel.obert()
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Colors.Fons)
            .systemBarsPadding()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            when {
                estat.jaTeSegell -> stringResource(R.string.segellar_ja_te, estat.nom)
                estat.fase == FaseSegellar.OBRINT -> stringResource(R.string.segellar_obrint)
                estat.fase == FaseSegellar.TRIANT -> stringResource(R.string.segellar_toca, estat.nom)
                else -> stringResource(R.string.segellar_decideix)
            },
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Box(Modifier.widthIn(max = 420.dp)) {
            PaginaPassaport(
                estat.nomComarca,
                stringResource(R.string.progres_de, estat.segellsComarca + if (estat.provisional != null) 1 else 0, estat.total),
                stringResource(R.string.passaport_pagina, estat.pagina + 1),
                estat.siluetaComarca,
                estat.segells,
                Modifier.pointerInput(estat.fase) {
                    detectTapGestures { o -> viewModel.toca(o.x / size.width, o.y / size.height) }
                },
            ) { amplada, alcada ->
                estat.provisional?.let { p ->
                    SegellQueCau(estat.nom, estat.siluetaMunicipi, rememberSegellPropi(estat.codi), p.x, p.y, p.gir, p.tinta, amplada, alcada, reduir)
                }
            }
            if (tapa.value > -165f && !reduir) {
                TapaDelPassaport(
                    estat.tapa,
                    estat.siluetaCatalunya,
                    stringResource(R.string.passaport_tapa_titol),
                    stringResource(R.string.passaport_tapa_subtitol),
                    Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            transformOrigin = TransformOrigin(0f, .5f)
                            rotationY = tapa.value
                            cameraDistance = 14f * density
                            alpha = if (tapa.value < -90f) 0f else 1f
                        },
                )
            }
        }
        if (estat.fase == FaseSegellar.DECIDINT) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BotoSecundari(stringResource(R.string.segellar_repeteix), viewModel::repeteix)
                BotoPrincipal(stringResource(R.string.segellar_accepta), viewModel::accepta)
            }
        }
        if (estat.jaTeSegell) BotoPrincipal(stringResource(R.string.continua), onFet)
    }
}

/** El segell nou: cau des de dalt, pica sobre el paper, fa una onada i esquitxa unes gotes de tinta. */
@Composable
private fun SegellQueCau(
    nom: String,
    silueta: Silueta?,
    propi: ImageBitmap?,
    x: Float,
    y: Float,
    gir: Float,
    tinta: Int,
    amplada: Dp,
    alcada: Dp,
    reduir: Boolean,
) {
    val caiguda = remember(x, y) { Animatable(if (reduir) 1f else 0f) }
    val impacte = remember(x, y) { Animatable(if (reduir) 1f else 0f) }
    LaunchedEffect(x, y) {
        if (!reduir) {
            caiguda.animateTo(1f, tween(380, easing = CubicBezierEasing(.5f, 0f, .75f, 0f)))
            impacte.animateTo(1f, tween(450, easing = LinearOutSlowInEasing))
        }
    }
    val color = ColorsPassaport.tinta(tinta)
    val w = amplada * AMPLADA_SEGELL
    val h = w * PROPORCIO_SEGELL
    // Onada i esquitxades, només un moment després de picar.
    if (impacte.value in .001f..0.999f) {
        Canvas(Modifier.size(amplada, alcada)) {
            val centre = Offset(size.width * x, size.height * y)
            val e = impacte.value
            val sw = w.toPx() * (1f + .25f * e)
            val sh = h.toPx() * (1f + .25f * e)
            drawRoundRect(
                color.copy(alpha = .7f * (1f - e)),
                topLeft = Offset(centre.x - sw / 2, centre.y - sh / 2),
                size = Size(sw, sh),
                cornerRadius = CornerRadius(12f),
                style = Stroke(3.dp.toPx()),
            )
            for (i in 0 until 7) {
                val angle = i * 0.9f + gir / 10f
                val d = (60f + (i * 13) % 40) * density * e
                drawCircle(color.copy(alpha = .9f * (1f - e)), 3.dp.toPx() * (1f - .4f * e), Offset(centre.x + cos(angle) * d, centre.y + sin(angle) * d * .6f))
            }
        }
    }
    val p = caiguda.value
    DibuixSegell(
        nom,
        dataSegell(System.currentTimeMillis()),
        silueta,
        color,
        propi = propi,
        modifier = Modifier
            .size(w, h)
            .offset(amplada * x - w / 2, alcada * y - h / 2)
            .graphicsLayer {
                val escala = 1.9f - .9f * FastOutSlowInEasing.transform(p)
                scaleX = escala
                scaleY = escala
                rotationZ = gir - 8f * (1f - p)
                alpha = .9f * p
            },
    )
}
