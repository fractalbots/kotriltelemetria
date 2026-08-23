package com.example.myapplication.domain.model

/**
 * Preferencias del usuario.
 *
 * Modelo de dominio: no sabe que por debajo se guardan con
 * DataStore. Si manana cambiara el mecanismo de persistencia,
 * esta clase no se toca.
 */
data class Preferencias(
    val ipRobot: String = IP_POR_DEFECTO,
    val velocidadMaxima: Float = 0.7f,
    val modoOscuro: Boolean = true,
    val usarFahrenheit: Boolean = false,
    val umbralGas: Int = 60,

    /** true = joysticks analogicos · false = pad de botones */
    val modoJoystick: Boolean = true,

    /** Vibracion al usar los controles */
    val hapticaActiva: Boolean = true
) {
    companion object {
        const val IP_POR_DEFECTO = "192.168.1.200"
    }
}
