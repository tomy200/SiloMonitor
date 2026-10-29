package com.example.silomonitor.model

import com.example.silomonitor.data.local.entity.GranjaEntity

/** Estado de la lista de silos con sus filtros. */
data class SilosUiState(
    val cargando: Boolean = true,
    val silos: List<SiloItem> = emptyList(),
    val totalSinFiltro: Int = 0,
    val granjas: List<GranjaEntity> = emptyList(),
    val granjaSeleccionada: Int? = null,
    val estadoSeleccionado: EstadoSilo? = null,
    val sincronizando: Boolean = false
) {
    val hayFiltros: Boolean get() = granjaSeleccionada != null || estadoSeleccionado != null
}
