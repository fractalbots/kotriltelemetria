package com.example.myapplication.domain.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Ensayo del robot: modelo de dominio.
 *
 * No conoce Room ni las anotaciones de persistencia.
 */
data class Ensayo(
    val id: Long = 0,
    val titulo: String,
    val notas: String,
    val fechaHora: Long,
    val pitch: Float,
    val roll: Float,
    val inclinacionMaxima: Float,
    val temperatura: Float,
    val indiceGas: Int,
    val uriFoto: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null
) {

    val tieneFoto: Boolean get() = !uriFoto.isNullOrBlank()
    val tieneUbicacion: Boolean get() = latitud != null && longitud != null

    val fechaLegible: String
        get() = SimpleDateFormat("dd/MM/yyyy  HH:mm", Locale.getDefault())
            .format(Date(fechaHora))

    val ubicacionLegible: String
        get() = if (tieneUbicacion)
            String.format(Locale.US, "%.5f, %.5f", latitud, longitud)
        else "Sin ubicacion"
}
