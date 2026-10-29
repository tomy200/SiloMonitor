package com.example.silomonitor.model

/** Tipos de movimiento de alimento que se registran sobre un silo. */
enum class TipoMovimiento(val etiqueta: String, val descripcion: String) {
    CARGA("Carga", "Ingreso de alimento al silo (reposición)"),
    DESCARGA("Descarga", "Retiro de alimento: traslado, merma o devolución"),
    CONSUMO("Consumo", "Alimento entregado a los animales");

    /** true si el movimiento aumenta el nivel del silo. */
    val sumaAlNivel: Boolean
        get() = this == CARGA

    companion object {
        fun desde(valor: String?): TipoMovimiento? = entries.firstOrNull { it.name == valor }
    }
}
