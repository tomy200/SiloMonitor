package com.example.silomonitor.model

/**
 * Perfiles de usuario definidos en el caso y los permisos de cada uno.
 * Los permisos se consultan desde la UI y los ViewModels para mostrar u ocultar acciones.
 */
enum class Rol(val etiqueta: String, val descripcion: String) {
    OPERARIO("Operario", "Consulta niveles y registra movimientos de alimento"),
    SUPERVISOR("Supervisor", "Monitorea silos, atiende alertas y coordina reposiciones"),
    JEFATURA("Jefatura", "Consulta resúmenes e históricos para planificar el abastecimiento");

    /** Cargas, descargas y consumos: operarios y supervisores. */
    val puedeRegistrarMovimientos: Boolean
        get() = this == OPERARIO || this == SUPERVISOR

    /** Reporte de incidencias en terreno: operarios y supervisores. */
    val puedeReportarIncidencias: Boolean
        get() = this == OPERARIO || this == SUPERVISOR

    /** Marcar alertas como atendidas: solo supervisores. */
    val puedeAtenderAlertas: Boolean
        get() = this == SUPERVISOR

    companion object {
        fun desde(valor: String?): Rol = entries.firstOrNull { it.name == valor } ?: OPERARIO
    }
}
