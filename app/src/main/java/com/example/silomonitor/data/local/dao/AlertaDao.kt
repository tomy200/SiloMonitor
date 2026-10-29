package com.example.silomonitor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.silomonitor.data.local.entity.AlertaDetalle
import com.example.silomonitor.data.local.entity.AlertaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertaDao {

    /** Alertas con nombre de silo y granja: primero las activas y luego las más recientes. */
    @Query(
        """
        SELECT a.*, s.nombre AS siloNombre, g.nombre AS granjaNombre
        FROM alertas a
        INNER JOIN silos s ON a.siloId = s.id
        INNER JOIN granjas g ON s.granjaId = g.id
        ORDER BY a.atendida ASC, a.fecha DESC
        """
    )
    fun observarDetalle(): Flow<List<AlertaDetalle>>

    @Query("SELECT COUNT(*) FROM alertas WHERE atendida = 0")
    fun observarCantidadActivas(): Flow<Int>

    @Insert
    suspend fun insertar(alerta: AlertaEntity): Long

    @Insert
    suspend fun insertarTodas(alertas: List<AlertaEntity>)

    @Query("UPDATE alertas SET atendida = 1, atendidaPor = :usuario, fechaAtencion = :fecha WHERE id = :id")
    suspend fun atender(id: Long, usuario: String, fecha: Long)

    @Query("UPDATE alertas SET atendida = 1, atendidaPor = :usuario, fechaAtencion = :fecha WHERE siloId = :siloId AND atendida = 0")
    suspend fun atenderPorSilo(siloId: String, usuario: String, fecha: Long)
}
