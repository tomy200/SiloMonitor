package com.example.silomonitor.model

/** Estado del formulario "Reportar incidencia". */
data class IncidenciaUiState(
    val silo: SiloItem? = null,
    val rol: Rol? = null,
    val tipo: TipoIncidencia? = null,
    val prioridad: Prioridad = Prioridad.MEDIA,
    val descripcion: String = "",
    val fotoUri: String? = null,
    val procesandoFoto: Boolean = false,
    val errores: IncidenciaErrores = IncidenciaErrores(),
    val guardando: Boolean = false,
    val guardadoExitoso: Boolean = false,
    val intentosFallidos: Int = 0
)

data class IncidenciaErrores(
    val tipo: String? = null,
    val descripcion: String? = null,
    val foto: String? = null
) {
    val tieneErrores: Boolean get() = tipo != null || descripcion != null || foto != null
}
