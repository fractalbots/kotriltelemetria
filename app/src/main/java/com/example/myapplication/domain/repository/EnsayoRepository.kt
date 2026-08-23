package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.Ensayo
import kotlinx.coroutines.flow.Flow

interface EnsayoRepository {

    fun observarEnsayos(): Flow<List<Ensayo>>

    suspend fun guardar(ensayo: Ensayo): Long

    suspend fun eliminar(ensayo: Ensayo)

    fun contar(): Flow<Int>
}
