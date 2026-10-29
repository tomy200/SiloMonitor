package com.example.silomonitor.model

/** Estado del formulario de registro de una cuenta nueva. */
data class RegistroUiState(
    val nombre: String = "",
    val correo: String = "",
    val clave: String = "",
    val confirmarClave: String = "",
    /** Cargo en que se desempeñará el usuario (operario, supervisor o jefatura). */
    val rol: Rol? = null,
    val mostrarClave: Boolean = false,
    val cargando: Boolean = false,
    val errores: RegistroErrores = RegistroErrores(),
    val errorGeneral: String? = null,
    /** Aumenta con cada intento inválido: la pantalla lo usa para animar una sacudida. */
    val intentosFallidos: Int = 0
)

/** Errores por campo del registro: null significa que el campo es válido. */
data class RegistroErrores(
    val nombre: String? = null,
    val correo: String? = null,
    val clave: String? = null,
    val confirmarClave: String? = null,
    val rol: String? = null
) {
    val tieneErrores: Boolean
        get() = nombre != null || correo != null || clave != null || confirmarClave != null || rol != null
}
