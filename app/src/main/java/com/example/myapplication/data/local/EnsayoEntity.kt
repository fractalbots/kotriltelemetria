package com.example.myapplication.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Registro de un ensayo del robot.
 *
 * NOTA IMPORTANTE: la foto NO se guarda en la base de datos.
 * Se guarda la RUTA del archivo (uriFoto) y la imagen vive en el
 * almacenamiento interno de la app. Meter imagenes como BLOB
 * hincha la base y degrada el rendimiento; es un error clasico.
 */
@Entity(tableName = "ensayos")
data class EnsayoEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "titulo")
    val titulo: String,

    @ColumnInfo(name = "notas")
    val notas: String,

    @ColumnInfo(name = "fecha_hora")
    val fechaHora: Long,

    // ── Telemetria capturada al cerrar el ensayo ──
    @ColumnInfo(name = "pitch")
    val pitch: Float,

    @ColumnInfo(name = "roll")
    val roll: Float,

    @ColumnInfo(name = "inclinacion_maxima")
    val inclinacionMaxima: Float,

    @ColumnInfo(name = "temperatura")
    val temperatura: Float,

    @ColumnInfo(name = "indice_gas")
    val indiceGas: Int,

    // ── Hardware del celular ──
    @ColumnInfo(name = "uri_foto")
    val uriFoto: String? = null,

    @ColumnInfo(name = "latitud")
    val latitud: Double? = null,

    @ColumnInfo(name = "longitud")
    val longitud: Double? = null
)
