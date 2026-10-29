package com.example.silomonitor.data.remote

import com.example.silomonitor.data.local.entity.SiloEntity
import kotlinx.coroutines.delay
import java.io.IOException
import kotlin.random.Random

/** DTO que representa la lectura que entrega el sistema de monitoreo para un silo. */
data class LecturaSiloDto(
    val siloId: String,
    val nivelKg: Double,
    val fechaLectura: Long
)

/**
 * API simulada del sistema de monitoreo de silos (el caso permite datos simulados para el MVP).
 * Imita una llamada de red con demora y puede fallar para probar el modo sin conexión.
 * En la EFT puede reemplazarse por una API REST real (por ejemplo, con Retrofit)
 * sin cambiar las pantallas, porque solo el repositorio conoce esta clase.
 */
class SiloApiSimulada {

    suspend fun obtenerLecturas(silos: List<SiloEntity>, sinConexion: Boolean): List<LecturaSiloDto> {
        delay(1_500) // simula la latencia de la red
        if (sinConexion) {
            throw IOException("Sin conexión con el sistema de monitoreo")
        }
        val ahora = System.currentTimeMillis()
        return silos.map { silo ->
            // Consumo según el tiempo transcurrido (máximo un día) o un consumo mínimo simulado
            val horas = ((ahora - silo.ultimaActualizacion) / 3_600_000.0).coerceAtLeast(0.0)
            val porTiempo = (silo.consumoDiarioKg * horas / 24.0).coerceAtMost(silo.consumoDiarioKg)
            val simulado = silo.capacidadKg * Random.nextDouble(0.005, 0.03)
            val consumo = maxOf(porTiempo, simulado).coerceAtMost(silo.nivelKg)
            LecturaSiloDto(
                siloId = silo.id,
                nivelKg = (silo.nivelKg - consumo).coerceAtLeast(0.0),
                fechaLectura = ahora
            )
        }
    }
}
