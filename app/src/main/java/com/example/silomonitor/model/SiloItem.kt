package com.example.silomonitor.model

/**
 * Modelo de presentación de un silo: une los datos guardados en Room
 * con los valores calculados (porcentaje, estado y días restantes).
 */
data class SiloItem(
    val id: String,
    val nombre: String,
    val granjaId: Int,
    val granjaNombre: String,
    val tipoAlimento: String,
    val capacidadKg: Double,
    val nivelKg: Double,
    val consumoDiarioKg: Double,
    val ultimaActualizacion: Long,
    val porcentaje: Double,
    val estado: EstadoSilo,
    val diasRestantes: Int?
) {
    val capacidadLibreKg: Double get() = (capacidadKg - nivelKg).coerceAtLeast(0.0)
}
