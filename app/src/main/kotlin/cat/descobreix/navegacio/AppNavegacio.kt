package cat.descobreix.navegacio

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cat.descobreix.R
import cat.descobreix.cataleg.CatalegScreen
import cat.descobreix.compte.CompteScreen
import cat.descobreix.compte.NomUsuariScreen
import cat.descobreix.compte.NovaContrasenyaScreen
import cat.descobreix.compte.SenseConnexioScreen
import cat.descobreix.compte.VisibilitatScreen
import cat.descobreix.data.compte.EstatCompte
import cat.descobreix.data.compte.TipusPerfil
import cat.descobreix.gent.GentScreen
import cat.descobreix.gent.PersonaScreen
import cat.descobreix.gent.SeguitsScreen
import cat.descobreix.map.MapaScreen
import cat.descobreix.missions.MissionsScreen
import cat.descobreix.municipality.MunicipiScreen
import cat.descobreix.onboarding.OnboardingScreen
import cat.descobreix.passaport.PassaportScreen
import cat.descobreix.passaport.SegellarScreen
import cat.descobreix.photos.CameraScreen
import cat.descobreix.photos.LectorQrScreen
import cat.descobreix.photos.FotoScreen
import cat.descobreix.profile.PerfilEspectadorScreen
import cat.descobreix.profile.PerfilScreen
import cat.descobreix.profile.SobreScreen
import cat.descobreix.sacs.AvisSacs
import cat.descobreix.sacs.SacsScreen
import cat.descobreix.ui.CelebracioMedalles
import cat.descobreix.ui.components.Icones
import cat.descobreix.ui.components.Obertura
import cat.descobreix.ui.theme.Colors

object Rutes {
    const val ONBOARDING = "onboarding"
    const val MAPA = "mapa?centre={centre}"
    const val MISSIONS = "missions"
    const val PERFIL = "perfil"
    const val MUNICIPI = "municipi/{codi}"
    const val CAMERA = "camera/{codi}?missio={missio}"
    const val QR = "qr/{codi}/{missio}"
    const val FOTO = "foto/{id}"
    const val SOBRE = "sobre"
    const val PASSAPORT = "passaport"
    const val SEGELLA = "segella/{codi}"
    const val SACS = "sacs?obre={obre}"
    const val CATALEG = "cataleg"
    const val GENT = "gent"
    const val PERSONA = "persona/{id}"
    const val SEGUITS = "seguits"

    fun mapa(centre: String? = null) = if (centre == null) "mapa" else "mapa?centre=$centre"
    fun municipi(codi: String) = "municipi/$codi"
    fun camera(codi: String, missio: String? = null) = if (missio == null) "camera/$codi" else "camera/$codi?missio=$missio"
    fun qr(codi: String, missio: String) = "qr/$codi/$missio"
    fun foto(id: String) = "foto/$id"
    fun segella(codi: String) = "segella/$codi"
    fun sacs(obre: Boolean = false) = "sacs?obre=$obre"
    fun persona(id: String) = "persona/$id"
}

private data class Pestanya(val ruta: String, val desti: String, val etiqueta: Int, val icona: ImageVector)

private val pestanyesExplorador = listOf(
    Pestanya(Rutes.MAPA, Rutes.mapa(), R.string.pestanya_mapa, Icones.Mapa),
    Pestanya(Rutes.MISSIONS, Rutes.MISSIONS, R.string.pestanya_missions, Icones.Missions),
    Pestanya(Rutes.GENT, Rutes.GENT, R.string.pestanya_gent, Icones.Gent),
    Pestanya(Rutes.PERFIL, Rutes.PERFIL, R.string.pestanya_perfil, Icones.Perfil),
)

// L'Espectador no juga: només té la gent que segueix i el perfil.
private val pestanyesEspectador = listOf(
    Pestanya(Rutes.GENT, Rutes.GENT, R.string.pestanya_gent, Icones.Gent),
    Pestanya(Rutes.PERFIL, Rutes.PERFIL, R.string.pestanya_perfil, Icones.Perfil),
)

@Composable
fun AppNavegacio(viewModel: AppViewModel = hiltViewModel()) {
    val compte by viewModel.compte.collectAsStateWithLifecycle()
    when (compte) {
        EstatCompte.Carregant -> Obertura(Modifier.fillMaxSize())
        EstatCompte.SenseSessio -> CompteScreen()
        EstatCompte.CalPerfil -> NomUsuariScreen()
        EstatCompte.SenseConnexio -> SenseConnexioScreen()
        EstatCompte.CalNovaContrasenya -> NovaContrasenyaScreen()
        is EstatCompte.CalVisibilitat -> VisibilitatScreen((compte as EstatCompte.CalVisibilitat).nomUsuari)
        is EstatCompte.Llest -> PantallesJoc(viewModel, (compte as EstatCompte.Llest).perfil.tipus == TipusPerfil.ESPECTADOR)
    }
}

@Composable
private fun PantallesJoc(viewModel: AppViewModel, esEspectador: Boolean) {
    val iniciada by viewModel.partidaIniciada.collectAsStateWithLifecycle()
    val inicial = iniciada
    if (inicial == null && !esEspectador) {
        Obertura(Modifier.fillMaxSize())
        return
    }
    val pestanyes = if (esEspectador) pestanyesEspectador else pestanyesExplorador
    // El destí inicial només es decideix una vegada. L'Espectador no juga: comença pel mur.
    val inici = remember {
        when {
            esEspectador -> Rutes.GENT
            inicial == true -> Rutes.MAPA
            else -> Rutes.ONBOARDING
        }
    }
    val nav = rememberNavController()

    // Si l'usuari esborra totes les dades, torna a començar.
    LaunchedEffect(inicial) {
        if (!esEspectador && inicial == false && nav.currentDestination?.route != Rutes.ONBOARDING) {
            nav.navigate(Rutes.ONBOARDING) { popUpTo(nav.graph.id) { inclusive = true } }
        }
    }

    val entrada by nav.currentBackStackEntryAsState()
    val rutaActual = entrada?.destination?.route
    val celebracio by viewModel.celebracio.collectAsStateWithLifecycle()
    Box(Modifier.fillMaxSize()) {
    Scaffold(
        containerColor = Colors.Fons,
        // Cada pantalla gestiona les barres del sistema (el mapa ocupa tota la pantalla).
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (pestanyes.any { it.ruta == rutaActual }) BarraInferior(nav, rutaActual, pestanyes)
        },
    ) { padding ->
        NavHost(nav, startDestination = inici, modifier = Modifier.padding(padding)) {
            composable(Rutes.ONBOARDING) {
                OnboardingScreen(onComencat = { codi ->
                    nav.navigate(Rutes.mapa(codi)) { popUpTo(nav.graph.id) { inclusive = true } }
                })
            }
            composable(
                Rutes.MAPA,
                arguments = listOf(navArgument("centre") { type = NavType.StringType; nullable = true; defaultValue = null }),
            ) {
                MapaScreen(onObreMunicipi = { nav.navigate(Rutes.municipi(it)) })
            }
            composable(Rutes.MISSIONS) {
                MissionsScreen(onObreMunicipi = { nav.navigate(Rutes.municipi(it)) })
            }
            composable(Rutes.GENT) {
                GentScreen(onObrePersona = { nav.navigate(Rutes.persona(it)) })
            }
            composable(Rutes.PERSONA, arguments = listOf(navArgument("id") { type = NavType.StringType })) {
                PersonaScreen(onEnrere = { nav.popBackStack() }, onObrePersona = { nav.navigate(Rutes.persona(it)) })
            }
            composable(Rutes.SEGUITS) {
                SeguitsScreen(onEnrere = { nav.popBackStack() }, onObrePersona = { nav.navigate(Rutes.persona(it)) })
            }
            composable(Rutes.PERFIL) {
                if (esEspectador) {
                    PerfilEspectadorScreen(onObreSeguits = { nav.navigate(Rutes.SEGUITS) }, onObreSobre = { nav.navigate(Rutes.SOBRE) })
                    return@composable
                }
                PerfilScreen(
                    onObreFoto = { nav.navigate(Rutes.foto(it)) },
                    onObreSobre = { nav.navigate(Rutes.SOBRE) },
                    onObrePassaport = { nav.navigate(Rutes.PASSAPORT) },
                    onObreSacs = { nav.navigate(Rutes.sacs()) },
                    onObreCataleg = { nav.navigate(Rutes.CATALEG) },
                    onObreSeguits = { nav.navigate(Rutes.SEGUITS) },
                )
            }
            composable(Rutes.MUNICIPI, arguments = listOf(navArgument("codi") { type = NavType.StringType })) {
                MunicipiScreen(
                    onEnrere = { nav.popBackStack() },
                    onObreMunicipi = { nav.navigate(Rutes.municipi(it)) },
                    onFesFoto = { codi, missio -> nav.navigate(Rutes.camera(codi, missio)) },
                    onObreFoto = { nav.navigate(Rutes.foto(it)) },
                    onVeureAlMapa = { codi -> irAlMapa(nav, codi) },
                    onSegella = { nav.navigate(Rutes.segella(it)) },
                    onLlegeixQr = { codi, missio -> nav.navigate(Rutes.qr(codi, missio)) },
                )
            }
            composable(
                Rutes.QR,
                arguments = listOf(navArgument("codi") { type = NavType.StringType }, navArgument("missio") { type = NavType.StringType }),
            ) {
                LectorQrScreen(onTanca = { nav.popBackStack() })
            }
            composable(
                Rutes.CAMERA,
                arguments = listOf(
                    navArgument("codi") { type = NavType.StringType },
                    navArgument("missio") { type = NavType.StringType; nullable = true; defaultValue = null },
                ),
            ) {
                CameraScreen(onTanca = { nav.popBackStack() })
            }
            composable(Rutes.FOTO, arguments = listOf(navArgument("id") { type = NavType.StringType })) {
                FotoScreen(onEnrere = { nav.popBackStack() })
            }
            composable(Rutes.SOBRE) {
                SobreScreen(onEnrere = { nav.popBackStack() })
            }
            composable(Rutes.PASSAPORT) {
                PassaportScreen(onEnrere = { nav.popBackStack() }, onSegella = { nav.navigate(Rutes.segella(it)) })
            }
            composable(Rutes.SEGELLA, arguments = listOf(navArgument("codi") { type = NavType.StringType })) {
                SegellarScreen(onFet = { nav.popBackStack() })
            }
            composable(Rutes.CATALEG) {
                CatalegScreen(onEnrere = { nav.popBackStack() }, onObreMunicipi = { nav.navigate(Rutes.municipi(it)) })
            }
            composable(Rutes.SACS, arguments = listOf(navArgument("obre") { type = NavType.BoolType; defaultValue = false })) { e ->
                SacsScreen(onEnrere = { nav.popBackStack() }, obreAra = e.arguments?.getBoolean("obre") == true)
            }
        }
    }
    celebracio?.let { c -> CelebracioMedalles(c.noves, c.nomsComarques, c.siluetes, viewModel::tancaCelebracio) }
    // Els sacs nous s'anuncien quan ja no hi ha cap celebració de medalles, i no mentre s'obren.
    val sacsNous by viewModel.sacsNous.collectAsStateWithLifecycle()
    if (celebracio == null && sacsNous > 0 && rutaActual != Rutes.SACS) {
        AvisSacs(
            sacsNous,
            onObre = {
                viewModel.tancaAvisSacs()
                nav.navigate(Rutes.sacs(obre = true))
            },
            onDespres = viewModel::tancaAvisSacs,
        )
    }
    }
}

private fun irAlMapa(nav: NavHostController, codi: String) {
    nav.navigate(Rutes.mapa(codi)) {
        popUpTo(nav.graph.findStartDestination().id)
        launchSingleTop = false
    }
}

@Composable
private fun BarraInferior(nav: NavHostController, rutaActual: String?, pestanyes: List<Pestanya>) {
    NavigationBar(containerColor = Colors.Fons, tonalElevation = 0.dp) {
        for (p in pestanyes) {
            val seleccionada = p.ruta == rutaActual
            NavigationBarItem(
                selected = seleccionada,
                onClick = {
                    if (!seleccionada) {
                        nav.navigate(p.desti) {
                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Box { Icon(p.icona, contentDescription = null, modifier = Modifier.size(24.dp)) } },
                label = { Text(stringResource(p.etiqueta)) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Colors.Ambre,
                    selectedTextColor = Colors.Ambre,
                    indicatorColor = Colors.Superficie2,
                    unselectedIconColor = Colors.TextSecundari,
                    unselectedTextColor = Colors.TextSecundari,
                ),
            )
        }
    }
}
