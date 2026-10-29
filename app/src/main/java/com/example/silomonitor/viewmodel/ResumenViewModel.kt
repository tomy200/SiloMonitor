package com.example.silomonitor.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.silomonitor.SiloMonitorApp
import com.example.silomonitor.data.local.entity.GranjaEntity
import com.example.silomonitor.domain.CalculosSilo
import com.example.silomonitor.model.EstadoSilo
import com.example.silomonitor.model.EventoUi
import com.example.silomonitor.model.ResumenGranja
import com.example.silomonitor.model.ResumenUiState
import com.example.silomonitor.model.Rol
import com.example.silomonitor.model.Sesion
import com.example.silomonitor.model.SiloItem
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Indicadores generales de todas las granjas (información resumida para jefaturas). */
class ResumenViewModel(application: Application) : AndroidViewModel(application) {

    private val contenedor = (application as SiloMonitorApp).contenedor
    private val repositorio = contenedor.siloRepository

    private val _eventos = MutableSharedFlow<EventoUi>()
    val eventos: SharedFlow<EventoUi> = _eventos.asSharedFlow()

    val estado: StateFlow<ResumenUiState> = combine(
        repositorio.silos,
        repositorio.granjas,
        contenedor.usuarioRepository.sesion,
        repositorio.ultimaSincronizacion,
        repositorio.sincronizando
    ) { silos, granjas, sesion, ultimaSync, sincronizando ->
        construirResumen(silos, granjas, sesion, ultimaSync, sincronizando)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ResumenUiState())

    fun sincronizar() {
        viewModelScope.launch {
            repositorio.sincronizar().fold(
                onSuccess = { resultado ->
                    contenedor.retroalimentacion.exito()
                    val extra = if (resultado.alertasNuevas > 0) " · ${resultado.alertasNuevas} alerta(s) nueva(s)" else ""
                    _eventos.emit(EventoUi.MostrarMensaje("Datos actualizados desde el sistema de monitoreo$extra"))
                },
                onFailure = {
                    contenedor.retroalimentacion.error()
                    _eventos.emit(EventoUi.MostrarMensaje("Sin conexión: se muestran los datos guardados en el dispositivo"))
                }
            )
        }
    }
}

/** Función pura que arma el resumen (separada para mantener el ViewModel simple). */
internal fun construirResumen(
    silos: List<SiloItem>,
    granjas: List<GranjaEntity>,
    sesion: Sesion?,
    ultimaSincronizacion: Long?,
    sincronizando: Boolean
): ResumenUiState {
    val resumenGranjas = granjas.map { granja ->
        val silosGranja = silos.filter { it.granjaId == granja.id }
        val disponible = silosGranja.sumOf { it.nivelKg }
        val capacidad = silosGranja.sumOf { it.capacidadKg }
        ResumenGranja(
            id = granja.id,
            nombre = granja.nombre,
            ubicacion = granja.ubicacion,
            totalSilos = silosGranja.size,
            porcentaje = CalculosSilo.porcentaje(disponible, capacidad),
            peorEstado = silosGranja.maxByOrNull { it.estado.gravedad }?.estado ?: EstadoSilo.NORMAL,
            criticos = silosGranja.count { it.estado == EstadoSilo.CRITICO },
            advertencias = silosGranja.count { it.estado == EstadoSilo.ADVERTENCIA }
        )
    }

    // Prioridad de reposición: silos bajo 30 % o que se agotan en 5 días o menos
    val prioridad = silos
        .filter { it.estado != EstadoSilo.NORMAL || (it.diasRestantes ?: Int.MAX_VALUE) <= 5 }
        .sortedWith(compareBy<SiloItem> { it.diasRestantes ?: Int.MAX_VALUE }.thenBy { it.porcentaje })
        .take(5)

    return ResumenUiState(
        cargando = false,
        nombreUsuario = sesion?.nombre.orEmpty(),
        rol = sesion?.rol ?: Rol.OPERARIO,
        totalSilos = silos.size,
        normales = silos.count { it.estado == EstadoSilo.NORMAL },
        advertencias = silos.count { it.estado == EstadoSilo.ADVERTENCIA },
        criticos = silos.count { it.estado == EstadoSilo.CRITICO },
        disponibleKg = silos.sumOf { it.nivelKg },
        capacidadKg = silos.sumOf { it.capacidadKg },
        granjas = resumenGranjas,
        prioridadReposicion = prioridad,
        ultimaSincronizacion = ultimaSincronizacion,
        sincronizando = sincronizando
    )
}
