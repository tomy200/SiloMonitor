package com.example.silomonitor.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.silomonitor.SiloMonitorApp
import com.example.silomonitor.data.local.entity.MovimientoEntity
import com.example.silomonitor.domain.CalculosSilo
import com.example.silomonitor.model.DetalleSiloUiState
import com.example.silomonitor.model.PuntoNivel
import com.example.silomonitor.model.Rol
import com.example.silomonitor.model.SiloItem
import com.example.silomonitor.navigation.Rutas
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Detalle de un silo. El id llega como argumento de la ruta mediante SavedStateHandle. */
class DetalleSiloViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val contenedor = (application as SiloMonitorApp).contenedor
    private val repositorio = contenedor.siloRepository

    val siloId: String = savedStateHandle.get<String>(Rutas.ARG_SILO_ID).orEmpty()

    val estado: StateFlow<DetalleSiloUiState> = combine(
        repositorio.silo(siloId),
        repositorio.movimientos(siloId),
        repositorio.incidencias(siloId),
        contenedor.usuarioRepository.sesion
    ) { silo, movimientos, incidencias, sesion ->
        DetalleSiloUiState(
            cargando = false,
            silo = silo,
            movimientos = movimientos,
            historial = construirHistorial(silo, movimientos),
            incidencias = incidencias,
            rol = sesion?.rol ?: Rol.OPERARIO
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetalleSiloUiState())

    /** Puntos del gráfico: nivel después de cada movimiento de los últimos 14 días + nivel actual. */
    private fun construirHistorial(silo: SiloItem?, movimientos: List<MovimientoEntity>): List<PuntoNivel> {
        if (silo == null) return emptyList()
        val desde = System.currentTimeMillis() - DIAS_HISTORIAL * 86_400_000L
        val puntos = movimientos
            .filter { it.fecha >= desde }
            .sortedBy { it.fecha }
            .map { PuntoNivel(it.fecha, CalculosSilo.porcentaje(it.nivelResultanteKg, silo.capacidadKg).toFloat()) }
        val fechaActual = maxOf(silo.ultimaActualizacion, puntos.lastOrNull()?.fecha ?: 0L)
        return puntos + PuntoNivel(fechaActual, silo.porcentaje.toFloat())
    }

    private companion object {
        const val DIAS_HISTORIAL = 14
    }
}
