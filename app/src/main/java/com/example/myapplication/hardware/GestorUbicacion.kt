package com.example.myapplication.hardware

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class Ubicacion(val latitud: Double, val longitud: Double)

/**
 * Obtiene la ubicacion actual del celular.
 *
 * Devuelve null si el usuario nego el permiso o si no hay senal
 * GPS. El ensayo se guarda igual: perder la ubicacion no debe
 * impedir registrar la prueba.
 *
 * Ese manejo del rechazo es justamente lo que pide el requisito
 * (e) del enunciado: "manejando el caso en que el usuario los
 * rechace".
 */
class GestorUbicacion(private val context: Context) {

    private val cliente = LocationServices.getFusedLocationProviderClient(context)

    fun tienePermiso(): Boolean =
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    suspend fun obtenerUbicacion(): Ubicacion? {
        if (!tienePermiso()) return null

        return suspendCancellableCoroutine { cont ->
            try {
                val solicitud = CurrentLocationRequest.Builder()
                    .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                    .setMaxUpdateAgeMillis(30_000)
                    .build()

                cliente.getCurrentLocation(solicitud, null)
                    .addOnSuccessListener { loc ->
                        cont.resume(
                            loc?.let { Ubicacion(it.latitude, it.longitude) }
                        )
                    }
                    .addOnFailureListener { cont.resume(null) }
            } catch (e: Exception) {
                cont.resume(null)
            }
        }
    }
}
