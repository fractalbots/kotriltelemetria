package com.example.myapplication.domain.model

/**
 * Modelo de DOMINIO.
 *
 * Esta clase no sabe que existe Retrofit, ni JSON, ni el ESP32.
 * Es lo que la app entiende por "estado del robot".
 *
 * Separarlo del DTO permite que si manana cambia el formato del
 * JSON del robot, solo se toque el mapper y no toda la app.
 */
data class Telemetria(
    val pitch: Float,
    val roll: Float,
    val yaw: Float,
    val temperaturaSensor: Float,   // interna del chip MPU6050
    val indiceGas: Int,             // 0..100
    val paroActivo: Boolean,
    val sensorOk: Boolean,
    val calidadSenal: Int,          // dBm
    val motores: EstadoMotores
) {
    /** Inclinacion total: util para detectar riesgo de volcadura. */
    val inclinacionTotal: Float
        get() = maxOf(kotlin.math.abs(pitch), kotlin.math.abs(roll))

    val enMovimiento: Boolean
        get() = motores.algunoActivo
}

data class EstadoMotores(
    val frontalIzq: Int,
    val frontalDer: Int,
    val traseroIzq: Int,
    val traseroDer: Int
) {
    val algunoActivo: Boolean
        get() = frontalIzq != 0 || frontalDer != 0 ||
                traseroIzq != 0 || traseroDer != 0
}
