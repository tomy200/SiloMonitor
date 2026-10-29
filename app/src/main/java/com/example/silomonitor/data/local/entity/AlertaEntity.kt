package com.example.silomonitor.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Alerta generada cuando un silo baja a nivel de advertencia o crítico. */
@Entity(
    tableName = "alertas",
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
data class AlertaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val siloId: String,
    val nivel: String,
    val porcentaje: Double,
    val mensaje: String,
    val fecha: Long,
    val atendida: Boolean = false,
    val atendidaPor: String? = null,
    val fechaAtencion: Long? = null
)
