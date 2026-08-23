package com.example.myapplication.data.repository

import com.example.myapplication.data.local.EnsayoDao
import com.example.myapplication.data.local.EnsayoEntity
import com.example.myapplication.domain.model.Ensayo
import com.example.myapplication.domain.repository.EnsayoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Repositorio de ensayos.
 *
 * Traduce entre la entidad de Room y el modelo de dominio, de
 * modo que ni el ViewModel ni la UI conocen las anotaciones de
 * persistencia.
 */
class EnsayoRepositoryImpl(
    private val dao: EnsayoDao
) : EnsayoRepository {

    override fun observarEnsayos(): Flow<List<Ensayo>> =
        dao.observarTodos()
            .map { lista -> lista.map { it.aDominio() } }
            .flowOn(Dispatchers.IO)

    override suspend fun guardar(ensayo: Ensayo): Long =
        withContext(Dispatchers.IO) { dao.insertar(ensayo.aEntidad()) }

    override suspend fun eliminar(ensayo: Ensayo) =
        withContext(Dispatchers.IO) { dao.eliminar(ensayo.aEntidad()) }

    override fun contar(): Flow<Int> = dao.contar().flowOn(Dispatchers.IO)
}

/* ─────────── Mappers ─────────── */

private fun EnsayoEntity.aDominio() = Ensayo(
    id = id,
    titulo = titulo,
    notas = notas,
    fechaHora = fechaHora,
    pitch = pitch,
    roll = roll,
    inclinacionMaxima = inclinacionMaxima,
    temperatura = temperatura,
    indiceGas = indiceGas,
    uriFoto = uriFoto,
    latitud = latitud,
    longitud = longitud
)

private fun Ensayo.aEntidad() = EnsayoEntity(
    id = id,
    titulo = titulo,
    notas = notas,
    fechaHora = fechaHora,
    pitch = pitch,
    roll = roll,
    inclinacionMaxima = inclinacionMaxima,
    temperatura = temperatura,
    indiceGas = indiceGas,
    uriFoto = uriFoto,
    latitud = latitud,
    longitud = longitud
)
