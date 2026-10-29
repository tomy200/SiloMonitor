package com.example.silomonitor.model

import com.example.silomonitor.data.local.entity.AlertaDetalle

enum class FiltroAlertas(val etiqueta: String) {
    ACTIVAS("Activas"),
    ATENDIDAS("Atendidas"),
    TODAS("Todas")
}

/** Estado de la pantalla de alertas. */
data class AlertasUiState(
    val cargando: Boolean = true,
    val alertas: List<AlertaDetalle> = emptyList(),
    val filtro: FiltroAlertas = FiltroAlertas.ACTIVAS,
    val activas: Int = 0,
    val atendidas: Int = 0,
    val rol: Rol = Rol.OPERARIO
)
