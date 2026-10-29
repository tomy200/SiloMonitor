package com.example.silomonitor.model

/** Tipos de incidencia que un operario puede reportar desde terreno. */
enum class TipoIncidencia(val etiqueta: String) {
    FALLA_SENSOR("Falla del sensor de nivel"),
    FUGA("Fuga o derrame de alimento"),
    HUMEDAD("Alimento húmedo o contaminado"),
    PLAGA("Presencia de plagas"),
    ESTRUCTURA("Daño en la estructura del silo"),
    OTRO("Otro");

    companion object {
        fun desde(valor: String?): TipoIncidencia = entries.firstOrNull { it.name == valor } ?: OTRO
    }
}

/** Prioridad de la incidencia. Las de prioridad ALTA exigen foto como evidencia. */
enum class Prioridad(val etiqueta: String) {
    BAJA("Baja"),
    MEDIA("Media"),
    ALTA("Alta");

    companion object {
        fun desde(valor: String?): Prioridad = entries.firstOrNull { it.name == valor } ?: MEDIA
    }
}
