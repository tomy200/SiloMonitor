package com.example.silomonitor.data.local.entity

import androidx.room.Embedded

/** Resultado de un JOIN silo + nombre de su granja. */
data class SiloConGranja(
    @Embedded val silo: SiloEntity,
    val granjaNombre: String
)

/** Resultado de un JOIN alerta + nombres de silo y granja. */
data class AlertaDetalle(
    @Embedded val alerta: AlertaEntity,
    val siloNombre: String,
    val granjaNombre: String
)
