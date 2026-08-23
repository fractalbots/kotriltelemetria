package com.example.myapplication.ui.screens.telemetria

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.prefs.PreferenciasRepository
import com.example.myapplication.domain.model.Preferencias
import com.example.myapplication.domain.model.Telemetria
import com.example.myapplication.domain.repository.Resultado
import com.example.myapplication.domain.repository.RobotRepository
import com.example.myapplication.domain.repository.TipoError
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Los tres estados que exige la rubrica, como tipos y no como
 * banderas sueltas. Con un sealed interface el compilador obliga
 * a cubrir todos los casos en la UI.
 */
sealed interface TelemetriaUiState {
    data object Cargando : TelemetriaUiState
    data class Exito(
        val datos: Telemetria,
        val actualizando: Boolean = false
    ) : TelemetriaUiState
    data class Error(
        val tipo: TipoError,
        val detalle: String?
    ) : TelemetriaUiState
}

class TelemetriaViewModel(
    private val repository: RobotRepository,
    preferenciasRepo: PreferenciasRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TelemetriaUiState>(TelemetriaUiState.Cargando)
    val uiState: StateFlow<TelemetriaUiState> = _uiState.asStateFlow()

    /**
     * Preferencias del usuario.
     *
     * La pantalla las necesita para dos cosas concretas:
     *  - convertir la temperatura a Fahrenheit si asi se eligio
     *  - marcar en rojo el gas cuando supera el umbral configurado
     *
     * Una preferencia que se guarda pero no afecta a nada no
     * sirve para nada.
     */
    val preferencias: StateFlow<Preferencias> = preferenciasRepo.preferencias
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = Preferencias()
        )

    private var trabajoSondeo: Job? = null

    init { iniciarSondeo() }

    /**
     * Sondeo periodico.
     *
     * 500 ms es un compromiso: mas rapido satura al ESP32 (es un
     * microcontrolador, no un servidor) y mas lento hace que los
     * angulos se vean a saltos.
     */
    fun iniciarSondeo() {
        trabajoSondeo?.cancel()
        trabajoSondeo = viewModelScope.launch {
            while (isActive) {
                consultar()
                delay(INTERVALO_MS)
            }
        }
    }

    fun detenerSondeo() {
        trabajoSondeo?.cancel()
        trabajoSondeo = null
    }

    fun reintentar() {
        _uiState.value = TelemetriaUiState.Cargando
        iniciarSondeo()
    }

    fun reiniciarOrientacion() {
        viewModelScope.launch { repository.reiniciarOrientacion() }
    }

    private suspend fun consultar() {
        // Si ya hay datos, marcamos "actualizando" en vez de volver
        // a Cargando: asi la pantalla no parpadea en cada ciclo.
        val actual = _uiState.value
        if (actual is TelemetriaUiState.Exito) {
            _uiState.value = actual.copy(actualizando = true)
        }

        _uiState.value = when (val r = repository.obtenerTelemetria()) {
            is Resultado.Exito -> TelemetriaUiState.Exito(r.datos)
            is Resultado.Error -> TelemetriaUiState.Error(r.tipo, r.detalle)
        }
    }

    override fun onCleared() {
        super.onCleared()
        detenerSondeo()
    }

    companion object {
        private const val INTERVALO_MS = 500L

        fun fabrica(
            repository: RobotRepository,
            preferencias: PreferenciasRepository
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                TelemetriaViewModel(repository, preferencias) as T
        }
    }
}
