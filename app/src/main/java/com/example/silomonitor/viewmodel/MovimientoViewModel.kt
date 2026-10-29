package com.example.silomonitor.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.silomonitor.SiloMonitorApp
import com.example.silomonitor.domain.validation.Validadores
import com.example.silomonitor.model.EventoUi
import com.example.silomonitor.model.MovimientoErrores
import com.example.silomonitor.model.MovimientoUiState
import com.example.silomonitor.model.ResultadoMovimiento
import com.example.silomonitor.model.TipoMovimiento
import com.example.silomonitor.navigation.Rutas
import com.example.silomonitor.util.Formatos
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Formulario de carga / descarga / consumo de alimento. */
class MovimientoViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val contenedor = (application as SiloMonitorApp).contenedor
    private val repositorio = contenedor.siloRepository

    private val siloId: String = savedStateHandle.get<String>(Rutas.ARG_SILO_ID).orEmpty()
    private val tipoInicial: TipoMovimiento =
        TipoMovimiento.desde(savedStateHandle.get<String>(Rutas.ARG_TIPO)) ?: TipoMovimiento.CARGA

    private val _estado = MutableStateFlow(MovimientoUiState(tipo = tipoInicial))
    val estado: StateFlow<MovimientoUiState> = _estado.asStateFlow()

    private val _eventos = MutableSharedFlow<EventoUi>()
    val eventos: SharedFlow<EventoUi> = _eventos.asSharedFlow()

    private var validarEnVivo = false

    init {
        // El silo se observa en tiempo real: si cambia su nivel, los límites se actualizan solos
        viewModelScope.launch {
            repositorio.silo(siloId).collect { silo ->
                _estado.update { it.copy(silo = silo) }
                val actual = _estado.value
                if (validarEnVivo && !actual.guardando && !actual.guardadoExitoso) revalidar()
            }
        }
        viewModelScope.launch {
            contenedor.usuarioRepository.sesion.collect { sesion ->
                _estado.update { it.copy(rol = sesion?.rol) }
            }
        }
    }

    fun onTipoChange(tipo: TipoMovimiento) {
        _estado.update { it.copy(tipo = tipo, errorGeneral = null) }
        if (validarEnVivo) revalidar() else _estado.update { it.copy(errores = MovimientoErrores()) }
    }

    fun onCantidadChange(valor: String) {
        if (valor.length > Validadores.CANTIDAD_MAX_DIGITOS + 2) return
        _estado.update { it.copy(cantidad = valor, errorGeneral = null) }
        if (validarEnVivo) revalidar() else _estado.update { it.copy(errores = it.errores.copy(cantidad = null)) }
    }

    fun onObservacionChange(valor: String) {
        _estado.update { it.copy(observacion = valor) }
        if (validarEnVivo) revalidar() else _estado.update { it.copy(errores = it.errores.copy(observacion = null)) }
    }

    private fun calcularErrores(s: MovimientoUiState): MovimientoErrores {
        val silo = s.silo
        return MovimientoErrores(
            cantidad = if (silo == null) {
                "No se encontró el silo"
            } else {
                Validadores.validarCantidad(s.cantidad, s.tipo, silo.nivelKg, silo.capacidadKg)
            },
            observacion = Validadores.validarObservacion(s.observacion, s.observacionObligatoria)
        )
    }

    private fun revalidar() {
        _estado.update { it.copy(errores = calcularErrores(it)) }
    }

    /** Validación global del formulario: retorna true si no hay errores. */
    fun validarFormulario(): Boolean {
        validarEnVivo = true
        val errores = calcularErrores(_estado.value)
        _estado.update { it.copy(errores = errores) }
        return !errores.tieneErrores
    }

    fun guardar() {
        val actual = _estado.value
        if (actual.guardando || actual.guardadoExitoso) return

        if (actual.rol?.puedeRegistrarMovimientos != true) {
            viewModelScope.launch { _eventos.emit(EventoUi.MostrarMensaje("Tu rol no permite registrar movimientos")) }
            return
        }
        if (!validarFormulario()) {
            _estado.update { it.copy(intentosFallidos = it.intentosFallidos + 1) }
            viewModelScope.launch { contenedor.retroalimentacion.error() }
            return
        }
        val silo = actual.silo ?: return
        val cantidad = actual.cantidadKg ?: return

        viewModelScope.launch {
            _estado.update { it.copy(guardando = true, errorGeneral = null) }
            val usuario = contenedor.usuarioRepository.sesion.first()?.nombre ?: "Usuario"
            val resultado = repositorio.registrarMovimiento(
                siloId = silo.id,
                tipo = actual.tipo,
                cantidadKg = cantidad,
                observacion = actual.observacion.trim(),
                usuario = usuario
            )
            resultado.fold(
                onSuccess = { r ->
                    if (r.alertaGenerada) contenedor.retroalimentacion.alerta() else contenedor.retroalimentacion.exito()
                    _estado.update {
                        it.copy(
                            guardando = false,
                            guardadoExitoso = true,
                            mensajeExito = construirMensaje(actual.tipo, cantidad, r)
                        )
                    }
                    delay(1_800) // deja ver el mensaje animado antes de volver
                    _eventos.emit(EventoUi.Volver)
                },
                onFailure = { error ->
                    contenedor.retroalimentacion.error()
                    _estado.update {
                        it.copy(guardando = false, errorGeneral = error.message ?: "No se pudo guardar el movimiento")
                    }
                }
            )
        }
    }

    private fun construirMensaje(tipo: TipoMovimiento, cantidad: Double, r: ResultadoMovimiento): String {
        val base = "${tipo.etiqueta} de ${Formatos.kg(cantidad)} registrada. " +
            "El silo quedó en ${Formatos.porcentaje(r.porcentajeNuevo)} (${r.estadoNuevo.etiqueta})."
        return if (r.alertaGenerada) "$base Se generó una alerta y se notificó al equipo." else base
    }
}
