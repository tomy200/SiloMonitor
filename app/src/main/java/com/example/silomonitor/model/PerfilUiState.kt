package com.example.silomonitor.model

/** Estado de la pantalla de perfil y preferencias. */
data class PerfilUiState(
    val cargando: Boolean = true,
    val nombre: String = "",
    val correo: String = "",
    val rol: Rol = Rol.OPERARIO,
    val fotoUri: String? = null,
    /** null = sigue el tema del sistema. */
    val temaOscuro: Boolean? = null,
    val notificaciones: Boolean = true,
    val vibracion: Boolean = true,
    val simularSinConexion: Boolean = false
)
