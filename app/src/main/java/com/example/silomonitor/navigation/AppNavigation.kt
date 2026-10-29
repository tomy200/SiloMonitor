package com.example.silomonitor.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.silomonitor.R
import com.example.silomonitor.model.TipoMovimiento
import com.example.silomonitor.ui.screens.alertas.AlertasScreen
import com.example.silomonitor.ui.screens.detalle.DetalleSiloScreen
import com.example.silomonitor.ui.screens.incidencia.ReportarIncidenciaScreen
import com.example.silomonitor.ui.screens.login.LoginScreen
import com.example.silomonitor.ui.screens.movimiento.RegistrarMovimientoScreen
import com.example.silomonitor.ui.screens.perfil.PerfilScreen
import com.example.silomonitor.ui.screens.registro.RegistroScreen
import com.example.silomonitor.ui.screens.resumen.ResumenScreen
import com.example.silomonitor.ui.screens.silos.SilosScreen
import com.example.silomonitor.ui.screens.splash.SplashScreen
import com.example.silomonitor.ui.utils.esCompacta
import com.example.silomonitor.util.NotificacionHelper
import com.example.silomonitor.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest

/** Pestaña de la barra de navegación (inferior en celular, lateral en tablet). */
private data class Pestana(
    val screen: Screen,
    val etiqueta: String,
    val iconoSeleccionado: ImageVector? = null,
    val iconoNormal: ImageVector? = null,
    @DrawableRes val iconoRecurso: Int? = null
)

private val pestanas = listOf(
    Pestana(Screen.Resumen, "Resumen", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    Pestana(Screen.Silos, "Silos", iconoRecurso = R.drawable.ic_silo),
    Pestana(Screen.Alertas, "Alertas", Icons.Filled.Notifications, Icons.Outlined.Notifications),
    Pestana(Screen.Perfil, "Perfil", Icons.Filled.Person, Icons.Outlined.Person)
)

private val rutasPestanas = pestanas.map { it.screen.route }

private fun tituloPara(ruta: String?): String = when (ruta) {
    Screen.Resumen.route -> "Resumen general"
    Screen.Silos.route -> "Silos por granja"
    Screen.Alertas.route -> "Alertas"
    Screen.Perfil.route -> "Mi perfil"
    Rutas.DETALLE_SILO -> "Detalle del silo"
    Rutas.REGISTRAR_MOVIMIENTO -> "Registrar movimiento"
    Rutas.REPORTAR_INCIDENCIA -> "Reportar incidencia"
    else -> "SiloMonitor"
}

private fun NavBackStackEntry.esPestana(): Boolean = destination.route in rutasPestanas

/**
 * Navegación de toda la app: NavHost + barras de navegación adaptables (Guías 9 y 10).
 * - Celular (Compact): NavigationBar inferior.
 * - Tablet / horizontal (Medium y Expanded): NavigationRail lateral.
 * Los eventos de navegación llegan desde MainViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppNavigation(
    mainViewModel: MainViewModel,
    windowSizeClass: WindowSizeClass
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val alertasActivas by mainViewModel.alertasActivas.collectAsState()
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route

    // Escuchar eventos de navegación emitidos por el ViewModel
    LaunchedEffect(Unit) {
        mainViewModel.navigationEvents.collectLatest { event ->
            when (event) {
                is NavigationEvent.NavigateTo -> navController.navigate(event.route.rutaNavegable()) {
                    if (event.limpiarPila) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    } else {
                        event.popUpToRoute?.let { destino ->
                            popUpTo(destino.route) {
                                inclusive = event.inclusive
                                saveState = event.guardarEstado
                            }
                        }
                    }
                    launchSingleTop = event.singleTop
                    restoreState = event.guardarEstado
                }
                NavigationEvent.PopBackStack -> navController.popBackStack()
                NavigationEvent.NavigateUp -> navController.navigateUp()
            }
        }
    }

    val enAreaPrivada = rutaActual != null && rutaActual != Screen.Splash.route && rutaActual != Screen.Login.route && rutaActual != Screen.Registro.route
    val esPestana = rutaActual in rutasPestanas
    val compacta = windowSizeClass.esCompacta

    if (enAreaPrivada) SolicitarPermisoNotificaciones()

    Row(Modifier.fillMaxSize()) {
        // ---------- Barra lateral (tablet) ----------
        if (enAreaPrivada && esPestana && !compacta) {
            NavigationRail(
                header = {
                    Image(
                        painter = painterResource(R.drawable.logo_silo),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .size(44.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                }
            ) {
                pestanas.forEach { pestana ->
                    val seleccionada = rutaActual == pestana.screen.route
                    NavigationRailItem(
                        selected = seleccionada,
                        onClick = { mainViewModel.navegarPestana(pestana.screen) },
                        icon = { IconoPestana(pestana, seleccionada, alertasActivas) },
                        label = { Text(pestana.etiqueta) }
                    )
                }
            }
        }

        Scaffold(
            modifier = Modifier.weight(1f),
            topBar = {
                if (enAreaPrivada) {
                    TopAppBar(
                        title = { Text(tituloPara(rutaActual)) },
                        navigationIcon = {
                            if (!esPestana) {
                                IconButton(onClick = mainViewModel::navigateBack) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                                }
                            }
                        }
                    )
                }
            },
            bottomBar = {
                // ---------- Barra inferior (celular) ----------
                if (enAreaPrivada && esPestana && compacta) {
                    NavigationBar {
                        pestanas.forEach { pestana ->
                            val seleccionada = rutaActual == pestana.screen.route
                            NavigationBarItem(
                                selected = seleccionada,
                                onClick = { mainViewModel.navegarPestana(pestana.screen) },
                                icon = { IconoPestana(pestana, seleccionada, alertasActivas) },
                                label = { Text(pestana.etiqueta) }
                            )
                        }
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route,
                // Splash y Login dibujan su propio fondo detrás de las barras del sistema
                modifier = if (enAreaPrivada) {
                    Modifier
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding)
                } else {
                    Modifier
                },
                // Transiciones animadas entre pantallas
                enterTransition = {
                    if (initialState.esPestana() && targetState.esPestana()) {
                        fadeIn(tween(250))
                    } else {
                        slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) +
                            fadeIn(tween(300))
                    }
                },
                exitTransition = {
                    if (initialState.esPestana() && targetState.esPestana()) {
                        fadeOut(tween(200))
                    } else {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Start,
                            tween(300),
                            targetOffset = { it / 4 }
                        ) + fadeOut(tween(300))
                    }
                },
                popEnterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.End,
                        tween(300),
                        initialOffset = { it / 4 }
                    ) + fadeIn(tween(300))
                },
                popExitTransition = {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) +
                        fadeOut(tween(300))
                }
            ) {
                composable(Screen.Splash.route) {
                    SplashScreen(onTerminado = { destino ->
                        mainViewModel.navigateTo(destino, limpiarPila = true)
                    })
                }
                composable(Screen.Login.route) {
                    LoginScreen(
                        onLoginExitoso = { mainViewModel.navigateTo(Screen.Resumen, limpiarPila = true) },
                        onIrARegistro = { mainViewModel.navigateTo(Screen.Registro, singleTop = true) }
                    )
                }
                composable(Screen.Registro.route) {
                    RegistroScreen(
                        onRegistroExitoso = { mainViewModel.navigateTo(Screen.Resumen, limpiarPila = true) },
                        onVolverAlLogin = mainViewModel::navigateBack
                    )
                }
                composable(Screen.Resumen.route) {
                    ResumenScreen(
                        anchoVentana = windowSizeClass.widthSizeClass,
                        snackbarHostState = snackbarHostState,
                        onVerSilo = { id -> mainViewModel.navigateTo(Screen.DetalleSilo(id)) },
                        onVerAlertas = { mainViewModel.navegarPestana(Screen.Alertas) }
                    )
                }
                composable(Screen.Silos.route) {
                    SilosScreen(
                        windowSizeClass = windowSizeClass,
                        snackbarHostState = snackbarHostState,
                        onVerSilo = { id -> mainViewModel.navigateTo(Screen.DetalleSilo(id)) }
                    )
                }
                composable(Screen.Alertas.route) {
                    AlertasScreen(
                        snackbarHostState = snackbarHostState,
                        onVerSilo = { id -> mainViewModel.navigateTo(Screen.DetalleSilo(id)) },
                        onRegistrarCarga = { id ->
                            mainViewModel.navigateTo(Screen.RegistrarMovimiento(id, TipoMovimiento.CARGA.name))
                        }
                    )
                }
                composable(Screen.Perfil.route) {
                    PerfilScreen(
                        snackbarHostState = snackbarHostState,
                        onSesionCerrada = { mainViewModel.navigateTo(Screen.Login, limpiarPila = true) }
                    )
                }
                composable(
                    route = Rutas.DETALLE_SILO,
                    arguments = listOf(navArgument(Rutas.ARG_SILO_ID) { type = NavType.StringType })
                ) {
                    DetalleSiloScreen(
                        windowSizeClass = windowSizeClass,
                        onRegistrarMovimiento = { id -> mainViewModel.navigateTo(Screen.RegistrarMovimiento(id)) },
                        onReportarIncidencia = { id -> mainViewModel.navigateTo(Screen.ReportarIncidencia(id)) }
                    )
                }
                composable(
                    route = Rutas.REGISTRAR_MOVIMIENTO,
                    arguments = listOf(
                        navArgument(Rutas.ARG_SILO_ID) { type = NavType.StringType },
                        navArgument(Rutas.ARG_TIPO) {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) {
                    RegistrarMovimientoScreen(
                        snackbarHostState = snackbarHostState,
                        onVolver = mainViewModel::navigateBack
                    )
                }
                composable(
                    route = Rutas.REPORTAR_INCIDENCIA,
                    arguments = listOf(navArgument(Rutas.ARG_SILO_ID) { type = NavType.StringType })
                ) {
                    ReportarIncidenciaScreen(
                        snackbarHostState = snackbarHostState,
                        onVolver = mainViewModel::navigateBack
                    )
                }
            }
        }
    }
}

@Composable
private fun IconoPestana(pestana: Pestana, seleccionada: Boolean, alertasActivas: Int) {
    val contenido: @Composable () -> Unit = {
        when {
            pestana.iconoRecurso != null ->
                Icon(painterResource(pestana.iconoRecurso), contentDescription = null)
            else -> {
                val vector = if (seleccionada) pestana.iconoSeleccionado else pestana.iconoNormal
                if (vector != null) Icon(vector, contentDescription = null)
            }
        }
    }
    // Badge con la cantidad de alertas activas
    if (pestana.screen == Screen.Alertas && alertasActivas > 0) {
        BadgedBox(badge = { Badge { Text(alertasActivas.toString()) } }) { contenido() }
    } else {
        contenido()
    }
}

/** Android 13+: solicita el permiso de notificaciones al entrar a la app (una vez). */
@Composable
private fun SolicitarPermisoNotificaciones() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (!NotificacionHelper.tienePermiso(context)) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
