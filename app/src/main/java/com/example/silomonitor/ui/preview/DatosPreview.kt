package com.example.silomonitor.ui.preview

import com.example.silomonitor.domain.CalculosSilo
import com.example.silomonitor.model.EstadoSilo
import com.example.silomonitor.model.ResumenGranja
import com.example.silomonitor.model.ResumenUiState
import com.example.silomonitor.model.Rol
import com.example.silomonitor.model.SiloItem

/** Datos de ejemplo solo para los @Preview de Android Studio. */
object DatosPreview {

    private val ahora = System.currentTimeMillis()

    fun silo(id: String, granja: String, porcentaje: Double, dias: Int?): SiloItem {
        val capacidad = 20_000.0
        return SiloItem(
            id = id,
            nombre = "Silo $id",
            granjaId = 1,
            granjaNombre = granja,
            tipoAlimento = "Engorda pavo",
            capacidadKg = capacidad,
            nivelKg = capacidad * porcentaje / 100,
            consumoDiarioKg = 1_100.0,
            ultimaActualizacion = ahora - 25 * 60_000,
            porcentaje = porcentaje,
            estado = CalculosSilo.estado(porcentaje),
            diasRestantes = dias
        )
    }

    val silos = listOf(
        silo("A-01", "Granja Los Aromos", 11.0, 2),
        silo("P-04", "Granja El Peumo", 15.0, 2),
        silo("E-03", "Granja Santa Elena", 23.0, 4),
        silo("P-02", "Granja El Peumo", 26.0, 4)
    )

    val resumen = ResumenUiState(
        cargando = false,
        nombreUsuario = "Carla Muñoz",
        rol = Rol.SUPERVISOR,
        totalSilos = 12,
        normales = 8,
        advertencias = 2,
        criticos = 2,
        disponibleKg = 128_500.0,
        capacidadKg = 256_000.0,
        granjas = listOf(
            ResumenGranja(1, "Granja El Peumo", "Melipilla", 4, 42.0, EstadoSilo.CRITICO, 1, 1),
            ResumenGranja(2, "Granja Santa Elena", "San Pedro", 4, 54.0, EstadoSilo.ADVERTENCIA, 0, 1),
            ResumenGranja(3, "Granja Los Aromos", "Alhué", 4, 43.0, EstadoSilo.CRITICO, 1, 0)
        ),
        prioridadReposicion = silos,
        ultimaSincronizacion = ahora - 5 * 60_000
    )
}
