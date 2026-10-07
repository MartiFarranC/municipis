package cat.descobreix.gent

import android.widget.Toast
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cat.descobreix.R
import cat.descobreix.data.compte.TipusPerfil
import cat.descobreix.data.social.EstatSeguiment
import cat.descobreix.data.social.EventMur
import cat.descobreix.data.social.Persona
import cat.descobreix.data.social.ReaccioRebuda
import cat.descobreix.data.social.TipusEvent
import cat.descobreix.joc.config.ConfiguracioJoc.Objecte
import cat.descobreix.joc.config.ConfiguracioJoc.Raresa
import cat.descobreix.joc.config.ConfiguracioJoc.TipusObjecte
import cat.descobreix.joc.model.CodiIne
import cat.descobreix.sacs.dibuixEmoji
import cat.descobreix.sacs.nomObjecte
import cat.descobreix.ui.components.BotoIcona
import cat.descobreix.ui.components.BotoPrincipal
import cat.descobreix.ui.components.BotoSecundari
import cat.descobreix.ui.components.Carregant
import cat.descobreix.ui.components.Icones
import cat.descobreix.ui.plural
import cat.descobreix.ui.theme.ChakraPetch
import cat.descobreix.ui.theme.Colors
import java.text.DateFormat
import java.util.Date

/** La pestanya «Gent»: el mur, buscar gent i les sol·licituds per seguir-te. */
@Composable
fun GentScreen(onObrePersona: (String) -> Unit, viewModel: GentViewModel = hiltViewModel()) {
    val estat by viewModel.estat.collectAsStateWithLifecycle()
    if (estat.carregant) {
        Carregant(Modifier.fillMaxSize())
        return
    }
    Avis(estat.avis, viewModel::tancaAvis)
    var denunciant by remember { mutableStateOf<ReaccioRebuda?>(null) }
    LazyColumn(
        Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text(stringResource(R.string.gent), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() }) }
        item {
            OutlinedTextField(
                value = estat.cerca,
                onValueChange = viewModel::cerca,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.gent_cerca)) },
                singleLine = true,
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
        if (estat.cerca.isNotBlank()) {
            if (estat.resultats.isEmpty()) {
                item { Text(stringResource(R.string.gent_cap_resultat), style = MaterialTheme.typography.bodyMedium.copy(color = Colors.TextSecundari)) }
            }
            items(estat.resultats, key = { "r" + it.id }) { p -> FilaPersona(p, { onObrePersona(p.id) }) }
        }
        if (estat.senseConnexio) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.gent_sense_connexio), style = MaterialTheme.typography.bodyMedium.copy(color = Colors.Error))
                    BotoSecundari(stringResource(R.string.torna_a_provar), viewModel::actualitza)
                }
            }
        }
        if (estat.sollicituds.isNotEmpty()) {
            item { Titol(stringResource(R.string.gent_sollicituds)) }
            items(estat.sollicituds, key = { "s" + it.id }) { p ->
                FilaPersona(p, { onObrePersona(p.id) }) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BotoPrincipal(stringResource(R.string.gent_accepta), { viewModel.accepta(p) }, Modifier.weight(1f))
                        BotoSecundari(stringResource(R.string.gent_rebutja), { viewModel.rebutja(p) }, Modifier.weight(1f))
                    }
                }
            }
        }
        if (estat.reaccions.isNotEmpty()) {
            item { Titol(stringResource(R.string.gent_t_han_animat)) }
            items(estat.reaccions.take(10), key = { "a" + it.id }) { r ->
                TargetaReaccio(
                    r,
                    if (r.objectiu == TipusEvent.MUNICIPI) estat.noms[r.objectiuId] else null,
                    onObre = { onObrePersona(r.autorId) },
                    onTreu = { viewModel.treuReaccio(r) },
                    onBloqueja = { viewModel.bloqueja(r) },
                    onDenuncia = { denunciant = r },
                )
            }
        }
        item { Titol(stringResource(R.string.gent_mur)) }
        if (estat.mur.isEmpty() && !estat.senseConnexio) {
            item { Text(stringResource(R.string.gent_mur_buit), style = MaterialTheme.typography.bodyMedium.copy(color = Colors.TextSecundari)) }
        }
        items(estat.mur, key = { it.usuariId + it.tipus + it.codiIne + (it.fotoId ?: "") + it.creatEl }) { e ->
            Event(e, estat.noms[e.codiIne] ?: e.codiIne, viewModel.imatges, { onObrePersona(e.usuariId) }, { viewModel.obreAnima(e) })
        }
        if (estat.hiHaMes) item { BotoSecundari(stringResource(R.string.gent_mes), viewModel::mes) }
    }
    estat.animant?.let { e -> FinestraAnima(e.nom, estat.emojis, onEmoji = { viewModel.anima(it, null) }, onMissatge = { viewModel.anima(null, it) }, onTanca = viewModel::tancaAnima) }
    denunciant?.let { r ->
        DialegDenuncia(r.autorNom, onEnvia = { motiu ->
            viewModel.denuncia(r, motiu)
            denunciant = null
        }, onTanca = { denunciant = null })
    }
}

/** Un avís breu (una notificació de pantalla) després d'una acció. */
@Composable
private fun Avis(avis: AvisGent?, onTancat: () -> Unit) {
    val context = LocalContext.current
    val text = avis?.let {
        stringResource(
            when (it) {
                AvisGent.ENVIAT -> R.string.anima_enviat
                AvisGent.BLOQUEJAT -> R.string.gent_bloquejat
                AvisGent.DENUNCIAT -> R.string.gent_denunciat
                AvisGent.ERROR -> R.string.gent_sense_connexio
            },
        )
    }
    LaunchedEffect(avis) {
        if (text != null) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
            onTancat()
        }
    }
}

/** Una reacció que t'han enviat: qui, l'emoji amb la frase (o el missatge) i per què; es pot treure, bloquejar o denunciar. */
@Composable
private fun TargetaReaccio(
    r: ReaccioRebuda,
    municipi: String?,
    onObre: () -> Unit,
    onTreu: () -> Unit,
    onBloqueja: () -> Unit,
    onDenuncia: () -> Unit,
) {
    val forma = RoundedCornerShape(14.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(Colors.Superficie)
            .border(1.dp, Colors.Linia, forma)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            Modifier.clickable(role = Role.Button, onClick = onObre),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val emoji = r.emoji
            val dibuix = emoji?.let { dibuixEmoji(it) }
            if (dibuix != null) Image(painterResource(dibuix), null, Modifier.size(48.dp)) else Inicial(r.autorNom)
            Column(Modifier.weight(1f)) {
                Text(
                    if (emoji != null) "«" + nomObjecte(Objecte(TipusObjecte.EMOJI, emoji, Raresa.INICIAL)) + "»" else "«" + r.missatge.orEmpty() + "»",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    if (municipi != null) stringResource(R.string.reaccio_de_per_municipi, r.autorNom, municipi) else stringResource(R.string.reaccio_de_per_foto, r.autorNom),
                    style = MaterialTheme.typography.bodySmall.copy(color = Colors.TextSecundari),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(onClick = onTreu) { Text(stringResource(R.string.reaccio_treu)) }
            TextButton(onClick = onBloqueja) { Text(stringResource(R.string.gent_bloqueja), color = Colors.Error) }
            TextButton(onClick = onDenuncia) { Text(stringResource(R.string.gent_denuncia), color = Colors.Error) }
        }
    }
}

/** Animar algú: els emojis que tens (cadascun amb la seva frase) o un missatge curt. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FinestraAnima(nom: String, emojis: List<String>, onEmoji: (String) -> Unit, onMissatge: (String) -> Unit, onTanca: () -> Unit) {
    var missatge by remember { mutableStateOf("") }
    Box(
        Modifier
            .fillMaxSize()
            .background(Colors.Fons.copy(alpha = .96f))
            .clickable(remember { MutableInteractionSource() }, indication = null, onClick = {}),
    ) {
        LazyColumn(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BotoIcona(Icones.Tancar, stringResource(R.string.tanca), onTanca)
                    Text(stringResource(R.string.anima_a, nom), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
                }
            }
            item {
                OutlinedTextField(
                    value = missatge,
                    onValueChange = { missatge = it.take(GentViewModel.MAX_MISSATGE) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.anima_missatge)) },
                    supportingText = { Text("${missatge.length}/${GentViewModel.MAX_MISSATGE}") },
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
            item { BotoPrincipal(stringResource(R.string.anima_envia), { onMissatge(missatge) }, enabled = missatge.isNotBlank()) }
            item { Text(stringResource(R.string.anima_o_emoji), style = MaterialTheme.typography.titleMedium) }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (id in emojis) {
                        val frase = nomObjecte(Objecte(TipusObjecte.EMOJI, id, Raresa.INICIAL))
                        Column(
                            Modifier
                                .size(width = 84.dp, height = 104.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Colors.Superficie)
                                .clickable(role = Role.Button, onClickLabel = frase) { onEmoji(id) }
                                .padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            dibuixEmoji(id)?.let { Image(painterResource(it), null, Modifier.size(56.dp)) }
                            Text(frase, style = MaterialTheme.typography.labelSmall, maxLines = 2, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
            item { Text(stringResource(R.string.anima_mes_emojis), style = MaterialTheme.typography.bodySmall.copy(color = Colors.TextSecundari)) }
        }
    }
}

/** Denunciar algú: un motiu opcional. Les denúncies les revisa el Martí. */
@Composable
private fun DialegDenuncia(nom: String, onEnvia: (String?) -> Unit, onTanca: () -> Unit) {
    var motiu by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onTanca,
        containerColor = Colors.Superficie,
        title = { Text(stringResource(R.string.denuncia_titol, nom)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.denuncia_text), style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = motiu,
                    onValueChange = { motiu = it.take(500) },
                    label = { Text(stringResource(R.string.denuncia_motiu)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = { TextButton(onClick = { onEnvia(motiu.ifBlank { null }) }) { Text(stringResource(R.string.gent_denuncia)) } },
        dismissButton = { TextButton(onClick = onTanca) { Text(stringResource(R.string.cancela)) } },
    )
}

@Composable
private fun Titol(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp).semantics { heading() })
}

/** Una persona en una llista: la inicial, el nom i si és Explorador o Espectador. */
@Composable
private fun FilaPersona(p: Persona, onObre: () -> Unit, sota: (@Composable () -> Unit)? = null) {
    val forma = RoundedCornerShape(14.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(Colors.Superficie)
            .border(1.dp, Colors.Linia, forma)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp)
                .clickable(role = Role.Button, onClick = onObre),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Inicial(p.nom)
            Column(Modifier.weight(1f)) {
                Text(p.nom, style = MaterialTheme.typography.titleMedium)
                Text(nomTipus(p.tipus), style = MaterialTheme.typography.bodySmall.copy(color = Colors.TextSecundari))
            }
        }
        sota?.invoke()
    }
}

@Composable
private fun Inicial(nom: String, mida: Int = 40) {
    Box(
        Modifier
            .size(mida.dp)
            .clip(CircleShape)
            .background(Colors.Disponible1),
        contentAlignment = Alignment.Center,
    ) {
        Text(nom.take(1).uppercase(), fontFamily = ChakraPetch, fontWeight = FontWeight.Bold, color = Colors.Ambre)
    }
}

@Composable
fun nomTipus(t: TipusPerfil): String = stringResource(if (t == TipusPerfil.EXPLORADOR) R.string.perfil_explorador else R.string.perfil_espectador)

/** Una cosa del mur: qui, què i quan; si és una foto, la miniatura. */
@Composable
private fun Event(e: EventMur, municipi: String, imatges: ImatgesRemotes, onObre: () -> Unit, onAnima: () -> Unit) {
    val forma = RoundedCornerShape(14.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(Colors.Superficie)
            .border(1.dp, Colors.Linia, forma)
            .clickable(role = Role.Button, onClick = onObre)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Inicial(e.nom, 32)
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(if (e.tipus == TipusEvent.FOTO) R.string.mur_foto else R.string.mur_municipi, e.nom, municipi),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(dataCurta(e.creatEl), style = MaterialTheme.typography.bodySmall.copy(color = Colors.TextSecundari))
            }
        }
        val ruta = e.rutaMiniatura
        if (ruta != null) ImatgeRemota(ruta, imatges, stringResource(R.string.mur_foto_de, municipi), Modifier.fillMaxWidth().aspectRatio(4f / 3f).clip(RoundedCornerShape(10.dp)))
        BotoSecundari(stringResource(R.string.anima), onAnima)
    }
}

private fun dataCurta(ms: Long): String = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(ms))

/** Una imatge del servidor; mentre es baixa (o si no es pot), un requadre buit. */
@Composable
fun ImatgeRemota(ruta: String, imatges: ImatgesRemotes, descripcio: String, modifier: Modifier = Modifier) {
    val imatge by produceState<ImageBitmap?>(null, ruta) { value = imatges.carrega(ruta) }
    val img = imatge
    if (img != null) {
        Image(img, descripcio, modifier, contentScale = ContentScale.Crop)
    } else {
        Box(modifier.background(Colors.Superficie2).semantics { contentDescription = descripcio })
    }
}

/** El perfil d'una altra persona. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PersonaScreen(onEnrere: () -> Unit, onObrePersona: (String) -> Unit, viewModel: PersonaViewModel = hiltViewModel()) {
    val estat by viewModel.estat.collectAsStateWithLifecycle()
    if (estat.carregant) {
        Carregant(Modifier.fillMaxSize())
        return
    }
    Avis(estat.avis, viewModel::tancaAvis)
    var denunciant by remember { mutableStateOf(false) }
    var bloquejant by remember { mutableStateOf(false) }
    val p = estat.perfil
    if (denunciant && p != null) {
        DialegDenuncia(p.nom, onEnvia = {
            viewModel.denuncia(it)
            denunciant = false
        }, onTanca = { denunciant = false })
    }
    if (bloquejant && p != null) {
        AlertDialog(
            onDismissRequest = { bloquejant = false },
            containerColor = Colors.Superficie,
            title = { Text(stringResource(R.string.bloqueja_titol, p.nom)) },
            text = { Text(stringResource(R.string.bloqueja_text)) },
            confirmButton = {
                TextButton(onClick = {
                    bloquejant = false
                    viewModel.bloqueja()
                }) { Text(stringResource(R.string.gent_bloqueja)) }
            },
            dismissButton = { TextButton(onClick = { bloquejant = false }) { Text(stringResource(R.string.cancela)) } },
        )
    }
    LazyColumn(
        Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { BotoIcona(Icones.Enrere, stringResource(R.string.enrere), onEnrere) }
        if (p == null) {
            item { Text(stringResource(R.string.gent_sense_connexio), style = MaterialTheme.typography.bodyMedium.copy(color = Colors.Error)) }
            return@LazyColumn
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Inicial(p.nom, 56)
                Column {
                    Text(p.nom, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
                    Text(
                        nomTipus(p.tipus) + " · " + stringResource(if (p.public) R.string.compte_public else R.string.compte_privat),
                        style = MaterialTheme.typography.bodySmall.copy(color = Colors.TextSecundari),
                    )
                }
            }
        }
        item {
            Text(
                plural(R.plurals.gent_seguidors, p.seguidors, p.seguidors) + " · " + plural(R.plurals.gent_segueix, p.seguits, p.seguits) +
                    if (p.emSegueix) " · " + stringResource(R.string.gent_et_segueix) else "",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (estat.bloquejat) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.gent_l_has_bloquejat), style = MaterialTheme.typography.bodyMedium.copy(color = Colors.TextSecundari))
                    BotoSecundari(stringResource(R.string.gent_desbloqueja), viewModel::desbloqueja, enabled = !estat.treballant)
                }
            }
        } else item {
            when (p.elSegueixo) {
                null -> BotoPrincipal(stringResource(R.string.gent_segueix), viewModel::segueix, carregant = estat.treballant)
                EstatSeguiment.PENDENT -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.gent_sollicitud_enviada), style = MaterialTheme.typography.bodyMedium.copy(color = Colors.TextSecundari))
                    BotoSecundari(stringResource(R.string.gent_retira_sollicitud), viewModel::deixaDeSeguir, enabled = !estat.treballant)
                }
                EstatSeguiment.ACCEPTAT -> BotoSecundari(stringResource(R.string.gent_deixa_de_seguir), viewModel::deixaDeSeguir, enabled = !estat.treballant)
            }
        }
        if (!p.visible && !p.public) {
            item { Text(stringResource(R.string.gent_compte_privat), style = MaterialTheme.typography.bodyMedium.copy(color = Colors.TextSecundari)) }
        }
        if (p.visible && p.tipus == TipusPerfil.EXPLORADOR) {
            item { Titol(stringResource(R.string.gent_mapa, estat.municipis.size, estat.total)) }
            item { MapaPersona(estat, Modifier.fillMaxWidth().aspectRatio(1.1f)) }
        }
        if (estat.fotos.isNotEmpty()) {
            item { Titol(stringResource(R.string.fotos)) }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (f in estat.fotos) {
                        ImatgeRemota(
                            f.rutaMiniatura,
                            viewModel.imatges,
                            stringResource(R.string.mur_foto_de, estat.noms[f.codiIne] ?: f.codiIne),
                            Modifier.size(104.dp).clip(RoundedCornerShape(10.dp)),
                        )
                    }
                }
            }
        }
        if (estat.seguits.isNotEmpty()) {
            item { Titol(stringResource(R.string.gent_a_qui_segueix)) }
            items(estat.seguits, key = { "q" + it.id }) { s -> FilaPersona(s, { onObrePersona(s.id) }) }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (!estat.bloquejat) TextButton(onClick = { bloquejant = true }) { Text(stringResource(R.string.gent_bloqueja), color = Colors.Error) }
                TextButton(onClick = { denunciant = true }) { Text(stringResource(R.string.gent_denuncia), color = Colors.Error) }
            }
        }
    }
}

/** El mapa d'una altra persona: Catalunya amb els municipis que ha descobert. */
@Composable
private fun MapaPersona(estat: PersonaEstat, modifier: Modifier) {
    val g = estat.geometria ?: return
    val camins = estat.camins ?: return
    val descoberts: Set<Int> = estat.municipis.mapNotNull { c: CodiIne -> estat.index[c] }.toSet()
    val descripcio = stringResource(R.string.gent_mapa_descripcio, estat.municipis.size)
    val ambre = Colors.Ambre
    val boira = Colors.Superficie2
    Canvas(modifier.semantics { contentDescription = descripcio }) {
        val f = minOf(size.width / g.amplada, size.height / g.alcada)
        val dx = (size.width - g.amplada * f) / 2f
        val dy = (size.height - g.alcada * f) / 2f
        withTransform({
            translate(dx, dy)
            scale(f, f, pivot = Offset.Zero)
        }) {
            for (i in camins.general.indices) drawPath(camins.general[i], if (i in descoberts) ambre else boira)
        }
    }
}

/** La gent que segueixes, amb les sol·licituds que encara no t'han acceptat. */
@Composable
fun SeguitsScreen(onEnrere: () -> Unit, onObrePersona: (String) -> Unit, viewModel: SeguitsViewModel = hiltViewModel()) {
    val estat by viewModel.estat.collectAsStateWithLifecycle()
    LazyColumn(
        Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BotoIcona(Icones.Enrere, stringResource(R.string.enrere), onEnrere)
                Text(stringResource(R.string.gent_que_segueixo), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
            }
        }
        if (estat.senseConnexio) {
            item { Text(stringResource(R.string.gent_sense_connexio), style = MaterialTheme.typography.bodyMedium.copy(color = Colors.Error)) }
        } else if (!estat.carregant && estat.seguits.isEmpty()) {
            item { Text(stringResource(R.string.gent_no_segueixes_ningu), style = MaterialTheme.typography.bodyMedium.copy(color = Colors.TextSecundari)) }
        }
        items(estat.seguits, key = { it.first.id }) { (p, e) ->
            FilaPersona(p, { onObrePersona(p.id) }) {
                if (e == EstatSeguiment.PENDENT) {
                    Text(stringResource(R.string.gent_sollicitud_enviada), style = MaterialTheme.typography.bodySmall.copy(color = Colors.TextSecundari))
                }
            }
        }
    }
}

/** El teu compte a la part social: públic o privat i la gent que segueixes. */
@Composable
fun CompteSocial(onObreSeguits: () -> Unit, viewModel: SeguitsViewModel = hiltViewModel()) {
    val estat by viewModel.estat.collectAsStateWithLifecycle()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.perfil_compte), style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Colors.Superficie)
                .border(1.dp, Colors.Linia, RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(if (estat.public) R.string.compte_public else R.string.compte_privat), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(if (estat.public) R.string.compte_public_text else R.string.compte_privat_text),
                    style = MaterialTheme.typography.bodySmall.copy(color = Colors.TextSecundari),
                )
            }
            Switch(
                checked = estat.public,
                onCheckedChange = viewModel::canviaVisibilitat,
                enabled = !estat.canviant,
                colors = SwitchDefaults.colors(checkedTrackColor = Colors.Ambre, checkedThumbColor = Colors.TintaAmbre),
            )
        }
        if (estat.errorCanviant) {
            Text(stringResource(R.string.compte_error_sense_connexio), style = MaterialTheme.typography.bodySmall.copy(color = Colors.Error))
        }
        BotoSecundari(
            plural(R.plurals.gent_segueixes, estat.seguits.size, estat.seguits.size),
            onObreSeguits,
            icona = Icones.Gent,
        )
    }
}
