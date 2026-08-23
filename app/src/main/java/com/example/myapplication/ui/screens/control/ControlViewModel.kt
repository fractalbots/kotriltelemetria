package com.example.myapplication.ui.screens.control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.prefs.PreferenciasRepository
import com.example.myapplication.domain.repository.Resultado
import com.example.myapplication.domain.repository.RobotRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class ComandoMovimiento(
    val vx: Float = 0f,
    val vy: Float = 0f,
    val w: Float = 0f
) {
    val enReposo: Boolean get() = vx == 0f && vy == 0f && w == 0f
}

data class ControlUiState(
    val comando: ComandoMovimiento = ComandoMovimiento(),
    val conectado: Boolean = false,
    val paroActivo: Boolean = false,
    val velocidadMaxima: Float = 0.7f,
    val modoJoystick: Boolean = true,
    val hapticaActiva: Boolean = true,
    val mensajeError: String? = null
)

class ControlViewModel(
    private val repository: RobotRepository,
    private val preferencias: PreferenciasRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ControlUiState())
    val uiState: StateFlow<ControlUiState> = _uiState.asStateFlow()

    /** Ultimo comando que dejo el dedo del usuario. */
    private var comandoPendiente = ComandoMovimiento()

    init {
        viewModelScope.launch {
            preferencias.preferencias.collect { p ->
                _uiState.value = _uiState.value.copy(
                    velocidadMaxima = p.velocidadMaxima,
                    modoJoystick = p.modoJoystick,
                    hapticaActiva = p.hapticaActiva
                )
            }
        }
        iniciarEnvioPeriodico()
    }

    fun alternarModoControl() {
        viewModelScope.launch {
            // Al cambiar de modo, frenar: evita que el robot se
            // quede andando con el ultimo comando del modo anterior.
            comandoPendiente = ComandoMovimiento()
            preferencias.guardarModoJoystick(!_uiState.value.modoJoystick)
        }
    }

    /**
     * El joystick puede disparar decenas de eventos por segundo
     * al arrastrar el dedo. Mandar un POST por cada uno saturaria
     * al ESP32 y lo colgaria.
     *
     * En vez de eso guardamos el ultimo valor y lo enviamos a
     * ritmo fijo de 10 Hz. El robot recibe comandos suaves y la
     * red no se satura.
     *
     * Esta es una de las decisiones tecnicas mas defendibles del
     * proyecto en la sustentacion.
     */
    private fun iniciarEnvioPeriodico() {
        viewModelScope.launch {
            var ultimoEnviado: ComandoMovimiento? = null

            while (isActive) {
                val actual = comandoPendiente

                // En reposo no hace falta insistir: el watchdog del
                // firmware ya frena solo. Basta con avisar una vez.
                val debeEnviar = actual != ultimoEnviado || !actual.enReposo

                if (debeEnviar && !_uiState.value.paroActivo) {
                    val v = _uiState.value.velocidadMaxima
                    when (repository.mover(actual.vx * v, actual.vy * v, actual.w * v)) {
                        is Resultado.Exito -> _uiState.value = _uiState.value.copy(
                            conectado = true, mensajeError = null
                        )
                        is Resultado.Error -> _uiState.value = _uiState.value.copy(
                            conectado = false,
                            mensajeError = "Sin enlace con el robot"
                        )
                    }
                    ultimoEnviado = actual
                }

                delay(INTERVALO_MS)
            }
        }
    }

    /** Joystick de traslacion o pad direccional. */
    fun onTraslacion(x: Float, y: Float) {
        comandoPendiente = comandoPendiente.copy(vx = y, vy = x)
        _uiState.value = _uiState.value.copy(comando = comandoPendiente)
    }

    /** Version del pad: recibe vx / vy ya resueltos. */
    fun onDireccionPad(vx: Float, vy: Float) {
        comandoPendiente = comandoPendiente.copy(vx = vx, vy = vy)
        _uiState.value = _uiState.value.copy(comando = comandoPendiente)
    }

    fun onRotacion(x: Float) {
        comandoPendiente = comandoPendiente.copy(w = x)
        _uiState.value = _uiState.value.copy(comando = comandoPendiente)
    }

    fun paroEmergencia() {
        comandoPendiente = ComandoMovimiento()
        _uiState.value = _uiState.value.copy(
            comando = ComandoMovimiento(),
            paroActivo = true
        )
        viewModelScope.launch { repository.detener() }
    }

    fun liberarParo() {
        _uiState.value = _uiState.value.copy(paroActivo = false)
    }

    fun alternarParo() {
        if (_uiState.value.paroActivo) liberarParo() else paroEmergencia()
    }

    override fun onCleared() {
        super.onCleared()
        comandoPendiente = ComandoMovimiento()
    }

    companion object {
        private const val INTERVALO_MS = 100L   // 10 Hz

        fun fabrica(
            repository: RobotRepository,
            preferencias: PreferenciasRepository
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ControlViewModel(repository, preferencias) as T
        }
    }
}
