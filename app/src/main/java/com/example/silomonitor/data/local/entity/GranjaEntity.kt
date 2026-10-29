package com.example.silomonitor.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Tabla de granjas. */
@Entity(tableName = "granjas")
data class GranjaEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    val ubicacion: String
)
