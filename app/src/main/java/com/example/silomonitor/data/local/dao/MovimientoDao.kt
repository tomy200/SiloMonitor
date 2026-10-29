package com.example.silomonitor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.silomonitor.data.local.entity.MovimientoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovimientoDao {

    @Query("SELECT * FROM movimientos WHERE siloId = :siloId ORDER BY fecha DESC")
    fun observarPorSilo(siloId: String): Flow<List<MovimientoEntity>>

    @Query("SELECT * FROM movimientos WHERE siloId = :siloId AND fecha >= :desde ORDER BY fecha ASC")
    suspend fun obtenerDesde(siloId: String, desde: Long): List<MovimientoEntity>

    @Insert
    suspend fun insertar(movimiento: MovimientoEntity): Long

    @Insert
    suspend fun insertarTodos(movimientos: List<MovimientoEntity>)
}
