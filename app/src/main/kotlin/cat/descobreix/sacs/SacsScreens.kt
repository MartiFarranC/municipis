package cat.descobreix.sacs

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cat.descobreix.R
import cat.descobreix.data.repositori.TapesPassaport
import cat.descobreix.joc.config.ConfiguracioJoc.Objecte
import cat.descobreix.joc.config.ConfiguracioJoc.Raresa
import cat.descobreix.joc.config.ConfiguracioJoc.TipusObjecte
import cat.descobreix.ui.Efectes
import cat.descobreix.ui.LocalEfectes
import cat.descobreix.ui.components.BotoIcona
import cat.descobreix.ui.components.BotoPrincipal
import cat.descobreix.ui.components.BotoSecundari
import cat.descobreix.ui.components.Carregant
import cat.descobreix.ui.components.IconaBarretina
import cat.descobreix.ui.components.Icones
import cat.descobreix.ui.components.SardanaCarregant
import cat.descobreix.ui.components.ombraDura
import cat.descobreix.ui.plural
import cat.descobreix.ui.theme.Archivo
import cat.descobreix.ui.theme.Caveat
import cat.descobreix.ui.theme.ColorSecundari
import cat.descobreix.ui.theme.Colors
import cat.descobreix.ui.theme.LocalReduirAnimacions

/** Els sacs per obrir i la col·lecció: el que ha sortit dels sacs, i triar el color, l'animació i la tapa. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SacsScreen(onEnrere: () -> Unit, obreAra: Boolean = false, viewModel: SacsViewModel = hiltViewModel()) {
    val estat by viewModel.estat.collectAsStateWithLifecycle()
    if (estat.carregant) {
        Carregant(Modifier.fillMaxSize())
        return
    }
    // Si s'hi arriba des de l'avís de sacs nous, el primer ja surt per obrir.
    LaunchedEffect(obreAra) { if (obreAra) viewModel.preparaSac() }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BotoIcona(Icones.Enrere, stringResource(R.string.enrere), onEnrere)
                    Text(stringResource(R.string.sacs_titol), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
                }
            }
            item { PerObrir(estat.perObrir.size, viewModel::preparaSac) }
            item {
                Text(
                    stringResource(R.string.sacs_col_leccio, estat.sortides, estat.delsSacs),
                    style = MaterialTheme.typography.bodyMedium.copy(color = Colors.TextSecundari),
                )
            }
            item { Seccio(nomTipus(TipusObjecte.COLOR), stringResource(R.string.sacs_ajuda_colors)) }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Fitxa(stringResource(R.string.color_groc), estat.color == null, onTria = { viewModel.colorDeSempre() }) {
                        Mostra(ColorSecundari.Groc)
                    }
                    for (o in estat.tot.filter { it.tipus == TipusObjecte.COLOR }) {
                        FitxaObjecte(o, o in estat.tinc, estat.color == o.id) { viewModel.faServir(o) }
                    }
                }
            }
            item { Seccio(nomTipus(TipusObjecte.ANIMACIO), stringResource(R.string.sacs_ajuda_animacions)) }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Fitxa(stringResource(R.string.animacio_sardana), estat.animacio == null, onTria = { viewModel.animacioDeSempre() }) {
                        SardanaCarregant(Modifier.fillMaxSize())
                    }
                    for (o in estat.tot.filter { it.tipus == TipusObjecte.ANIMACIO }) {
                        FitxaObjecte(o, o in estat.tinc, estat.animacio == o.id) { viewModel.faServir(o) }
                    }
                }
            }
            item { Seccio(nomTipus(TipusObjecte.PORTADA), stringResource(R.string.sacs_ajuda_portades)) }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (t in TapesPassaport.classiques) {
                        Fitxa(nomPortada(t), estat.tapa == t, onTria = { viewModel.faServir(Objecte(TipusObjecte.PORTADA, t, Raresa.INICIAL)) }) {
                            MiniPortada(t)
                        }
                    }
                    for (o in estat.tot.filter { it.tipus == TipusObjecte.PORTADA }) {
                        FitxaObjecte(o, o in estat.tinc, estat.tapa == o.id) { viewModel.faServir(o) }
                    }
                }
            }
            item { Seccio(nomTipus(TipusObjecte.EMOJI), stringResource(R.string.sacs_ajuda_emojis)) }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (o in estat.tot.filter { it.tipus == TipusObjecte.EMOJI }) FitxaObjecte(o, o in estat.tinc, false, null)
                }
            }
        }
        val obrint = estat.obrint
        if (obrint != null) {
            ObrintSac(
                sortit = estat.sortit,
                queden = estat.perObrir.count { it != obrint },
                onObre = viewModel::obre,
                onFesServir = { o ->
                    viewModel.faServir(o)
                    viewModel.tancaSac()
                },
                onTanca = viewModel::tancaSac,
                onUnAltre = {
                    viewModel.tancaSac()
                    viewModel.preparaSac()
                },
            )
        }
    }
}

@Composable
private fun PerObrir(n: Int, onObre: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RectangleShape)
            .background(Colors.Superficie)
            .border(1.dp, Colors.Linia, RectangleShape)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        DibuixSac(Modifier.size(72.dp), gris = n == 0)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                if (n == 0) stringResource(R.string.sacs_cap) else plural(R.plurals.sacs_per_obrir, n, n),
                style = MaterialTheme.typography.titleMedium,
            )
            if (n == 0) {
                Text(stringResource(R.string.sacs_com_es_guanyen), style = MaterialTheme.typography.bodySmall.copy(color = Colors.TextSecundari))
            } else {
                BotoPrincipal(stringResource(R.string.sacs_obre), onObre)
            }
        }
    }
}

@Composable
private fun Seccio(titol: String, ajuda: String) {
    Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(titol, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
        Text(ajuda, style = MaterialTheme.typography.bodySmall.copy(color = Colors.TextSecundari))
    }
}

/** Una fitxa de la col·lecció: el dibuix, el nom i, si es pot triar, si és la triada. */
@Composable
private fun Fitxa(nom: String, triada: Boolean, onTria: (() -> Unit)?, vora: Color = Colors.Linia, dibuix: @Composable () -> Unit) {
    val forma = RectangleShape
    Column(
        Modifier
            .width(92.dp)
            .clip(forma)
            .background(Colors.Superficie)
            .border(if (triada) 2.dp else 1.dp, if (triada) Colors.AmbreText else vora, forma)
            .then(if (onTria != null) Modifier.clickable(role = Role.RadioButton, onClickLabel = nom, onClick = onTria) else Modifier)
            .semantics(mergeDescendants = true) { selected = triada }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) { dibuix() }
        Text(nom, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, maxLines = 2)
    }
}

/** Una cosa dels sacs: si encara no es té, surt amagada (només se'n veu la raresa). */
@Composable
private fun FitxaObjecte(o: Objecte, laTinc: Boolean, triada: Boolean, onTria: (() -> Unit)?) {
    if (!laTinc) {
        val amagada = stringResource(R.string.sacs_amagada, nomRaresa(o.raresa))
        Fitxa("?", false, null, colorRaresa(o.raresa).copy(alpha = .35f)) {
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(RectangleShape)
                    .background(Colors.Superficie2)
                    .semantics { contentDescription = amagada },
                contentAlignment = Alignment.Center,
            ) {
                Text("?", fontFamily = Archivo, fontWeight = FontWeight.Bold, fontSize = 26.sp, color = colorRaresa(o.raresa).copy(alpha = .6f))
            }
        }
        return
    }
    Fitxa(nomObjecte(o), triada, onTria, if (o.raresa == Raresa.INICIAL) Colors.Linia else colorRaresa(o.raresa).copy(alpha = .6f)) {
        DibuixObjecte(o, Modifier.fillMaxSize())
    }
}

/** El dibuix d'una cosa de la col·lecció. */
@Composable
fun DibuixObjecte(o: Objecte, modifier: Modifier = Modifier) {
    when (o.tipus) {
        TipusObjecte.EMOJI -> dibuixEmoji(o.id)?.let { Image(painterResource(it), null, modifier) }
        TipusObjecte.PORTADA -> Box(modifier, contentAlignment = Alignment.Center) { MiniPortada(o.id) }
        TipusObjecte.ANIMACIO -> AnimacioCarrega(o.id, modifier)
        TipusObjecte.COLOR -> Box(modifier, contentAlignment = Alignment.Center) { Mostra(colorSac(o.id) ?: ColorSecundari.Groc) }
    }
}

@Composable
private fun Mostra(c: Color) {
    Box(
        Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(c)
            .border(2.dp, Colors.Fons, CircleShape),
    )
}

@Composable
private fun MiniPortada(id: String) {
    val p = portada(id)
    Box(
        Modifier
            .size(44.dp, 62.dp)
            .clip(RectangleShape)
            .background(p.fons)
            .border(1.dp, p.daurat.copy(alpha = .6f), RectangleShape),
    ) {
        p.dibuix?.let { Image(painterResource(it), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
    }
}

/** Les fases d'obrir un sac. */
private enum class Fase { TANCAT, SACSEJANT, OBERT }

/**
 * Obrir un sac (docs/disseny.md): el sac surt tancat i es balanceja. Quan l'usuari el toca, es demana què hi ha a dins
 * i el sac tremola una vegada si és comú, dues si és rar i tres si és llegendari. Després la corda cau, el coll
 * s'eixampla, el sac s'aixafa i en surt un cromo que puja i es gira.
 */
@Composable
private fun ObrintSac(
    sortit: Sortit?,
    queden: Int,
    onObre: () -> Unit,
    onFesServir: (Objecte) -> Unit,
    onTanca: () -> Unit,
    onUnAltre: () -> Unit,
) {
    val reduir = LocalReduirAnimacions.current
    val efectes = LocalEfectes.current
    var fase by remember { mutableStateOf(if (sortit != null) Fase.OBERT else Fase.TANCAT) }
    val transicio = rememberInfiniteTransition(label = "sac")
    val balanceig by transicio.animateFloat(-1f, 1f, infiniteRepeatable(tween(1300), RepeatMode.Reverse), label = "balanceig")
    val sacseig = remember { Animatable(0f) }
    val obertura = remember { Animatable(if (sortit != null) 1f else 0f) }
    val cromo = remember { Animatable(if (sortit != null || reduir) 1f else 0f) }
    LaunchedEffect(fase, sortit) {
        if (fase != Fase.SACSEJANT || sortit == null) return@LaunchedEffect
        val cops = when (sortit) {
            is Sortit.Nou -> when (sortit.objecte.raresa) {
                Raresa.LLEGENDARIA -> 3
                Raresa.RARA -> 2
                else -> 1
            }
            is Sortit.Punts -> 1
        }
        if (!reduir) {
            repeat(cops) {
                efectes?.fes(Efectes.So.SACSEIG)
                for (g in listOf(-10f, 9f, -6f, 3f, 0f)) sacseig.animateTo(g, tween(70))
            }
        }
        efectes?.fes(Efectes.So.OBRE)
        obertura.animateTo(1f, tween(if (reduir) 0 else 450))
        fase = Fase.OBERT
        efectes?.fes(Efectes.So.CROMO)
        cromo.animateTo(1f, tween(if (reduir) 0 else 850, easing = FastOutSlowInEasing))
    }
    val tocaText = stringResource(R.string.sacs_toca)
    Box(
        Modifier
            .fillMaxSize()
            .background(Colors.Fons)
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
            Text(stringResource(R.string.sacs_nou), style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            Box(
                Modifier
                    .size(260.dp)
                    .clickable(enabled = fase == Fase.TANCAT, role = Role.Button, onClickLabel = tocaText) {
                        fase = Fase.SACSEJANT
                        onObre()
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (cromo.value < 1f || sortit == null) {
                    DibuixSac(
                        Modifier
                            .size(220.dp)
                            .graphicsLayer {
                                transformOrigin = TransformOrigin(.5f, .92f)
                                rotationZ = if (reduir || fase != Fase.TANCAT) sacseig.value else 1.5f * balanceig + sacseig.value
                                alpha = 1f - cromo.value
                                translationY = cromo.value * 40.dp.toPx()
                            },
                        obertura = obertura.value,
                    )
                }
                if (sortit != null && fase == Fase.OBERT) Cromo(sortit, cromo.value)
            }
            if (fase != Fase.OBERT) {
                Text(tocaText, fontFamily = Caveat, fontSize = 24.sp, color = Colors.TextSecundari, modifier = Modifier.graphicsLayer { rotationZ = -2f })
            } else if (sortit != null) {
                Column(
                    Modifier.graphicsLayer { alpha = cromo.value },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    when (sortit) {
                        is Sortit.Nou -> {
                            val o = sortit.objecte
                            Text(
                                nomRaresa(o.raresa).uppercase() + " · " + "★".repeat(estrelles(o.raresa)),
                                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp),
                            )
                            Text(nomObjecte(o), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                            Text(
                                stringResource(
                                    when (o.tipus) {
                                        TipusObjecte.EMOJI -> R.string.sacs_sortit_emoji
                                        TipusObjecte.PORTADA -> R.string.sacs_sortit_portada
                                        TipusObjecte.ANIMACIO -> R.string.sacs_sortit_animacio
                                        TipusObjecte.COLOR -> R.string.sacs_sortit_color
                                    },
                                ),
                                style = MaterialTheme.typography.bodyMedium.copy(color = Colors.TextSecundari),
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                stringResource(
                                    when (o.raresa) {
                                        Raresa.LLEGENDARIA -> R.string.sacs_comenta_llegendaria
                                        Raresa.RARA -> R.string.sacs_comenta_rara
                                        else -> R.string.sacs_comenta_comuna
                                    },
                                ),
                                fontFamily = Caveat,
                                fontSize = 24.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.graphicsLayer { rotationZ = -2f },
                            )
                            if (o.tipus != TipusObjecte.EMOJI) BotoPrincipal(stringResource(R.string.sacs_fes_servir), { onFesServir(o) })
                        }
                        is Sortit.Punts -> Text(
                            plural(R.plurals.sacs_ja_ho_tens_tot, sortit.punts, sortit.punts),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                    }
                    if (queden > 0) BotoSecundari(plural(R.plurals.sacs_obre_un_altre, queden, queden), onUnAltre)
                    BotoSecundari(stringResource(R.string.continua), onTanca)
                }
            }
        }
    }
}

private fun estrelles(r: Raresa) = when (r) {
    Raresa.LLEGENDARIA -> 3
    Raresa.RARA -> 2
    else -> 1
}

/**
 * El cromo que surt del sac: una carta amb vora de tinta i ombra dura, amb una banda del color fort amb la raresa i
 * les estrelles. Puja i es gira; el llegendari té una lluentor que hi passa per sobre.
 */
@Composable
private fun Cromo(sortit: Sortit, progres: Float) {
    val llegendari = sortit is Sortit.Nou && sortit.objecte.raresa == Raresa.LLEGENDARIA
    val lluu = rememberInfiniteTransition(label = "lluentor")
    val pas by lluu.animateFloat(-1f, 2f, infiniteRepeatable(tween(2400, delayMillis = 600)), label = "pas")
    val reduir = LocalReduirAnimacions.current
    Box(
        Modifier
            .size(width = 170.dp, height = 222.dp)
            .graphicsLayer {
                val t = progres
                cameraDistance = 12f * density
                rotationY = (1f - t) * 180f
                translationY = (1f - t) * 90.dp.toPx()
                scaleX = .3f + .7f * t
                scaleY = .3f + .7f * t
                rotationZ = -3f * t
            }
            .ombraDura(6.dp)
            .background(Colors.Fons)
            .border(2.5.dp, Colors.Text),
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Colors.Ambre)
                    .drawBehind { drawLine(Color.Black, Offset(0f, size.height), Offset(size.width, size.height), 2.5.dp.toPx()) }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val (nom, n) = when (sortit) {
                    is Sortit.Nou -> nomRaresa(sortit.objecte.raresa) to estrelles(sortit.objecte.raresa)
                    is Sortit.Punts -> "" to 0
                }
                Text(nom.uppercase(), style = MaterialTheme.typography.labelMedium.copy(color = Colors.TintaAmbre, letterSpacing = 1.5.sp))
                Text("★".repeat(n), style = MaterialTheme.typography.labelMedium.copy(color = Colors.TintaAmbre))
            }
            Box(Modifier.fillMaxSize().padding(14.dp), contentAlignment = Alignment.Center) {
                when (sortit) {
                    is Sortit.Nou -> DibuixObjecte(sortit.objecte, Modifier.fillMaxSize())
                    is Sortit.Punts -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("+${sortit.punts}", fontFamily = Archivo, fontSize = 34.sp, color = Colors.Text)
                        IconaBarretina(Modifier.size(32.dp))
                    }
                }
            }
        }
        if (llegendari && !reduir && progres >= 1f) {
            Canvas(Modifier.matchParentSize()) {
                val x = pas * size.width
                drawRect(
                    Brush.linearGradient(
                        0f to Color.Transparent, .45f to Color.Transparent, .5f to Color.White.copy(alpha = .7f), .55f to Color.Transparent, 1f to Color.Transparent,
                        start = Offset(x - size.width, 0f),
                        end = Offset(x, size.height),
                    ),
                )
            }
        }
    }
}

/**
 * El sac de roba (docs/disseny.md): trama de teixit, plecs al coll, la corda del color fort amb el llaç i l'etiqueta
 * «BOCINS». «obertura» va de 0 (tancat) a 1 (la corda ha caigut, el coll s'ha eixamplat i el sac s'ha aixafat).
 * En gris, si no n'hi ha cap per obrir.
 */
@Composable
fun DibuixSac(modifier: Modifier = Modifier, gris: Boolean = false, obertura: Float = 0f) {
    val tinta = if (gris) Colors.TextSecundari else Colors.Text
    val fons = Colors.Fons
    val trama = Colors.TextSecundari.copy(alpha = .35f)
    val accent = if (gris) Colors.Linia else Colors.Ambre
    val mesurador = rememberTextMeasurer()
    val estil = TextStyle(fontFamily = Archivo, fontSize = 10.sp, color = tinta, letterSpacing = 1.sp)
    Canvas(modifier) {
        val s = size.minDimension / 100f
        translate((size.width - 100f * s) / 2f, (size.height - 100f * s) / 2f) {
            scale(s, pivot = Offset.Zero) {
                val o = obertura.coerceIn(0f, 1f)
                // El cos: s'aixafa en obrir-se.
                scale(1f + .08f * o, 1f - .14f * o, pivot = Offset(50f, 96f)) {
                    val cos = Path().apply {
                        moveTo(34f, 47f)
                        cubicTo(18f, 54f, 12f, 72f, 16f, 86f)
                        quadraticTo(19f, 94f, 30f, 95f)
                        quadraticTo(50f, 98f, 70f, 95f)
                        quadraticTo(81f, 94f, 84f, 86f)
                        cubicTo(88f, 72f, 82f, 54f, 66f, 47f)
                        close()
                    }
                    translate(4f, 3f) { drawPath(cos, tinta) }
                    drawPath(cos, fons)
                    clipPath(cos) {
                        var x = 10f
                        while (x < 92f) {
                            drawLine(trama, Offset(x, 40f), Offset(x, 100f), .5f)
                            x += 3f
                        }
                        var y = 44f
                        while (y < 100f) {
                            drawLine(trama, Offset(10f, y), Offset(92f, y), .5f)
                            y += 3f
                        }
                    }
                    drawPath(cos, tinta, style = Stroke(2.4f, join = StrokeJoin.Round))
                    for (x0 in listOf(38f, 62f)) drawLine(tinta.copy(alpha = .45f), Offset(x0, 52f), Offset(if (x0 < 50) 36f else 64f, 86f), .9f)
                    // L'etiqueta impresa: es mesura en píxels i s'escala perquè faci 9 unitats d'alt.
                    val text = mesurador.measure("BOCINS", estil)
                    val k = 9f / text.size.height
                    translate(50f - text.size.width * k / 2f, 64f) {
                        scale(k, pivot = Offset.Zero) { drawText(text) }
                    }
                    rotate(-3f, pivot = Offset(50f, 81f)) {
                        drawRect(accent, Offset(30f, 78f), Size(40f, 6f))
                        drawRect(tinta, Offset(30f, 78f), Size(40f, 6f), style = Stroke(1.2f))
                    }
                }
                // El coll, amb els plecs: s'eixampla en obrir-se.
                scale(1f + .5f * o, 1f - .4f * o, pivot = Offset(50f, 47f)) {
                    val coll = Path().apply {
                        moveTo(34f, 47f)
                        quadraticTo(30f, 36f, 24f, 28f)
                        quadraticTo(34f, 31f, 38f, 26f)
                        quadraticTo(42f, 32f, 46f, 25f)
                        quadraticTo(50f, 31f, 54f, 25f)
                        quadraticTo(58f, 32f, 62f, 26f)
                        quadraticTo(66f, 31f, 76f, 28f)
                        quadraticTo(70f, 36f, 66f, 47f)
                        close()
                    }
                    drawPath(coll, fons)
                    drawPath(coll, tinta, style = Stroke(2.2f, join = StrokeJoin.Round))
                    for ((a, b) in listOf(40f to 38f, 50f to 50f, 60f to 62f)) drawLine(tinta.copy(alpha = .5f), Offset(a, 46f), Offset(b, 32f), .9f)
                }
                // La corda i el llaç: cauen en obrir-se.
                val caiguda = o * 30f
                drawRect(accent, Offset(31f, 43f + caiguda), Size(38f, 6f), alpha = 1f - o)
                drawRect(tinta, Offset(31f, 43f + caiguda), Size(38f, 6f), style = Stroke(1.6f), alpha = 1f - o)
                for (costat in listOf(-1f, 1f)) {
                    rotate(costat * 70f * o, pivot = Offset(50f, 46f)) {
                        val lla = Path().apply {
                            moveTo(50f, 46f)
                            cubicTo(50f + costat * 10f, 38f, 50f + costat * 18f, 42f, 50f + costat * 13f, 47f)
                            quadraticTo(50f + costat * 6f, 50f, 50f, 46f)
                            close()
                        }
                        translate(0f, caiguda) {
                            drawPath(lla, accent, alpha = 1f - o)
                            drawPath(lla, tinta, style = Stroke(1.6f), alpha = 1f - o)
                            drawLine(tinta, Offset(50f, 48f), Offset(50f + costat * 7f, 62f), 2.6f, cap = StrokeCap.Round, alpha = 1f - o)
                            drawLine(accent, Offset(50f, 48f), Offset(50f + costat * 7f, 62f), 1.2f, cap = StrokeCap.Round, alpha = 1f - o)
                        }
                    }
                }
            }
        }
    }
}

/** L'avís que s'ha guanyat un sac (o més): obrir-lo ara o després, des del perfil. */
@Composable
fun AvisSacs(n: Int, onObre: () -> Unit, onDespres: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Colors.Fons.copy(alpha = .9f))
            .clickable(remember { MutableInteractionSource() }, indication = null, onClick = {}),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .systemBarsPadding()
                .padding(24.dp)
                .widthIn(max = 360.dp)
                .clip(RectangleShape)
                .background(Colors.Superficie)
                .border(1.dp, Colors.Linia, RectangleShape)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DibuixSac(Modifier.size(120.dp))
            Text(stringResource(R.string.sacs_avis_titol), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
            Text(
                plural(R.plurals.sacs_avis, n, n),
                style = MaterialTheme.typography.bodyMedium.copy(color = Colors.TextSecundari),
                textAlign = TextAlign.Center,
            )
            BotoPrincipal(stringResource(R.string.sacs_obre), onObre)
            BotoSecundari(stringResource(R.string.sacs_despres), onDespres)
        }
    }
}
