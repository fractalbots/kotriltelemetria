package com.example.myapplication.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Construye el cliente HTTP.
 *
 * Los timeouts son CORTOS a proposito. Contra un servidor de
 * internet uno esperaria 30 s, pero el ESP32 esta en la red local
 * a pocos metros: si no contesta en 2 segundos es que esta
 * apagado o fuera de alcance, y la UI debe avisar de inmediato en
 * lugar de dejar al usuario mirando un spinner.
 */
object RetrofitFactory {

    private const val TIMEOUT_SEGUNDOS = 2L

    fun crear(habilitarLogs: Boolean = true): RobotApi {

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val logging = HttpLoggingInterceptor().apply {
            level = if (habilitarLogs) HttpLoggingInterceptor.Level.BASIC
            else HttpLoggingInterceptor.Level.NONE
        }

        val cliente = OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            // URL base ficticia: las llamadas usan @Url completa.
            // Retrofit exige una base aunque no se use.
            .baseUrl("http://localhost/")
            .client(cliente)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(RobotApi::class.java)
    }
}
