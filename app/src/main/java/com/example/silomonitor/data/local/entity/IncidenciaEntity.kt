package com.example.silomonitor.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Incidencia reportada en terreno, opcionalmente con foto de evidencia. */
@Entity(
    tableName = "incidencias",
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
data class IncidenciaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val siloId: String,
    val tipo: String,
    val prioridad: String,
    val descripcion: String,
    val fotoUri: String?,
    val fecha: Long,
    val usuario: String
)
