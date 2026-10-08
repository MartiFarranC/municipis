package cat.descobreix.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cat.descobreix.R
import cat.descobreix.joc.model.CodiIne
import cat.descobreix.joc.model.EstatMunicipi
import cat.descobreix.municipality.Distintiu
import cat.descobreix.ui.Celebracio
import cat.descobreix.ui.DialegMissatge
import cat.descobreix.ui.DialegTriaMunicipi
import cat.descobreix.ui.components.BarraProgres
import cat.descobreix.ui.components.BotoIcona
import cat.descobreix.ui.components.Carregant
import cat.descobreix.ui.components.IconaBarretina
import cat.descobreix.ui.components.Icones
import cat.descobreix.ui.esCelebracio
import cat.descobreix.ui.plural
import cat.descobreix.ui.rememberPermisUbicacio
import cat.descobreix.ui.textDe
import cat.descobreix.ui.theme.ChakraPetch
import cat.descobreix.ui.theme.Colors
import cat.descobreix.ui.theme.LocalReduirAnimacions
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop

@OptIn(FlowPreview::class)
@Composable
fun MapaScreen(
    onObreMunicipi: (CodiIne) -> Unit,
    viewModel: MapaViewModel = hiltViewModel(),
) {
    val estat by viewModel.estat.collectAsStateWithLifecycle()
    val geometria = estat.geometria
    val camins = estat.camins
    if (geometria == null || camins == null) {
        Carregant(Modifier.fillMaxSize())
        return
    }
    val camera = remember(geometria) { CameraMapa(geometria.amplada.toFloat(), geometria.alcada.toFloat()) }
    val reduir = LocalReduirAnimacions.current
    val avisos = remember { SnackbarHostState() }
    val demanaUbicacio = rememberPermisUbicacio(onConcedit = viewModel::onSoc, onDenegat = viewModel::sensePermis)

    // Posició inicial: la que es va desar, o tota Catalunya fins que el ViewModel la mogui.
    LaunchedEffect(camera, camera.mida) {
        if (!camera.inicialitzada && camera.mida.width > 0f) {
            val c = estat.cameraInicial
            if (c != null) {
                camera.inicialitza(c.x, c.y, 1f)
                camera.centra(c.x, c.y, c.escala)
            } else {
                camera.inicialitza(geometria.amplada / 2f, geometria.alcada / 2f, 1f)
            }
        }
    }
    LaunchedEffect(estat.ordre, camera.inicialitzada) {
        val o = estat.ordre ?: return@LaunchedEffect
        if (!camera.inicialitzada) return@LaunchedEffect
        camera.centra(o.x, o.y, o.zoom?.let { camera.escalaMinima * it } ?: camera.escala)
    }
    // Desa la posició del mapa quan l'usuari deixa de moure'l.
    LaunchedEffect(camera) {
        snapshotFlow { Triple(camera.desplacament, camera.escala, camera.inicialitzada) }
            .drop(1)
            .debounce(1000)
            .collect { (_, escala, ok) ->
                if (ok) {
                    val c = camera.centreMapa()
                    viewModel.desaCamera(c.x, c.y, escala)
                }
            }
    }
    val textAvis = estat.avis?.takeUnless { it.esCelebracio }?.let { textDe(it) }
    LaunchedEffect(textAvis) {
        if (textAvis != null) {
            avisos.showSnackbar(textAvis)
            viewModel.tancaAvis()
        }
    }

    Box(Modifier.fillMaxSize().background(Colors.Fons)) {
        MapaCanvas(
            geometria = geometria,
            camins = camins,
            estats = estat.estats,
            noms = estat.noms,
            inici = estat.inici,
            seleccionat = estat.seleccio?.index,
            camera = camera,
            animacions = !reduir,
            descripcio = stringResource(R.string.mapa_descripcio, estat.descoberts, estat.total),
            onToc = { viewModel.toca(it.x, it.y) },
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Capcalera(estat)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(
                    Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Colors.Fons.copy(alpha = 0.88f))
                        .border(1.dp, Colors.Linia, RoundedCornerShape(12.dp))
                        .padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Minimapa(
                        camins = camins,
                        geometria = geometria,
                        estats = estat.estats,
                        camera = camera,
                        descripcio = stringResource(R.string.minimapa_descripcio),
                        modifier = Modifier
                            .width(96.dp)
                            .aspectRatio(geometria.amplada.toFloat() / geometria.alcada),
                    )
                    Text(
                        stringResource(R.string.comptador_curt, estat.descoberts, estat.total),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.labelMedium.fontSize),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BotoIcona(Icones.Cercar, stringResource(R.string.cerca_municipi), { viewModel.obreCerca(true) })
                    BotoIcona(Icones.Centrar, stringResource(R.string.centra_territori), viewModel::centraAlTerritori)
                    BotoIcona(
                        Icones.Ubicacio,
                        stringResource(if (estat.buscantUbicacio) R.string.buscant_ubicacio else R.string.on_soc),
                        demanaUbicacio,
                        tint = if (estat.buscantUbicacio) Colors.Blau else Colors.Text,
                    )
                }
            }
        }

        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SnackbarHost(avisos)
            val s = estat.seleccio
            if (s != null) {
                TargetaSeleccio(
                    s,
                    collaborador = s.codi in estat.collaboradors,
                    desbloquejant = estat.desbloquejant,
                    onObre = { onObreMunicipi(s.codi) },
                    onDesbloqueja = viewModel::desbloqueja,
                    onTanca = viewModel::treuSeleccio,
                )
            } else {
                Llegenda()
            }
        }

        if (estat.cercant) {
            PanellCerca(
                text = estat.textCerca,
                resultats = estat.resultatsCerca,
                onText = viewModel::canviaCerca,
                onTria = viewModel::triaResultat,
                onTanca = { viewModel.obreCerca(false) },
            )
        }
    }

    estat.candidats?.let { DialegTriaMunicipi(it, viewModel::triaCandidat, viewModel::tancaCandidats) }
    estat.missatge?.let { DialegMissatge(it, viewModel::tancaMissatge) }
    estat.avis?.takeIf { it.esCelebracio }?.let { Celebracio(it, viewModel::tancaAvis) }
}

@Composable
private fun Capcalera(estat: MapaEstat) {
    val nivell = estat.nivell
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Colors.Fons.copy(alpha = 0.88f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (nivell != null) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Colors.Superficie2)
                    .border(1.dp, Colors.Linia, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text("${nivell.numero}", fontFamily = ChakraPetch, fontWeight = FontWeight.Bold, color = Colors.Ambre)
            }
            Column(Modifier.width(96.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.nivell, nivell.numero).uppercase(), style = MaterialTheme.typography.labelSmall.copy(color = Colors.TextSecundari))
                BarraProgres(nivell.progres, alcada = 6)
            }
        }
        Column(Modifier.weight(1f).semantics(mergeDescendants = true) {}) {
            Text("${estat.descoberts}", style = MaterialTheme.typography.headlineMedium.copy(color = Colors.Ambre))
            Text(stringResource(R.string.comptador_llarg, estat.total), style = MaterialTheme.typography.bodySmall)
        }
        Row(
            Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Colors.Superficie2)
                .border(1.dp, Colors.Linia, RoundedCornerShape(999.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .semantics(mergeDescendants = true) {},
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            IconaBarretina(Modifier.size(18.dp), descripcio = stringResource(R.string.punts))
            Text("${estat.saldo}", fontFamily = ChakraPetch, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TargetaSeleccio(
    s: Seleccio,
    collaborador: Boolean,
    desbloquejant: Boolean,
    onObre: () -> Unit,
    onDesbloqueja: () -> Unit,
    onTanca: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Colors.Superficie)
            .border(1.dp, Colors.Linia, RoundedCornerShape(16.dp))
            .semantics { liveRegion = LiveRegionMode.Polite }
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(
            Modifier
                .weight(1f)
                .clickable(role = Role.Button, onClick = onObre)
                .padding(vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            val etiqueta = when (s.estat) {
                EstatMunicipi.DESCOBERT -> stringResource(R.string.estat_descobert).uppercase()
                EstatMunicipi.DISPONIBLE -> s.veiDe?.let { stringResource(R.string.vei_de, it).uppercase() }
                    ?: stringResource(R.string.estat_disponible).uppercase()
                EstatMunicipi.BOIRA -> stringResource(R.string.estat_boira).uppercase()
            }
            Text(etiqueta, style = MaterialTheme.typography.labelMedium.copy(color = Colors.Blau))
            Text(s.nom, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
            val detall = when (s.estat) {
                EstatMunicipi.DESCOBERT -> stringResource(R.string.missions_fetes, s.missionsFetes, s.missions)
                EstatMunicipi.DISPONIBLE -> plural(R.plurals.missions_ocultes, s.missions, s.missions)
                EstatMunicipi.BOIRA -> s.distancia?.let { plural(R.plurals.distancia_territori, it, it) } ?: s.comarca
            }
            Text("${s.comarca} · $detall", style = MaterialTheme.typography.bodySmall)
            if (collaborador) Distintiu(Modifier.padding(top = 4.dp))
        }
        when {
            s.estat == EstatMunicipi.DISPONIBLE && s.potDesbloquejar -> Button(
                onClick = onDesbloqueja,
                enabled = !desbloquejant,
                modifier = Modifier.heightIn(min = 48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Colors.Ambre, contentColor = Colors.TintaAmbre),
            ) {
                val cost = plural(R.plurals.punts_curt, s.cost, s.cost)
                Row(
                    Modifier.clearAndSetSemantics { contentDescription = cost },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("${s.cost}", fontFamily = ChakraPetch, fontWeight = FontWeight.Bold)
                    IconaBarretina(Modifier.size(18.dp), color = Colors.TintaAmbre)
                }
            }
            else -> Button(
                onClick = onObre,
                modifier = Modifier.heightIn(min = 48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Colors.Superficie2, contentColor = Colors.Text),
            ) {
                Text(stringResource(R.string.obre_fitxa))
            }
        }
        BotoIcona(Icones.Tancar, stringResource(R.string.tanca), onTanca, modifier = Modifier.size(48.dp))
    }
}

@Composable
private fun Llegenda() {
    Row(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Colors.Fons.copy(alpha = 0.85f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ElementLlegenda(Colors.Ambre, stringResource(R.string.estat_descobert))
        ElementLlegenda(Colors.Disponible2, stringResource(R.string.estat_disponible))
        ElementLlegenda(Colors.Superficie2, stringResource(R.string.estat_boira))
    }
}

@Composable
private fun ElementLlegenda(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
                .border(1.dp, Colors.Linia, RoundedCornerShape(3.dp)),
        )
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun PanellCerca(
    text: String,
    resultats: List<OpcioCerca>,
    onText: (String) -> Unit,
    onTria: (OpcioCerca) -> Unit,
    onTanca: () -> Unit,
) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    Column(
        Modifier
            .fillMaxSize()
            .background(Colors.Fons.copy(alpha = 0.96f))
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = text,
                onValueChange = onText,
                modifier = Modifier.weight(1f).focusRequester(focus),
                placeholder = { Text(stringResource(R.string.cerca_municipi)) },
                leadingIcon = { Icon(Icones.Cercar, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Colors.Ambre,
                    unfocusedBorderColor = Colors.Linia,
                    focusedContainerColor = Colors.Superficie,
                    unfocusedContainerColor = Colors.Superficie,
                    cursorColor = Colors.Ambre,
                ),
            )
            BotoIcona(Icones.Tancar, stringResource(R.string.tanca), onTanca)
        }
        for (r in resultats) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(role = Role.Button) { onTria(r) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(r.nom, style = MaterialTheme.typography.bodyLarge)
                Text(r.comarca, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
