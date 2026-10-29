package com.example.silomonitor.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Usuario pre-registrado por el administrador (no hay registro público, según el caso).
 * La contraseña se guarda como hash SHA-256, nunca en texto plano.
 */
@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey val correo: String,
    val nombre: String,
    val rol: String,
    val claveHash: String,
    val fotoUri: String? = null
)
