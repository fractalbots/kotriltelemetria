package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Acceso a la tabla de ensayos.
 *
 * Las consultas devuelven Flow: Room notifica sola cuando la
 * tabla cambia y la UI se actualiza sin que nadie tenga que
 * pedir un refresco manual.
 */
@Dao
interface EnsayoDao {

    @Query("SELECT * FROM ensayos ORDER BY fecha_hora DESC")
    fun observarTodos(): Flow<List<EnsayoEntity>>

    @Query("SELECT * FROM ensayos WHERE id = :id")
    suspend fun obtenerPorId(id: Long): EnsayoEntity?

    @Insert
    suspend fun insertar(ensayo: EnsayoEntity): Long

    @Delete
    suspend fun eliminar(ensayo: EnsayoEntity)

    @Query("SELECT COUNT(*) FROM ensayos")
    fun contar(): Flow<Int>
}
