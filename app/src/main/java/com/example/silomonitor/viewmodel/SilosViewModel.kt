package com.example.silomonitor.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.silomonitor.SiloMonitorApp
import com.example.silomonitor.model.EstadoSilo
import com.example.silomonitor.model.EventoUi
import com.example.silomonitor.model.SilosUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Lista de silos por granja con filtros por granja y por estado. */
class SilosViewModel(application: Application) : AndroidViewModel(application) {

    private val contenedor = (application as SiloMonitorApp).contenedor
    private val repositorio = contenedor.siloRepository

    private val granjaSeleccionada = MutableStateFlow<Int?>(null)
    private val estadoSeleccionado = MutableStateFlow<EstadoSilo?>(null)

    private val _eventos = MutableSharedFlow<EventoUi>()
    val eventos: SharedFlow<EventoUi> = _eventos.asSharedFlow()

    val estado: StateFlow<SilosUiState> = combine(
        repositorio.silos,
        repositorio.granjas,
        granjaSeleccionada,
        estadoSeleccionado,
        repositorio.sincronizando
    ) { silos, granjas, granja, estadoFiltro, sincronizando ->
        SilosUiState(
            cargando = false,
            silos = silos.filter { silo ->
                (granja == null || silo.granjaId == granja) &&
                    (estadoFiltro == null || silo.estado == estadoFiltro)
            },
            totalSinFiltro = silos.size,
            granjas = granjas,
            granjaSeleccionada = granja,
            estadoSeleccionado = estadoFiltro,
            sincronizando = sincronizando
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SilosUiState())

    fun seleccionarGranja(id: Int?) {
        granjaSeleccionada.value = id
    }

    fun seleccionarEstado(estado: EstadoSilo?) {
        estadoSeleccionado.value = estado
    }

    fun limpiarFiltros() {
        granjaSeleccionada.value = null
        estadoSeleccionado.value = null
    }

    /** Pull-to-refresh: consulta la API simulada. */
    fun sincronizar() {
        viewModelScope.launch {
            repositorio.sincronizar().fold(
                onSuccess = { resultado ->
                    contenedor.retroalimentacion.exito()
                    val extra = if (resultado.alertasNuevas > 0) " · ${resultado.alertasNuevas} alerta(s) nueva(s)" else ""
                    _eventos.emit(EventoUi.MostrarMensaje("Niveles actualizados$extra"))
                },
                onFailure = {
                    contenedor.retroalimentacion.error()
                    _eventos.emit(EventoUi.MostrarMensaje("Sin conexión: se muestran los datos guardados en el dispositivo"))
                }
            )
        }
    }
}
