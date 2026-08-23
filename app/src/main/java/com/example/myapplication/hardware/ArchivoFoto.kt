package com.example.myapplication.hardware

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Gestion del archivo de foto para la camara.
 *
 * La camara del sistema necesita un Uri donde escribir la imagen.
 * No se le puede pasar una ruta directa: desde Android 7 eso
 * lanza FileUriExposedException. Hay que usar un FileProvider.
 *
 * CRITICO: la carpeta "fotos" debe coincidir EXACTAMENTE con lo
 * declarado en res/xml/file_paths.xml:
 *     <files-path name="fotos_ensayos" path="fotos/" />
 * Si no coincide, la app revienta con IllegalArgumentException y
 * el mensaje no ayuda nada a encontrar la causa.
 */
object ArchivoFoto {

    private const val CARPETA = "fotos"

    fun crearArchivo(context: Context): File {
        val dir = File(context.filesDir, CARPETA)
        if (!dir.exists()) dir.mkdirs()

        val marca = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
            .format(Date())
        return File(dir, "ensayo_$marca.jpg")
    }

    fun uriPara(context: Context, archivo: File): Uri =
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            archivo
        )
}
