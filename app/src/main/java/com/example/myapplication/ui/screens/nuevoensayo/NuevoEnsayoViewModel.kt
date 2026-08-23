package com.example.myapplication.ui.screens.nuevoensayo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Ensayo
import com.example.myapplication.domain.repository.EnsayoRepository
import com.example.myapplication.domain.repository.Resultado
import com.example.myapplication.domain.repository.RobotRepository
import com.example.myapplication.hardware.GestorUbicacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NuevoEnsayoUiState(
    val titulo: String = "",
    val notas: String = "",
    val uriFoto: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val buscandoUbicacion: Boolean = false,
    val telemetriaCapturada: Boolean = false,
    val pitch: Float = 0f,
    val roll: Float = 0f,
    val inclinacionMaxima: Float = 0f,
    val temperatura: Float = 0f,
    val indiceGas: Int = 0,
    val guardando: Boolean = false,
    val guardado: Boolean = false,
    val aviso: String? = null
) {
    val puedeGuardar: Boolean get() = titulo.isNotBlank() && !guardando
}

class NuevoEnsayoViewModel(
    private val ensayos: EnsayoRepository,
    private val robot: RobotRepository,
    private val ubicacion: GestorUbicacion
) : ViewModel() {

    private val _uiState = MutableStateFlow(NuevoEnsayoUiState())
    val uiState: StateFlow<NuevoEnsayoUiState> = _uiState.asStateFlow()

    init {
        capturarTelemetria()
    }

    /** Toma una foto instantanea del estado del robot al cerrar el ensayo. */
    private fun capturarTelemetria() {
        viewModelScope.launch {
            when (val r = robot.obtenerTelemetria()) {
                is Resultado.Exito -> {
                    val t = r.datos
                    _uiState.value = _uiState.value.copy(
                        telemetriaCapturada = true,
                        pitch = t.pitch,
                        roll = t.roll,
                        inclinacionMaxima = t.inclinacionTotal,
                        temperatura = t.temperaturaSensor,
                        indiceGas = t.indiceGas
                    )
                }
                is Resultado.Error -> {
                    // El ensayo se puede registrar igual, sin telemetria.
                    _uiState.value = _uiState.value.copy(
                        telemetriaCapturada = false,
                        aviso = "Sin conexion con el robot: se guardara sin telemetria"
                    )
                }
            }
        }
    }

    fun onTitulo(v: String) { _uiState.value = _uiState.value.copy(titulo = v) }
    fun onNotas(v: String)  { _uiState.value = _uiState.value.copy(notas = v) }

    fun onFotoTomada(uri: String) {
        _uiState.value = _uiState.value.copy(uriFoto = uri)
    }

    /**
     * Pide la ubicacion. Si el permiso fue denegado o no hay
     * senal, se avisa pero no se bloquea el guardado.
     */
    fun obtenerUbicacion() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(buscandoUbicacion = true)
            val u = ubicacion.obtenerUbicacion()
            _uiState.value = if (u != null) {
                _uiState.value.copy(
                    latitud = u.latitud,
                    longitud = u.longitud,
                    buscandoUbicacion = false,
                    aviso = null
                )
            } else {
                _uiState.value.copy(
                    buscandoUbicacion = false,
                    aviso = "No se pudo obtener la ubicacion. El ensayo se " +
                            "guardara sin coordenadas."
                )
            }
        }
    }

    fun guardar() {
        val s = _uiState.value
        if (!s.puedeGuardar) return

        viewModelScope.launch {
            _uiState.value = s.copy(guardando = true)

            ensayos.guardar(
                Ensayo(
                    titulo = s.titulo.trim(),
                    notas = s.notas.trim(),
                    fechaHora = System.currentTimeMillis(),
                    pitch = s.pitch,
                    roll = s.roll,
                    inclinacionMaxima = s.inclinacionMaxima,
                    temperatura = s.temperatura,
                    indiceGas = s.indiceGas,
                    uriFoto = s.uriFoto,
                    latitud = s.latitud,
                    longitud = s.longitud
                )
            )

            _uiState.value = _uiState.value.copy(guardando = false, guardado = true)
        }
    }

    companion object {
        fun fabrica(
            ensayos: EnsayoRepository,
            robot: RobotRepository,
            ubicacion: GestorUbicacion
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                NuevoEnsayoViewModel(ensayos, robot, ubicacion) as T
        }
    }
}
