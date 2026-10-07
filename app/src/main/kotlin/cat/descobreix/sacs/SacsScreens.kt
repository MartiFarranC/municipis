package cat.descobreix.sacs

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
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
import androidx.compose.ui.text.font.FontWeight
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
import cat.descobreix.ui.plural
import cat.descobreix.ui.components.BotoIcona
import cat.descobreix.ui.components.BotoPrincipal
import cat.descobreix.ui.components.BotoSecundari
import cat.descobreix.ui.components.Carregant
import cat.descobreix.ui.components.Icones
import cat.descobreix.ui.components.SardanaCarregant
import cat.descobreix.ui.theme.ChakraPetch
import cat.descobreix.ui.theme.ColorSecundari
import cat.descobreix.ui.theme.Colors
import cat.descobreix.ui.theme.LocalReduirAnimacions
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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
                    Fitxa(stringResource(R.string.color_ambre), estat.color == null, onTria = { viewModel.colorDeSempre() }) {
                        Mostra(ColorSecundari.Ambre)
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
            .clip(RoundedCornerShape(16.dp))
            .background(Colors.Superficie)
            .border(1.dp, Colors.Linia, RoundedCornerShape(16.dp))
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
    val forma = RoundedCornerShape(14.dp)
    Column(
        Modifier
            .width(92.dp)
            .clip(forma)
            .background(Colors.Superficie)
            .border(if (triada) 2.dp else 1.dp, if (triada) Colors.Ambre else vora, forma)
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(Colors.Superficie2)
                    .semantics { contentDescription = amagada },
                contentAlignment = Alignment.Center,
            ) {
                Text("?", fontFamily = ChakraPetch, fontWeight = FontWeight.Bold, fontSize = 26.sp, color = colorRaresa(o.raresa).copy(alpha = .6f))
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
        TipusObjecte.COLOR -> Box(modifier, contentAlignment = Alignment.Center) { Mostra(colorSac(o.id) ?: ColorSecundari.Ambre) }
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
            .clip(RoundedCornerShape(5.dp))
            .background(p.fons)
            .border(1.dp, p.daurat.copy(alpha = .6f), RoundedCornerShape(5.dp)),
    ) {
        p.dibuix?.let { Image(painterResource(it), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
    }
}

/**
 * Obrir un sac: el sac surt tancat i es mou; quan l'usuari el toca, tremola, s'obre i en surt la cosa nova,
 * amb la llum del color de la seva raresa.
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
    val transicio = rememberInfiniteTransition(label = "sac")
    val balanceig by transicio.animateFloat(-1f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "balanceig")
    val gir by transicio.animateFloat(0f, 360f, infiniteRepeatable(tween(9000, easing = LinearEasing)), label = "raigs")
    val sacseig = remember { Animatable(0f) }
    val ambit = rememberCoroutineScope()
    val obertura = remember { Animatable(if (reduir) 1f else 0f) }
    LaunchedEffect(sortit) {
        if (sortit != null && !reduir) {
            obertura.snapTo(0f)
            obertura.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
        }
    }
    val tocaText = stringResource(R.string.sacs_toca)
    Box(
        Modifier
            .fillMaxSize()
            .background(Colors.Fons.copy(alpha = .96f))
            .clickable(remember { MutableInteractionSource() }, indication = null, onClick = {}),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .systemBarsPadding()
                .padding(24.dp)
                .widthIn(max = 360.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (sortit == null) {
                Text(stringResource(R.string.sacs_nou), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                Box(
                    Modifier
                        .size(220.dp)
                        .clickable(role = Role.Button, onClickLabel = tocaText) {
                            // Tremola una mica i s'obre.
                            ambit.launch {
                                if (!reduir) for (g in listOf(-10f, 10f, -8f, 8f, -4f, 0f)) sacseig.animateTo(g, tween(60))
                                onObre()
                            }
                        }
                        .graphicsLayer { rotationZ = if (reduir) 0f else 4f * balanceig + sacseig.value },
                    contentAlignment = Alignment.Center,
                ) { DibuixSac(Modifier.fillMaxSize()) }
                Text(tocaText, style = MaterialTheme.typography.bodyMedium.copy(color = Colors.TextSecundari))
            } else {
                val color = when (sortit) {
                    is Sortit.Nou -> colorRaresa(sortit.objecte.raresa)
                    is Sortit.Punts -> Colors.Ambre
                }
                Box(Modifier.size(240.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.fillMaxSize()) { raigs(color, if (reduir) 0f else gir, obertura.value.coerceIn(0f, 1f)) }
                    Box(
                        Modifier
                            .size(150.dp)
                            .graphicsLayer {
                                val e = obertura.value
                                scaleX = e
                                scaleY = e
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        when (sortit) {
                            is Sortit.Nou -> DibuixObjecte(sortit.objecte, Modifier.fillMaxSize())
                            is Sortit.Punts -> Text("+${sortit.punts}", fontFamily = ChakraPetch, fontWeight = FontWeight.Bold, fontSize = 48.sp, color = Colors.Ambre)
                        }
                    }
                }
                when (sortit) {
                    is Sortit.Nou -> {
                        val o = sortit.objecte
                        Text(
                            nomRaresa(o.raresa).uppercase(),
                            fontFamily = ChakraPetch,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = color,
                        )
                        Text(nomObjecte(o), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
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
                        if (o.tipus != TipusObjecte.EMOJI) BotoPrincipal(stringResource(R.string.sacs_fes_servir), { onFesServir(o) })
                    }
                    is Sortit.Punts -> Text(
                        stringResource(R.string.sacs_ja_ho_tens_tot, sortit.punts),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                }
                if (queden > 0) {
                    BotoSecundari(plural(R.plurals.sacs_obre_un_altre, queden, queden), onUnAltre)
                }
                BotoSecundari(stringResource(R.string.continua), onTanca)
            }
        }
    }
}

/** La llum de darrere de la cosa nova: raigs que giren. */
private fun DrawScope.raigs(color: Color, gir: Float, forca: Float) {
    val c = Offset(size.width / 2f, size.height / 2f)
    val r = size.minDimension / 2f
    rotate(gir, c) {
        for (i in 0 until 12) {
            val a = i * 2 * PI / 12
            val b = a + PI / 18
            val p = Path().apply {
                moveTo(c.x, c.y)
                lineTo(c.x + r * cos(a).toFloat(), c.y + r * sin(a).toFloat())
                lineTo(c.x + r * cos(b).toFloat(), c.y + r * sin(b).toFloat())
                close()
            }
            drawPath(p, color, alpha = .18f * forca)
        }
    }
    drawCircle(color, r * .55f, c, alpha = .16f * forca)
}

/** Un sac de roba lligat amb una corda del color secundari, i una estrella. En gris, si no n'hi ha cap. */
@Composable
fun DibuixSac(modifier: Modifier = Modifier, gris: Boolean = false) {
    val accent = if (gris) Colors.Linia else Colors.Ambre
    val roba = if (gris) Colors.Superficie2 else Color(0xFFB07A4A)
    val fosc = if (gris) Colors.Linia else Color(0xFF7A5230)
    val clar = if (gris) Colors.Superficie2 else Color(0xFFD09A62)
    Canvas(modifier) {
        val s = size.minDimension / 100f
        translate((size.width - 100f * s) / 2f, (size.height - 100f * s) / 2f) {
            scale(s, pivot = Offset.Zero) {
                val cosSac = Path().apply {
                    moveTo(30f, 42f)
                    cubicTo(16f, 56f, 14f, 80f, 24f, 90f)
                    quadraticTo(50f, 98f, 76f, 90f)
                    cubicTo(86f, 80f, 84f, 56f, 70f, 42f)
                    close()
                }
                drawPath(cosSac, roba)
                drawPath(cosSac, fosc, style = Stroke(2f))
                val coll = Path().apply {
                    moveTo(36f, 20f)
                    quadraticTo(44f, 26f, 42f, 36f)
                    lineTo(58f, 36f)
                    quadraticTo(56f, 26f, 64f, 20f)
                    quadraticTo(56f, 24f, 50f, 18f)
                    quadraticTo(44f, 24f, 36f, 20f)
                    close()
                }
                drawPath(coll, clar)
                drawPath(coll, fosc, style = Stroke(1.6f))
                drawRoundRect(accent, Offset(36f, 35f), Size(28f, 7f), CornerRadius(3.5f))
                drawCircle(accent, 4f, Offset(56f, 41f))
                drawLine(accent, Offset(56f, 44f), Offset(60f, 54f), 2.4f)
                drawLine(accent, Offset(56f, 44f), Offset(52f, 53f), 2.4f)
                val estrella = Path()
                for (i in 0 until 10) {
                    val r = if (i % 2 == 0) 11f else 4.8f
                    val a = Math.toRadians(-90.0 + i * 36.0)
                    val x = 50f + r * cos(a).toFloat()
                    val y = 70f + r * sin(a).toFloat()
                    if (i == 0) estrella.moveTo(x, y) else estrella.lineTo(x, y)
                }
                estrella.close()
                drawPath(estrella, accent)
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
                .clip(RoundedCornerShape(20.dp))
                .background(Colors.Superficie)
                .border(1.dp, Colors.Linia, RoundedCornerShape(20.dp))
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
