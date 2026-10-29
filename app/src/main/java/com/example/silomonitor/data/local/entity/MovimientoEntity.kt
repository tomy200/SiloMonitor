package com.example.silomonitor.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Movimiento de alimento (carga, descarga o consumo).
 * [nivelResultanteKg] guarda el nivel del silo después del movimiento,
 * lo que permite construir el historial de niveles sin otra tabla.
 */
@Entity(
    tableName = "movimientos",
    foreignKeys = [
        ForeignKey(
            entity = SiloEntity::class,
            parentColumns = ["id"],
            childColumns = ["siloId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("siloId")]
)
data class MovimientoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val siloId: String,
    val tipo: String,
    val cantidadKg: Double,
    val nivelResultanteKg: Double,
    val fecha: Long,
    val observacion: String,
    val usuario: String
)
