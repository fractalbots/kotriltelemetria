package com.example.myapplication.data.remote.dto

import com.example.myapplication.domain.model.EstadoMotores
import com.example.myapplication.domain.model.Telemetria
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * DTO: refleja EXACTAMENTE el JSON que devuelve el ESP32.
 *
 * Los nombres feos y las abreviaturas del firmware se quedan
 * aqui. El resto de la app trabaja con el modelo de dominio.
 */
@JsonClass(generateAdapter = false)
data class TelemetriaDto(
    @Json(name = "pitch")       val pitch: Float = 0f,
    @Json(name = "roll")        val roll: Float = 0f,
    @Json(name = "yaw")         val yaw: Float = 0f,
    @Json(name = "temperatura") val temperatura: Float = 0f,
    @Json(name = "gasCrudo")    val gasCrudo: Int = 0,
    @Json(name = "gasIndice")   val gasIndice: Int = 0,
    @Json(name = "paro")        val paro: Boolean = false,
    @Json(name = "mpuOk")       val mpuOk: Boolean = false,
    @Json(name = "rssi")        val rssi: Int = 0,
    @Json(name = "motores")     val motores: MotoresDto = MotoresDto(),
    @Json(name = "timestamp")   val timestamp: Long = 0L
)

@JsonClass(generateAdapter = false)
data class MotoresDto(
    @Json(name = "frontalIzq") val frontalIzq: Int = 0,
    @Json(name = "frontalDer") val frontalDer: Int = 0,
    @Json(name = "traseroIzq") val traseroIzq: Int = 0,
    @Json(name = "traseroDer") val traseroDer: Int = 0
)

@JsonClass(generateAdapter = false)
data class ComandoMovimientoDto(
    @Json(name = "vx") val vx: Float,
    @Json(name = "vy") val vy: Float,
    @Json(name = "w")  val w: Float
)

@JsonClass(generateAdapter = false)
data class RespuestaSimpleDto(
    @Json(name = "ok")      val ok: Boolean = false,
    @Json(name = "mensaje") val mensaje: String? = null
)

/**
 * MAPPER: DTO -> Dominio.
 *
 * Toda la traduccion entre "lo que habla el robot" y "lo que
 * entiende la app" ocurre en este unico lugar.
 */
fun TelemetriaDto.aDominio(): Telemetria = Telemetria(
    pitch = pitch,
    roll = roll,
    yaw = yaw,
    temperaturaSensor = temperatura,
    indiceGas = gasIndice.coerceIn(0, 100),
    paroActivo = paro,
    sensorOk = mpuOk,
    calidadSenal = rssi,
    motores = EstadoMotores(
        frontalIzq = motores.frontalIzq,
        frontalDer = motores.frontalDer,
        traseroIzq = motores.traseroIzq,
        traseroDer = motores.traseroDer
    )
)
