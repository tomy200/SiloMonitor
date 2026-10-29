package com.example.silomonitor.model

/** Estado del formulario de inicio de sesión (Guía 11: data class de estado + errores). */
data class LoginUiState(
    val correo: String = "",
    val clave: String = "",
    val recordar: Boolean = true,
    val mostrarClave: Boolean = false,
    val cargando: Boolean = false,
    val errores: LoginErrores = LoginErrores(),
    val errorGeneral: String? = null,
    /** Aumenta con cada intento inválido: la pantalla lo usa para animar una sacudida. */
    val intentosFallidos: Int = 0
)

/** Errores por campo: null significa que el campo es válido. */
data class LoginErrores(
    val correo: String? = null,
    val clave: String? = null
) {
    val tieneErrores: Boolean get() = correo != null || clave != null
}
