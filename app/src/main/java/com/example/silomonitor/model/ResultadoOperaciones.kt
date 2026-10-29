package com.example.silomonitor.model

/** Punto del gráfico de historial de nivel. */
data class PuntoNivel(val fecha: Long, val porcentaje: Float)

/** Resultado de registrar un movimiento: permite informar si cambió el estado del silo. */
data class ResultadoMovimiento(
    val estadoAnterior: EstadoSilo,
    val estadoNuevo: EstadoSilo,
    val porcentajeNuevo: Double,
    val alertaGenerada: Boolean
)

/** Resultado de sincronizar con la API simulada del sistema de monitoreo. */
data class ResultadoSincronizacion(
    val silosActualizados: Int,
    val alertasNuevas: Int
)
