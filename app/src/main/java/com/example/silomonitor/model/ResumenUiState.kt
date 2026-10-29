package com.example.silomonitor.model

import com.example.silomonitor.domain.CalculosSilo

/** Resumen de una granja para el panel de jefaturas y supervisores. */
data class ResumenGranja(
    val id: Int,
    val nombre: String,
    val ubicacion: String,
    val totalSilos: Int,
    val porcentaje: Double,
    val peorEstado: EstadoSilo,
    val criticos: Int,
    val advertencias: Int
)

/** Estado de la pantalla Resumen (indicadores generales). */
data class ResumenUiState(
    val cargando: Boolean = true,
    val nombreUsuario: String = "",
    val rol: Rol = Rol.OPERARIO,
    val totalSilos: Int = 0,
    val normales: Int = 0,
    val advertencias: Int = 0,
    val criticos: Int = 0,
    val disponibleKg: Double = 0.0,
    val capacidadKg: Double = 0.0,
    val granjas: List<ResumenGranja> = emptyList(),
    val prioridadReposicion: List<SiloItem> = emptyList(),
    val ultimaSincronizacion: Long? = null,
    val sincronizando: Boolean = false
) {
    val porcentajeGlobal: Double get() = CalculosSilo.porcentaje(disponibleKg, capacidadKg)
}
