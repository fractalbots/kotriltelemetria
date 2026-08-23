package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.Telemetria

/**
 * Contrato del repositorio, declarado en DOMINIO.
 *
 * La implementacion vive en data/. El ViewModel depende de esta
 * interfaz, no de la clase concreta. Eso es lo que permite el
 * "modo demo": basta con otra implementacion que devuelva datos
 * simulados y la UI no se entera de nada.
 *
 * Este es el punto que hay que saber explicar en la sustentacion.
 */
interface RobotRepository {

    suspend fun obtenerTelemetria(): Resultado<Telemetria>

    suspend fun mover(vx: Float, vy: Float, w: Float): Resultado<Unit>

    suspend fun detener(): Resultado<Unit>

    suspend fun reiniciarOrientacion(): Resultado<Unit>
}

/**
 * Envoltorio de resultado.
 *
 * Evita lanzar excepciones a traves de las capas: el error viaja
 * como un valor mas y la UI decide como mostrarlo.
 */
sealed interface Resultado<out T> {
    data class Exito<T>(val datos: T) : Resultado<T>
    data class Error(val tipo: TipoError, val detalle: String? = null) : Resultado<Nothing>
}

enum class TipoError {
    SIN_CONEXION,       // no hay red o el robot esta apagado
    TIEMPO_AGOTADO,     // el robot no respondio a tiempo
    RESPUESTA_INVALIDA, // llego algo que no se pudo interpretar
    DESCONOCIDO;

    val mensaje: String
        get() = when (this) {
            SIN_CONEXION       -> "No se encuentra el robot. Revisa que este encendido y en la misma red."
            TIEMPO_AGOTADO     -> "El robot no respondio a tiempo."
            RESPUESTA_INVALIDA -> "El robot respondio algo inesperado."
            DESCONOCIDO        -> "Ocurrio un error inesperado."
        }
}
