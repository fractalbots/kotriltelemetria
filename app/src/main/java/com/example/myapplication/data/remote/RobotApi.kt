package com.example.myapplication.data.remote

import com.example.myapplication.data.remote.dto.ComandoMovimientoDto
import com.example.myapplication.data.remote.dto.RespuestaSimpleDto
import com.example.myapplication.data.remote.dto.TelemetriaDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * API REST del robot (servidor propio corriendo en el ESP32).
 *
 * Se usa @Url en lugar de rutas fijas porque la IP del robot es
 * configurable desde Ajustes. Con @Url la URL completa se pasa en
 * cada llamada y no hay que reconstruir Retrofit cuando cambia.
 *
 * Es una decision de diseno defendible en la sustentacion: evita
 * mantener un cliente HTTP por cada IP posible.
 */
interface RobotApi {

    @GET
    suspend fun obtenerTelemetria(@Url url: String): TelemetriaDto

    @POST
    suspend fun mover(
        @Url url: String,
        @Body comando: ComandoMovimientoDto
    ): RespuestaSimpleDto

    @POST
    suspend fun detener(@Url url: String): RespuestaSimpleDto

    @POST
    suspend fun reiniciarYaw(@Url url: String): RespuestaSimpleDto
}

/** Construye las URLs completas a partir de la IP guardada. */
object RutasRobot {
    fun base(ip: String) = "http://$ip"
    fun telemetria(ip: String) = "${base(ip)}/telemetria"
    fun mover(ip: String) = "${base(ip)}/mover"
    fun parar(ip: String) = "${base(ip)}/parar"
    fun resetYaw(ip: String) = "${base(ip)}/reset-yaw"
}
