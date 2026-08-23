package com.example.myapplication.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.di.AppContainer
import com.example.myapplication.ui.screens.ajustes.AjustesViewModel
import com.example.myapplication.ui.screens.ajustes.PantallaAjustes
import com.example.myapplication.ui.screens.control.ControlViewModel
import com.example.myapplication.ui.screens.control.PantallaControl
import com.example.myapplication.ui.screens.historial.HistorialViewModel
import com.example.myapplication.ui.screens.historial.PantallaHistorial
import com.example.myapplication.ui.screens.manual.PantallaManual
import com.example.myapplication.ui.screens.nuevoensayo.NuevoEnsayoViewModel
import com.example.myapplication.ui.screens.nuevoensayo.PantallaNuevoEnsayo
import com.example.myapplication.ui.screens.telemetria.PantallaTelemetria
import com.example.myapplication.ui.screens.telemetria.TelemetriaViewModel
import com.example.myapplication.ui.theme.KotrilCian

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavegacionRobot(container: AppContainer) {

    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val destinoActual = entradaActual?.destination

    val esPantallaDetalle = destinoActual?.route == Rutas.NuevoEnsayo.ruta

    val rutaPrincipal = Rutas.principales.firstOrNull { r ->
        destinoActual?.hierarchy?.any { it.route == r.ruta } == true
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (!esPantallaDetalle) {
                Column {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    "KOTRIL",
                                    fontSize = 11.sp,
                                    letterSpacing = 4.sp,
                                    color = KotrilCian,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    rutaPrincipal?.titulo?.uppercase() ?: "ROBOT",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                    // Linea de acento bajo la barra
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        KotrilCian.copy(alpha = 0.5f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
            }
        },
        bottomBar = {
            if (!esPantallaDetalle) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    Rutas.principales.forEach { ruta ->
                        val seleccionada = destinoActual
                            ?.hierarchy?.any { it.route == ruta.ruta } == true

                        NavigationBarItem(
                            selected = seleccionada,
                            onClick = {
                                navController.navigate(ruta.ruta) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(ruta.icono, contentDescription = ruta.titulo) },
                            label = {
                                Text(
                                    ruta.titulo.uppercase(),
                                    fontSize = 9.sp,
                                    letterSpacing = 0.8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = KotrilCian,
                                indicatorColor = KotrilCian,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = Rutas.Control.ruta,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            composable(Rutas.Control.ruta) {
                val vm: ControlViewModel = viewModel(
                    factory = ControlViewModel.fabrica(
                        container.robotRepository,
                        container.preferenciasRepository
                    )
                )
                PantallaControl(viewModel = vm)
            }

            composable(Rutas.Telemetria.ruta) {
                val vm: TelemetriaViewModel = viewModel(
                    factory = TelemetriaViewModel.fabrica(
                        container.robotRepository,
                        container.preferenciasRepository
                    )
                )
                PantallaTelemetria(viewModel = vm)
            }

            composable(Rutas.Historial.ruta) {
                val vm: HistorialViewModel = viewModel(
                    factory = HistorialViewModel.fabrica(container.ensayoRepository)
                )
                PantallaHistorial(
                    viewModel = vm,
                    onNuevoEnsayo = { navController.navigate(Rutas.NuevoEnsayo.ruta) }
                )
            }

            composable(Rutas.NuevoEnsayo.ruta) {
                val vm: NuevoEnsayoViewModel = viewModel(
                    factory = NuevoEnsayoViewModel.fabrica(
                        container.ensayoRepository,
                        container.robotRepository,
                        container.gestorUbicacion
                    )
                )
                PantallaNuevoEnsayo(
                    viewModel = vm,
                    onVolver = { navController.popBackStack() }
                )
            }

            composable(Rutas.Manual.ruta) {
                PantallaManual()
            }

            composable(Rutas.Ajustes.ruta) {
                val vm: AjustesViewModel = viewModel(
                    factory = AjustesViewModel.fabrica(
                        container.preferenciasRepository,
                        container.robotRepository
                    )
                )
                PantallaAjustes(viewModel = vm)
            }
        }
    }
}
