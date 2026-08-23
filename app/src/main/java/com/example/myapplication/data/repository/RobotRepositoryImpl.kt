package com.example.myapplication.data.repository

import com.example.myapplication.data.remote.RobotApi
import com.example.myapplication.data.remote.RutasRobot
import com.example.myapplication.data.remote.dto.ComandoMovimientoDto
import com.example.myapplication.data.remote.dto.aDominio
import com.example.myapplication.domain.model.Telemetria
import com.example.myapplication.domain.repository.Resultado
import com.example.myapplication.domain.repository.RobotRepository
import com.example.myapplication.domain.repository.TipoError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Implementacion real: habla con el ESP32.
 *
 * Responsabilidades:
 *  - Ejecutar en Dispatchers.IO (nunca red en el hilo principal)
 *  - Traducir DTO -> dominio
 *  - Convertir excepciones en errores tipados
 *
 * El ViewModel nunca ve una excepcion de Retrofit.
 */
class RobotRepositoryImpl(
    private val api: RobotApi,
    private val proveedorIp: () -> String
) : RobotRepository {

    override suspend fun obtenerTelemetria(): Resultado<Telemetria> =
        ejecutar {
            api.obtenerTelemetria(RutasRobot.telemetria(proveedorIp())).aDominio()
        }

    override suspend fun mover(vx: Float, vy: Float, w: Float): Resultado<Unit> =
        ejecutar {
            api.mover(
                RutasRobot.mover(proveedorIp()),
                ComandoMovimientoDto(vx, vy, w)
            )
            Unit
        }

    override suspend fun detener(): Resultado<Unit> =
        ejecutar {
            api.detener(RutasRobot.parar(proveedorIp()))
            Unit
        }

    override suspend fun reiniciarOrientacion(): Resultado<Unit> =
        ejecutar {
            api.reiniciarYaw(RutasRobot.resetYaw(proveedorIp()))
            Unit
        }

    /**
     * Envoltorio comun: mueve el trabajo a IO y clasifica el error.
     * Sin esto habria el mismo try/catch repetido cuatro veces.
     */
    private suspend fun <T> ejecutar(bloque: suspend () -> T): Resultado<T> =
        withContext(Dispatchers.IO) {
            try {
                Resultado.Exito(bloque())
            } catch (e: SocketTimeoutException) {
                Resultado.Error(TipoError.TIEMPO_AGOTADO, e.message)
            } catch (e: IOException) {
                // Cubre robot apagado, IP equivocada, sin WiFi
                Resultado.Error(TipoError.SIN_CONEXION, e.message)
            } catch (e: HttpException) {
                Resultado.Error(TipoError.RESPUESTA_INVALIDA, "HTTP ${e.code()}")
            } catch (e: Exception) {
                Resultado.Error(TipoError.DESCONOCIDO, e.message)
            }
        }
}
