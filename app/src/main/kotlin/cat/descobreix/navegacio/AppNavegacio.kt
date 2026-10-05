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
import cat.descobreix.compte.CompteScreen
import cat.descobreix.compte.NomUsuariScreen
import cat.descobreix.compte.NovaContrasenyaScreen
import cat.descobreix.compte.SenseConnexioScreen
import cat.descobreix.data.compte.EstatCompte
import cat.descobreix.map.MapaScreen
import cat.descobreix.missions.MissionsScreen
import cat.descobreix.municipality.MunicipiScreen
import cat.descobreix.onboarding.OnboardingScreen
import cat.descobreix.photos.CameraScreen
import cat.descobreix.photos.FotoScreen
import cat.descobreix.profile.PerfilScreen
import cat.descobreix.profile.SobreScreen
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
    const val FOTO = "foto/{id}"
    const val SOBRE = "sobre"

    fun mapa(centre: String? = null) = if (centre == null) "mapa" else "mapa?centre=$centre"
    fun municipi(codi: String) = "municipi/$codi"
    fun camera(codi: String, missio: String? = null) = if (missio == null) "camera/$codi" else "camera/$codi?missio=$missio"
    fun foto(id: String) = "foto/$id"
}

private data class Pestanya(val ruta: String, val desti: String, val etiqueta: Int, val icona: ImageVector)

private val pestanyes = listOf(
    Pestanya(Rutes.MAPA, Rutes.mapa(), R.string.pestanya_mapa, Icones.Mapa),
    Pestanya(Rutes.MISSIONS, Rutes.MISSIONS, R.string.pestanya_missions, Icones.Missions),
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
        is EstatCompte.Llest -> PantallesJoc(viewModel)
    }
}

@Composable
private fun PantallesJoc(viewModel: AppViewModel) {
    val iniciada by viewModel.partidaIniciada.collectAsStateWithLifecycle()
    val inicial = iniciada
    if (inicial == null) {
        Obertura(Modifier.fillMaxSize())
        return
    }
    // El destí inicial només es decideix una vegada.
    val inici = remember { if (inicial) Rutes.MAPA else Rutes.ONBOARDING }
    val nav = rememberNavController()

    // Si l'usuari esborra totes les dades, torna a començar.
    LaunchedEffect(inicial) {
        if (!inicial && nav.currentDestination?.route != Rutes.ONBOARDING) {
            nav.navigate(Rutes.ONBOARDING) { popUpTo(nav.graph.id) { inclusive = true } }
        }
    }

    val entrada by nav.currentBackStackEntryAsState()
    val rutaActual = entrada?.destination?.route
    Scaffold(
        containerColor = Colors.Fons,
        // Cada pantalla gestiona les barres del sistema (el mapa ocupa tota la pantalla).
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (pestanyes.any { it.ruta == rutaActual }) BarraInferior(nav, rutaActual)
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
            composable(Rutes.PERFIL) {
                PerfilScreen(
                    onObreFoto = { nav.navigate(Rutes.foto(it)) },
                    onObreSobre = { nav.navigate(Rutes.SOBRE) },
                )
            }
            composable(Rutes.MUNICIPI, arguments = listOf(navArgument("codi") { type = NavType.StringType })) {
                MunicipiScreen(
                    onEnrere = { nav.popBackStack() },
                    onObreMunicipi = { nav.navigate(Rutes.municipi(it)) },
                    onFesFoto = { codi, missio -> nav.navigate(Rutes.camera(codi, missio)) },
                    onObreFoto = { nav.navigate(Rutes.foto(it)) },
                    onVeureAlMapa = { codi -> irAlMapa(nav, codi) },
                )
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
private fun BarraInferior(nav: NavHostController, rutaActual: String?) {
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
