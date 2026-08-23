package com.example.myapplication.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Rutas centralizadas.
 *
 * Tenerlas como objetos y no como strings sueltos por todo el
 * codigo evita el error clasico de escribir mal un destino y
 * descubrirlo recien en tiempo de ejecucion.
 */
sealed class Rutas(
    val ruta: String,
    val titulo: String,
    val icono: ImageVector
) {
    data object Control    : Rutas("control", "Control", Icons.Filled.Gamepad)
    data object Telemetria : Rutas("telemetria", "Datos", Icons.Filled.ShowChart)
    data object Historial  : Rutas("historial", "Ensayos", Icons.Filled.History)
    data object Manual     : Rutas("manual", "Manual", Icons.Filled.MenuBook)
    data object Ajustes    : Rutas("ajustes", "Ajustes", Icons.Filled.Settings)

    /** Pantalla de detalle: no aparece en la barra inferior. */
    data object NuevoEnsayo : Rutas("nuevo_ensayo", "Nuevo ensayo", Icons.Filled.History)

    companion object {
        val principales = listOf(Control, Telemetria, Historial, Manual, Ajustes)
    }
}
