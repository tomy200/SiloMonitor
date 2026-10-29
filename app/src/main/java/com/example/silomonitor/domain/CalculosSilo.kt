package com.example.silomonitor.domain

import com.example.silomonitor.data.local.entity.MovimientoEntity
import com.example.silomonitor.model.EstadoSilo
import com.example.silomonitor.model.TipoMovimiento
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * Reglas de negocio del monitoreo de silos, separadas de la interfaz
 * para poder probarlas con pruebas unitarias (ver app/src/test).
 */
object CalculosSilo {

    const val UMBRAL_ADVERTENCIA = 30.0
    const val UMBRAL_CRITICO = 20.0
    const val DIAS_PROMEDIO_CONSUMO = 7
    private const val MILIS_DIA = 86_400_000L

    /** Porcentaje de llenado entre 0 y 100. */
    fun porcentaje(nivelKg: Double, capacidadKg: Double): Double =
        if (capacidadKg <= 0.0) 0.0 else (nivelKg / capacidadKg * 100.0).coerceIn(0.0, 100.0)

    /** Normal > 30 %, Advertencia entre 20 % y 30 %, Crítico < 20 %. */
    fun estado(porcentaje: Double): EstadoSilo = when {
        porcentaje < UMBRAL_CRITICO -> EstadoSilo.CRITICO
        porcentaje <= UMBRAL_ADVERTENCIA -> EstadoSilo.ADVERTENCIA
        else -> EstadoSilo.NORMAL
    }

    /** Nivel del silo después de aplicar un movimiento. */
    fun nivelResultante(nivelActualKg: Double, tipo: TipoMovimiento, cantidadKg: Double): Double =
        if (tipo.sumaAlNivel) nivelActualKg + cantidadKg else nivelActualKg - cantidadKg

    /** Promedio diario de salidas (consumos + descargas) de los últimos [dias] días. */
    fun consumoPromedioDiario(
        movimientos: List<MovimientoEntity>,
        ahora: Long,
        dias: Int = DIAS_PROMEDIO_CONSUMO
    ): Double {
        if (dias <= 0) return 0.0
        val desde = ahora - dias * MILIS_DIA
        val totalSalidas = movimientos
            .filter { it.fecha >= desde && TipoMovimiento.desde(it.tipo)?.sumaAlNivel == false }
            .sumOf { it.cantidadKg }
        return totalSalidas / dias
    }

    /** Días que alcanza el alimento con el consumo actual. null si no hay consumo registrado. */
    fun diasRestantes(nivelKg: Double, consumoDiarioKg: Double): Int? =
        if (consumoDiarioKg <= 0.0) null else floor(nivelKg / consumoDiarioKg).toInt()

    fun mensajeAlerta(siloNombre: String, granjaNombre: String, estado: EstadoSilo, porcentaje: Double): String =
        when (estado) {
            EstadoSilo.CRITICO ->
                "$siloNombre ($granjaNombre) está en nivel crítico: ${porcentaje.roundToInt()} % de capacidad. Requiere reposición inmediata."
            EstadoSilo.ADVERTENCIA ->
                "$siloNombre ($granjaNombre) bajó a ${porcentaje.roundToInt()} % de capacidad. Planifica su reposición."
            EstadoSilo.NORMAL ->
                "$siloNombre ($granjaNombre) volvió a nivel normal."
        }
}
