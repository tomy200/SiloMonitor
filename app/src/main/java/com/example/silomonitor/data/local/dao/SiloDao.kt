package com.example.silomonitor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.silomonitor.data.local.entity.SiloConGranja
import com.example.silomonitor.data.local.entity.SiloEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SiloDao {

    @Query("SELECT COUNT(*) FROM silos")
    suspend fun contar(): Int

    /** Flow reactivo: la UI se actualiza sola cada vez que cambia un silo. */
    @Query(
        """
        SELECT s.*, g.nombre AS granjaNombre
        FROM silos s INNER JOIN granjas g ON s.granjaId = g.id
        ORDER BY s.granjaId, s.id
        """
    )
    fun observarSilosConGranja(): Flow<List<SiloConGranja>>

    @Query(
        """
        SELECT s.*, g.nombre AS granjaNombre
        FROM silos s INNER JOIN granjas g ON s.granjaId = g.id
        WHERE s.id = :id
        """
    )
    fun observarSiloConGranja(id: String): Flow<SiloConGranja?>

    @Query("SELECT * FROM silos WHERE id = :id")
    suspend fun obtener(id: String): SiloEntity?

    @Query("SELECT * FROM silos ORDER BY granjaId, id")
    suspend fun obtenerTodos(): List<SiloEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(silos: List<SiloEntity>)

    @Update
    suspend fun actualizar(silo: SiloEntity)
}
