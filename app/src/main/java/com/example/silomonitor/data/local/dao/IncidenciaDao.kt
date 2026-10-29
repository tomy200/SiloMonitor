package com.example.silomonitor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.silomonitor.data.local.entity.IncidenciaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidenciaDao {

    @Query("SELECT * FROM incidencias WHERE siloId = :siloId ORDER BY fecha DESC")
    fun observarPorSilo(siloId: String): Flow<List<IncidenciaEntity>>

    @Insert
    suspend fun insertar(incidencia: IncidenciaEntity): Long
}
