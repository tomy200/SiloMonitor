package com.example.silomonitor.model

import com.example.silomonitor.domain.CalculosSilo

/** Estado del formulario "Registrar movimiento". */
data class MovimientoUiState(
    val silo: SiloItem? = null,
    val rol: Rol? = null,
    val tipo: TipoMovimiento = TipoMovimiento.CARGA,
    val cantidad: String = "",
    val observacion: String = "",
    val errores: MovimientoErrores = MovimientoErrores(),
    val guardando: Boolean = false,
    val guardadoExitoso: Boolean = false,
    val mensajeExito: String? = null,
    val errorGeneral: String? = null,
    val intentosFallidos: Int = 0
) {
    val cantidadKg: Double? get() = cantidad.trim().toLongOrNull()?.toDouble()

    val observacionObligatoria: Boolean get() = tipo == TipoMovimiento.DESCARGA

    /** Máximo que se puede cargar (capacidad libre) o retirar (alimento disponible). */
    val maximoPermitidoKg: Double
        get() = silo?.let { if (tipo == TipoMovimiento.CARGA) it.capacidadLibreKg else it.nivelKg } ?: 0.0

    /** Porcentaje en que quedaría el silo si se guarda el movimiento (vista previa). */
    val porcentajeProyectado: Double?
        get() {
            val s = silo ?: return null
            val c = cantidadKg ?: return null
            if (c <= 0.0) return null
            val nivel = CalculosSilo.nivelResultante(s.nivelKg, tipo, c).coerceIn(0.0, s.capacidadKg)
            return CalculosSilo.porcentaje(nivel, s.capacidadKg)
        }
}

data class MovimientoErrores(
    val cantidad: String? = null,
    val observacion: String? = null
) {
    val tieneErrores: Boolean get() = cantidad != null || observacion != null
}
