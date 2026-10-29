package com.example.silomonitor.model

/**
 * Estados operativos de un silo según su porcentaje de llenado.
 * Umbrales definidos en el caso: Normal > 30 %, Advertencia 20–30 %, Crítico < 20 %.
 *
 * [gravedad] permite comparar estados (por ejemplo, saber si un silo "empeoró").
 */
enum class EstadoSilo(val etiqueta: String, val descripcion: String, val gravedad: Int) {
    NORMAL("Normal", "Sobre 30 % de su capacidad", 0),
    ADVERTENCIA("Advertencia", "Entre 20 % y 30 % de su capacidad", 1),
    CRITICO("Crítico", "Bajo 20 % de su capacidad", 2);

    companion object {
        fun desde(valor: String?): EstadoSilo = entries.firstOrNull { it.name == valor } ?: NORMAL
    }
}
