package cat.descobreix.municipality

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cat.descobreix.R
import cat.descobreix.domain.Foto
import cat.descobreix.domain.MissioPropia
import cat.descobreix.joc.model.CodiIne
import cat.descobreix.joc.model.EstatMunicipi
import cat.descobreix.joc.model.Missio
import cat.descobreix.joc.model.TipusMissio
import cat.descobreix.joc.model.TipusProva
import cat.descobreix.joc.progressio.Medalles
import cat.descobreix.ui.Celebracio
import cat.descobreix.ui.DialegMissatge
import cat.descobreix.ui.DialegTriaMunicipi
import cat.descobreix.ui.components.BarraProgres
import cat.descobreix.ui.components.BotoIcona
import cat.descobreix.ui.components.BotoPrincipal
import cat.descobreix.ui.components.BotoSecundari
import cat.descobreix.ui.components.Carregant
import cat.descobreix.ui.components.Icones
import cat.descobreix.ui.components.ImatgeLocal
import cat.descobreix.ui.esCelebracio
import cat.descobreix.ui.plural
import cat.descobreix.ui.rememberPermisUbicacio
import cat.descobreix.ui.textDe
import cat.descobreix.ui.theme.ChakraPetch
import cat.descobreix.ui.theme.Colors

@Composable
fun MunicipiScreen(
    onEnrere: () -> Unit,
    onObreMunicipi: (CodiIne) -> Unit,
    onFesFoto: (CodiIne, String?) -> Unit,
    onObreFoto: (String) -> Unit,
    onVeureAlMapa: (CodiIne) -> Unit,
    onSegella: (CodiIne) -> Unit,
    viewModel: MunicipiViewModel = hiltViewModel(),
) {
    val estat by viewModel.estat.collectAsStateWithLifecycle()
    // Quan es tanca la celebració del check-in, s'obre el passaport per posar-hi el segell.
    val segell = estat.segellPendent
    LaunchedEffect(segell, estat.avis) {
        if (segell != null && estat.avis == null) {
            viewModel.segellObert()
            onSegella(segell)
        }
    }
    if (estat.carregant) {
        Carregant(Modifier.fillMaxSize())
        return
    }
    val avisos = remember { SnackbarHostState() }
    val textAvis = estat.avis?.takeUnless { it.esCelebracio }?.let { textDe(it) }
    LaunchedEffect(textAvis) {
        if (textAvis != null) {
            avisos.showSnackbar(textAvis)
            viewModel.tancaAvis()
        }
    }
    var missioGps by remember { mutableStateOf<Missio?>(null) }
    val demanaUbicacio = rememberPermisUbicacio(
        onConcedit = { missioGps?.let(viewModel::provaGps) },
        onDenegat = viewModel::sensePermis,
    )

    Box(Modifier.fillMaxSize().background(Colors.Fons)) {
        if (estat.estat == EstatMunicipi.DESCOBERT) {
            FitxaDescoberta(
                estat = estat,
                onEnrere = onEnrere,
                onProva = { m ->
                    when (m.prova) {
                        TipusProva.GPS -> {
                            missioGps = m
                            demanaUbicacio()
                        }
                        TipusProva.FOTO -> onFesFoto(estat.codi, m.id)
                    }
                },
                onFesFoto = { onFesFoto(estat.codi, null) },
                onObreFoto = onObreFoto,
                onObreMunicipi = onObreMunicipi,
                onVeureAlMapa = { onVeureAlMapa(estat.codi) },
                onAfegeixPropia = viewModel::afegeixPropia,
                onCanviaPropia = viewModel::canviaPropia,
                onEsborraPropia = viewModel::esborraPropia,
            )
        } else {
            FitxaBloquejada(
                estat = estat,
                onEnrere = onEnrere,
                onDesbloqueja = viewModel::desbloqueja,
                onObreMunicipi = onObreMunicipi,
                onVeureAlMapa = { onVeureAlMapa(estat.codi) },
            )
        }
        SnackbarHost(
            avisos,
            Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp),
        )
    }

    estat.avis?.takeIf { it.esCelebracio }?.let { Celebracio(it, viewModel::tancaAvis) }
    estat.candidats?.let { DialegTriaMunicipi(it, viewModel::triaCandidat, viewModel::tancaCandidats) }
    estat.missatge?.let { DialegMissatge(it, viewModel::tancaMissatge) }
}

private val ratllat = Brush.linearGradient(
    0f to Colors.Superficie, 0.5f to Colors.Superficie, 0.5f to Color(0xFF11151C), 1f to Color(0xFF11151C),
    start = Offset.Zero,
    end = Offset(34f, 34f),
    tileMode = TileMode.Repeated,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FitxaDescoberta(
    estat: MunicipiEstat,
    onEnrere: () -> Unit,
    onProva: (Missio) -> Unit,
    onFesFoto: () -> Unit,
    onObreFoto: (String) -> Unit,
    onObreMunicipi: (CodiIne) -> Unit,
    onVeureAlMapa: () -> Unit,
    onAfegeixPropia: (String, String) -> Unit,
    onCanviaPropia: (String, Boolean) -> Unit,
    onEsborraPropia: (String) -> Unit,
) {
    var afegint by rememberSaveable { mutableStateOf(false) }
    LazyColumn(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(ratllat),
                contentAlignment = Alignment.Center,
            ) {
                val portada = estat.portada
                if (portada != null) {
                    ImatgeLocal(
                        portada.fitxer,
                        stringResource(R.string.foto_portada_de, estat.nom),
                        Modifier
                            .fillMaxSize()
                            .clickable(role = Role.Image) { onObreFoto(portada.id) },
                    )
                } else {
                    BotoSecundari(
                        stringResource(R.string.afegeix_foto_de, estat.nom),
                        onFesFoto,
                        icona = Icones.Camera,
                        modifier = Modifier.padding(horizontal = 48.dp),
                    )
                }
                BotoIcona(
                    Icones.Enrere,
                    stringResource(R.string.enrere),
                    onEnrere,
                    Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(16.dp),
                )
                BotoIcona(
                    Icones.Mapa,
                    stringResource(R.string.veure_al_mapa),
                    onVeureAlMapa,
                    Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(16.dp),
                )
            }
        }
        item {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            if (estat.esInici) stringResource(R.string.comarca_inici, estat.comarca) else estat.comarca,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(estat.nom, style = MaterialTheme.typography.displayMedium, modifier = Modifier.semantics { heading() })
                    }
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.semantics(mergeDescendants = true) {}) {
                        Text("${estat.puntsGuanyats}", style = MaterialTheme.typography.headlineMedium.copy(color = Colors.Ambre))
                        Text(stringResource(R.string.de_punts, estat.puntsPossibles), style = MaterialTheme.typography.bodySmall)
                    }
                }
                BarraProgres(if (estat.puntsPossibles == 0) 0f else estat.puntsGuanyats.toFloat() / estat.puntsPossibles)
                Text(
                    stringResource(R.string.progres_missions, estat.missionsFetes, estat.missions.size, estat.bonus),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        items(estat.missions, key = { it.missio.id }) { f ->
            Column {
                FilaDeMissio(f, provant = estat.provant == f.missio.id, onProva = { onProva(f.missio) })
                // La foto del cartell ja feta es pot repetir (sense punts) per canviar el cromo del catàleg.
                if (f.completada && f.missio.clau == Medalles.CLAU_CARTELL) {
                    val teCromo = estat.fotos.any { it.esCromo }
                    BotoSecundari(
                        stringResource(if (teCromo) R.string.torna_a_fer_foto_cartell else R.string.fes_foto_cartell_cataleg),
                        { onProva(f.missio) },
                        icona = Icones.Camera,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
                    )
                }
            }
        }
        item {
            Seccio(stringResource(R.string.les_teves_missions)) {
                Text(stringResource(R.string.les_teves_missions_text), style = MaterialTheme.typography.bodySmall)
                for (p in estat.propies) FilaPropia(p, onCanviaPropia, onEsborraPropia)
                BotoSecundari(stringResource(R.string.afegeix_missio), { afegint = true }, icona = Icones.Mes)
            }
        }
        item {
            Seccio(stringResource(R.string.fotos)) {
                if (estat.fotos.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(estat.fotos, key = { it.id }) { f -> Miniatura(f, estat.nom) { onObreFoto(f.id) } }
                    }
                }
                BotoSecundari(stringResource(R.string.fes_foto), onFesFoto, icona = Icones.Camera)
            }
        }
        item {
            Seccio(stringResource(R.string.veins)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (v in estat.veins) XipVei(v) { onObreMunicipi(v.codi) }
                }
            }
        }
        item { Spacer(Modifier.navigationBarsPadding().height(24.dp)) }
    }

    if (afegint) {
        DialegMissioPropia(
            onDesa = { t, d ->
                onAfegeixPropia(t, d)
                afegint = false
            },
            onCancela = { afegint = false },
        )
    }
}

@Composable
private fun Seccio(titol: String, contingut: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(titol, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
        contingut()
    }
}

@Composable
private fun FilaDeMissio(f: FilaMissio, provant: Boolean, onProva: () -> Unit) {
    val m = f.missio
    val titol = titolMissio(m)
    val prova = stringResource(if (m.prova == TipusProva.GPS) R.string.prova_gps else R.string.prova_foto)
    val estatText = stringResource(if (f.completada) R.string.completada else R.string.pendent)
    Row(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (f.completada) Colors.FonsCompletada else Colors.Superficie)
            .border(1.dp, if (f.completada) Colors.Disponible2 else Colors.Linia, RoundedCornerShape(14.dp))
            .clickable(enabled = !f.completada && !provant, role = Role.Button, onClick = onProva)
            .semantics(mergeDescendants = true) { stateDescription = estatText }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (f.completada) Colors.Ambre else Color.Transparent)
                .border(2.dp, if (f.completada) Colors.Ambre else Color(0xFF4B5566), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (f.completada) Icon(Icones.Fet, contentDescription = null, tint = Colors.TintaAmbre, modifier = Modifier.size(16.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(titol, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = if (f.completada) Colors.TextSecundari else Colors.Text))
            Text(
                if (f.completada) stringResource(R.string.prova_feta, prova) else stringResource(R.string.prova_toca, prova),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        if (provant) {
            CircularProgressIndicator(Modifier.size(20.dp), color = Colors.Ambre, strokeWidth = 2.dp)
        } else {
            Icon(
                if (m.prova == TipusProva.GPS) Icones.Gps else Icones.Camera,
                contentDescription = null,
                tint = Colors.TextSecundari,
                modifier = Modifier.size(18.dp),
            )
        }
        Text("+${m.punts}", fontFamily = ChakraPetch, fontWeight = FontWeight.Bold, color = Colors.Ambre)
    }
}

/** Títol de la missió: les genèriques tenen el text a strings.xml; les de lloc, el nom del lloc. */
@Composable
fun titolMissio(m: Missio): String = if (m.tipus == TipusMissio.GENERICA) {
    when (m.clau) {
        "checkin" -> stringResource(R.string.missio_checkin)
        "cartell" -> stringResource(R.string.missio_cartell)
        "ajuntament" -> stringResource(R.string.missio_ajuntament)
        else -> m.titol
    }
} else {
    m.titol
}

@Composable
private fun FilaPropia(p: MissioPropia, onCanvia: (String, Boolean) -> Unit, onEsborra: (String) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Colors.Superficie)
            .padding(start = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
                .clickable(role = Role.Checkbox) { onCanvia(p.id, !p.completada) }
                .semantics(mergeDescendants = true) {},
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (p.completada) Colors.Blau else Color.Transparent)
                    .border(2.dp, Colors.Blau, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (p.completada) Icon(Icones.Fet, contentDescription = null, tint = Colors.Fons, modifier = Modifier.size(14.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(p.titol, style = MaterialTheme.typography.bodyLarge)
                p.descripcio?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                Text(
                    stringResource(if (p.completada) R.string.completada else R.string.pendent),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        IconButton(onClick = { onEsborra(p.id) }) {
            Icon(Icones.Esborrar, contentDescription = stringResource(R.string.esborra_missio, p.titol), tint = Colors.TextSecundari)
        }
    }
}

@Composable
private fun Miniatura(f: Foto, nom: String, onClick: () -> Unit) {
    ImatgeLocal(
        f.miniatura,
        stringResource(R.string.foto_de, nom),
        Modifier
            .size(96.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(role = Role.Image, onClick = onClick),
        midaMaxima = 320,
    )
}

@Composable
private fun XipVei(v: FilaVei, onClick: () -> Unit) {
    val (fons, text) = when (v.estat) {
        EstatMunicipi.DESCOBERT -> Colors.Disponible1 to Colors.Ambre
        EstatMunicipi.DISPONIBLE -> Colors.Superficie to Colors.Text
        EstatMunicipi.BOIRA -> Colors.Superficie to Colors.TextSecundari
    }
    val estat = stringResource(
        when (v.estat) {
            EstatMunicipi.DESCOBERT -> R.string.estat_descobert
            EstatMunicipi.DISPONIBLE -> R.string.estat_disponible
            EstatMunicipi.BOIRA -> R.string.estat_boira
        },
    )
    Row(
        Modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(fons)
            .border(1.dp, Colors.Linia, RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { stateDescription = estat }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (v.estat != EstatMunicipi.DESCOBERT) {
            Icon(Icones.Cadenat, contentDescription = null, tint = text, modifier = Modifier.size(14.dp))
        }
        Text(v.nom, style = MaterialTheme.typography.bodyMedium.copy(color = text))
    }
}

@Composable
private fun DialegMissioPropia(onDesa: (String, String) -> Unit, onCancela: () -> Unit) {
    var titol by rememberSaveable { mutableStateOf("") }
    var descripcio by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onCancela,
        containerColor = Colors.Superficie,
        title = { Text(stringResource(R.string.nova_missio)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.nova_missio_text), style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(titol, { titol = it }, label = { Text(stringResource(R.string.titol)) }, singleLine = true)
                OutlinedTextField(descripcio, { descripcio = it }, label = { Text(stringResource(R.string.descripcio_opcional)) })
            }
        },
        confirmButton = {
            TextButton(onClick = { onDesa(titol, descripcio) }, enabled = titol.isNotBlank()) { Text(stringResource(R.string.desa)) }
        },
        dismissButton = { TextButton(onClick = onCancela) { Text(stringResource(R.string.cancela)) } },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FitxaBloquejada(
    estat: MunicipiEstat,
    onEnrere: () -> Unit,
    onDesbloqueja: () -> Unit,
    onObreMunicipi: (CodiIne) -> Unit,
    onVeureAlMapa: () -> Unit,
) {
    LazyColumn(
        Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                BotoIcona(Icones.Enrere, stringResource(R.string.enrere), onEnrere)
                BotoIcona(Icones.Mapa, stringResource(R.string.veure_al_mapa), onVeureAlMapa)
            }
        }
        item {
            Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(200.dp, 150.dp)
                        .clip(RoundedCornerShape(40.dp))
                        .background(if (estat.estat == EstatMunicipi.DISPONIBLE) Colors.Disponible1 else Colors.Superficie),
                )
                Box(
                    Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Colors.Fons)
                        .border(2.dp, Colors.Ambre, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icones.Cadenat, contentDescription = null, tint = Colors.Ambre, modifier = Modifier.size(30.dp))
                }
            }
        }
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                val etiqueta = estat.veiDescobert?.let { stringResource(R.string.vei_de_comarca, it, estat.comarca) } ?: estat.comarca
                Text(etiqueta.uppercase(), style = MaterialTheme.typography.labelMedium.copy(color = Colors.Blau))
                Text(estat.nom, style = MaterialTheme.typography.headlineLarge, modifier = Modifier.semantics { heading() })
                Text(
                    if (estat.estat == EstatMunicipi.DISPONIBLE) {
                        stringResource(R.string.bloquejat_text)
                    } else {
                        estat.distancia?.let { plural(R.plurals.boira_text, it, it) } ?: stringResource(R.string.bloquejat_text)
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(color = Colors.TextSecundari),
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Dada(stringResource(R.string.missions_ocultes_titol), "${estat.missions.size}", Modifier.weight(1f))
                Dada(stringResource(R.string.punts_en_joc), stringResource(R.string.fins_a, estat.puntsPossibles), Modifier.weight(1f), Colors.Ambre)
            }
        }
        if (estat.estat == EstatMunicipi.DISPONIBLE) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.tens_punts, estat.saldo), style = MaterialTheme.typography.bodyMedium.copy(color = Colors.TextSecundari))
                        if (estat.potDesbloquejar) {
                            Text(stringResource(R.string.despres_punts, estat.saldo - estat.cost), style = MaterialTheme.typography.bodyMedium.copy(color = Colors.TextSecundari))
                        }
                    }
                    BotoPrincipal(
                        stringResource(R.string.desbloqueja_cost, estat.cost),
                        onDesbloqueja,
                        enabled = estat.potDesbloquejar,
                        carregant = estat.desbloquejant,
                    )
                    if (!estat.potDesbloquejar) {
                        Text(
                            stringResource(R.string.et_falten, estat.cost - estat.saldo),
                            style = MaterialTheme.typography.bodyMedium.copy(color = Colors.Ambre),
                        )
                    }
                }
            }
        }
        item {
            Text(stringResource(R.string.veins), style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
        }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (v in estat.veins) XipVei(v) { onObreMunicipi(v.codi) }
            }
        }
        item { Spacer(Modifier.navigationBarsPadding()) }
    }
}

@Composable
private fun Dada(etiqueta: String, valor: String, modifier: Modifier = Modifier, color: Color = Colors.Text) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Colors.Superficie)
            .border(1.dp, Colors.Linia, RoundedCornerShape(14.dp))
            .padding(14.dp)
            .semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(etiqueta, style = MaterialTheme.typography.bodySmall)
        Text(valor, style = MaterialTheme.typography.headlineSmall.copy(color = color))
    }
}

