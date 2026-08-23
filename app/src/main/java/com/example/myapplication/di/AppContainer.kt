package com.example.myapplication.di

import android.content.Context
import com.example.myapplication.data.local.RobotDatabase
import com.example.myapplication.data.prefs.PreferenciasRepository
import com.example.myapplication.data.remote.RetrofitFactory
import com.example.myapplication.data.repository.EnsayoRepositoryImpl
import com.example.myapplication.data.repository.RobotRepositoryImpl
import com.example.myapplication.domain.model.Preferencias
import com.example.myapplication.domain.repository.EnsayoRepository
import com.example.myapplication.domain.repository.RobotRepository
import com.example.myapplication.hardware.GestorUbicacion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Contenedor de dependencias MANUAL.
 *
 * Hace lo mismo que Hilt: crear cada objeto una sola vez y
 * entregarlo a quien lo necesite. La diferencia es que aqui todo
 * esta a la vista y se puede explicar linea por linea, sin
 * anotaciones que generan codigo por detras.
 *
 * 'by lazy' garantiza una sola instancia, creada solo cuando de
 * verdad se usa.
 */
class AppContainer(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // ── Preferencias (DataStore) ──
    val preferenciasRepository = PreferenciasRepository(context.applicationContext)

    /**
     * Cache de la IP.
     *
     * El repositorio necesita la IP de forma sincrona en cada
     * llamada, pero DataStore la entrega como Flow. Aqui nos
     * suscribimos una vez y mantenemos el ultimo valor en
     * memoria: cuando el usuario cambia la IP en Ajustes, la
     * siguiente peticion ya sale con la nueva.
     */
    @Volatile
    private var ipActual: String = Preferencias.IP_POR_DEFECTO

    init {
        preferenciasRepository.preferencias
            .onEach { ipActual = it.ipRobot }
            .launchIn(scope)
    }

    // ── Red (Retrofit) ──
    private val api by lazy { RetrofitFactory.crear() }

    val robotRepository: RobotRepository by lazy {
        RobotRepositoryImpl(api = api, proveedorIp = { ipActual })
    }

    // ── Base de datos (Room) ──
    private val database by lazy { RobotDatabase.obtener(context.applicationContext) }

    val ensayoRepository: EnsayoRepository by lazy {
        EnsayoRepositoryImpl(database.ensayoDao())
    }

    // ── Hardware ──
    val gestorUbicacion by lazy { GestorUbicacion(context.applicationContext) }
}
