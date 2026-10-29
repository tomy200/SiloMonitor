package com.example.silomonitor.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.silomonitor.SiloMonitorApp
import com.example.silomonitor.model.AlertasUiState
import com.example.silomonitor.model.EventoUi
import com.example.silomonitor.model.FiltroAlertas
import com.example.silomonitor.model.Rol
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Alertas de nivel. Solo el supervisor puede marcarlas como atendidas. */
class AlertasViewModel(application: Application) : AndroidViewModel(application) {

    private val contenedor = (application as SiloMonitorApp).contenedor
    private val repositorio = contenedor.siloRepository

    private val filtro = MutableStateFlow(FiltroAlertas.ACTIVAS)

    private val _eventos = MutableSharedFlow<EventoUi>()
    val eventos: SharedFlow<EventoUi> = _eventos.asSharedFlow()

    val estado: StateFlow<AlertasUiState> = combine(
        repositorio.alertas,
        filtro,
        contenedor.usuarioRepository.sesion
    ) { alertas, filtroActual, sesion ->
        AlertasUiState(
            cargando = false,
            alertas = when (filtroActual) {
                FiltroAlertas.ACTIVAS -> alertas.filter { !it.alerta.atendida }
                FiltroAlertas.ATENDIDAS -> alertas.filter { it.alerta.atendida }
                FiltroAlertas.TODAS -> alertas
            },
            filtro = filtroActual,
            activas = alertas.count { !it.alerta.atendida },
            atendidas = alertas.count { it.alerta.atendida },
            rol = sesion?.rol ?: Rol.OPERARIO
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AlertasUiState())

    fun cambiarFiltro(nuevo: FiltroAlertas) {
        filtro.value = nuevo
    }

    fun atender(alertaId: Long) {
        viewModelScope.launch {
            val sesion = contenedor.usuarioRepository.sesion.first()
            if (sesion == null || !sesion.rol.puedeAtenderAlertas) {
                _eventos.emit(EventoUi.MostrarMensaje("Solo un supervisor puede marcar alertas como atendidas"))
                return@launch
            }
            repositorio.atenderAlerta(alertaId, sesion.nombre)
            contenedor.retroalimentacion.exito()
            _eventos.emit(EventoUi.MostrarMensaje("Alerta marcada como atendida"))
        }
    }
}
