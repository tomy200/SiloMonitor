package com.example.silomonitor.model

import com.example.silomonitor.data.local.entity.IncidenciaEntity
import com.example.silomonitor.data.local.entity.MovimientoEntity

/** Estado del detalle de un silo: datos, historial, movimientos e incidencias. */
data class DetalleSiloUiState(
    val cargando: Boolean = true,
    val silo: SiloItem? = null,
    val movimientos: List<MovimientoEntity> = emptyList(),
    val historial: List<PuntoNivel> = emptyList(),
    val incidencias: List<IncidenciaEntity> = emptyList(),
    val rol: Rol = Rol.OPERARIO
)
