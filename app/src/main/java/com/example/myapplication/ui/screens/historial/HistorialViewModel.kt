package com.example.myapplication.ui.screens.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.Ensayo
import com.example.myapplication.domain.repository.EnsayoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface HistorialUiState {
    data object Cargando : HistorialUiState
    data object Vacio : HistorialUiState
    data class ConDatos(val ensayos: List<Ensayo>) : HistorialUiState
}

class HistorialViewModel(
    private val repository: EnsayoRepository
) : ViewModel() {

    val uiState: StateFlow<HistorialUiState> =
        repository.observarEnsayos()
            .map { lista ->
                if (lista.isEmpty()) HistorialUiState.Vacio
                else HistorialUiState.ConDatos(lista)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HistorialUiState.Cargando
            )

    fun eliminar(ensayo: Ensayo) {
        viewModelScope.launch { repository.eliminar(ensayo) }
    }

    companion object {
        fun fabrica(repository: EnsayoRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                HistorialViewModel(repository) as T
        }
    }
}
