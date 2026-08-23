package com.example.myapplication.ui.screens.ajustes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.prefs.PreferenciasRepository
import com.example.myapplication.domain.model.Preferencias
import com.example.myapplication.domain.repository.Resultado
import com.example.myapplication.domain.repository.RobotRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface EstadoPrueba {
    data object Inactivo : EstadoPrueba
    data object Probando : EstadoPrueba
    data class Exito(val mensaje: String) : EstadoPrueba
    data class Fallo(val mensaje: String) : EstadoPrueba
}

class AjustesViewModel(
    private val preferencias: PreferenciasRepository,
    private val robot: RobotRepository
) : ViewModel() {

    /**
     * El Flow de DataStore se convierte en StateFlow para que la
     * UI siempre tenga un valor disponible y no parpadee al rotar
     * la pantalla.
     */
    val preferenciasState: StateFlow<Preferencias> =
        preferencias.preferencias.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = Preferencias()
        )

    private val _estadoPrueba = MutableStateFlow<EstadoPrueba>(EstadoPrueba.Inactivo)
    val estadoPrueba: StateFlow<EstadoPrueba> = _estadoPrueba.asStateFlow()

    fun guardarIp(ip: String) {
        viewModelScope.launch { preferencias.guardarIp(ip) }
    }

    fun guardarVelocidad(v: Float) {
        viewModelScope.launch { preferencias.guardarVelocidadMaxima(v) }
    }

    fun guardarModoOscuro(activo: Boolean) {
        viewModelScope.launch { preferencias.guardarModoOscuro(activo) }
    }

    fun guardarFahrenheit(activo: Boolean) {
        viewModelScope.launch { preferencias.guardarUsarFahrenheit(activo) }
    }

    fun guardarUmbralGas(valor: Int) {
        viewModelScope.launch { preferencias.guardarUmbralGas(valor) }
    }

    /** Comprueba que la IP guardada responda de verdad. */
    fun probarConexion() {
        viewModelScope.launch {
            _estadoPrueba.value = EstadoPrueba.Probando
            _estadoPrueba.value = when (val r = robot.obtenerTelemetria()) {
                is Resultado.Exito -> EstadoPrueba.Exito(
                    if (r.datos.sensorOk) "Robot conectado y sensor operativo"
                    else "Robot conectado, pero el MPU6050 no responde"
                )
                is Resultado.Error -> EstadoPrueba.Fallo(r.tipo.mensaje)
            }
        }
    }

    companion object {
        fun fabrica(
            preferencias: PreferenciasRepository,
            robot: RobotRepository
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                AjustesViewModel(preferencias, robot) as T
        }
    }
}
