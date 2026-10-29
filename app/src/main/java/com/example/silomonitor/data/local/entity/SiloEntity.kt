package com.example.silomonitor.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tabla de silos. El porcentaje y el estado NO se guardan: se calculan
 * a partir de [nivelKg] y [capacidadKg] para que nunca queden desactualizados.
 */
@Entity(
    tableName = "silos",
    foreignKeys = [
        ForeignKey(
            entity = GranjaEntity::class,
            parentColumns = ["id"],
            childColumns = ["granjaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("granjaId")]
)
data class SiloEntity(
    @PrimaryKey val id: String,
    val granjaId: Int,
    val nombre: String,
    val tipoAlimento: String,
    val capacidadKg: Double,
    val nivelKg: Double,
    val consumoDiarioKg: Double,
    val ultimaActualizacion: Long
)
