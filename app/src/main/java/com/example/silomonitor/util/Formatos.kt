package com.example.silomonitor.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Formatos de números y fechas en español de Chile. */
object Formatos {

    private val localeChile = Locale("es", "CL")
    private val formatoFechaHora = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", localeChile)
    private val formatoFechaCorta = DateTimeFormatter.ofPattern("dd/MM", localeChile)

    /** 12500.0 -> "12.500 kg" */
    fun kg(valor: Double): String = String.format(localeChile, "%,.0f kg", valor)

    /** 128500.0 -> "128,5 t" */
    fun toneladas(valorKg: Double): String = String.format(localeChile, "%,.1f t", valorKg / 1000.0)

    /** 45.2 -> "45 %" */
    fun porcentaje(valor: Double): String = String.format(localeChile, "%.0f %%", valor)

    fun fechaHora(millis: Long): String =
        formatoFechaHora.format(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()))

    fun fechaCorta(millis: Long): String =
        formatoFechaCorta.format(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()))

    fun tiempoRelativo(millis: Long, ahora: Long = System.currentTimeMillis()): String {
        val minutos = (ahora - millis) / 60_000
        return when {
            minutos < 1 -> "hace instantes"
            minutos < 60 -> "hace $minutos min"
            minutos < 1_440 -> "hace ${minutos / 60} h"
            else -> "hace ${minutos / 1_440} d"
        }
    }

    fun diasRestantes(dias: Int?): String = when {
        dias == null -> "Sin consumo registrado"
        dias <= 0 -> "Se agota hoy"
        dias == 1 -> "≈ 1 día restante"
        else -> "≈ $dias días restantes"
    }
}
