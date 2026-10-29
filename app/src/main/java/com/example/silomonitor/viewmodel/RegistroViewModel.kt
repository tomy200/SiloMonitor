package com.example.silomonitor.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.silomonitor.SiloMonitorApp
import com.example.silomonitor.domain.validation.Validadores
import com.example.silomonitor.model.EventoUi
import com.example.silomonitor.model.RegistroErrores
import com.example.silomonitor.model.RegistroUiState
import com.example.silomonitor.model.ResultadoLogin
import com.example.silomonitor.model.Rol
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Lógica del formulario de registro. La validación vive aquí y en Validadores, no en la pantalla. */
class RegistroViewModel(application: Application) : AndroidViewModel(application) {

    private val contenedor = (application as SiloMonitorApp).contenedor

    private val _estado = MutableStateFlow(RegistroUiState())
    val estado: StateFlow<RegistroUiState> = _estado.asStateFlow()

    private val _eventos = MutableSharedFlow<EventoUi>()
    val eventos: SharedFlow<EventoUi> = _eventos.asSharedFlow()

    /** Después del primer intento de envío, los errores se recalculan mientras el usuario escribe. */
    private var validarEnVivo = false

    fun onNombreChange(valor: String) {
        _estado.update { it.copy(nombre = valor, errorGeneral = null) }
        actualizarErrores()
    }

    fun onCorreoChange(valor: String) {
        _estado.update { it.copy(correo = valor, errorGeneral = null) }
        actualizarErrores()
    }

    fun onClaveChange(valor: String) {
        _estado.update { it.copy(clave = valor, errorGeneral = null) }
        actualizarErrores()
    }

    fun onConfirmarClaveChange(valor: String) {
        _estado.update { it.copy(confirmarClave = valor, errorGeneral = null) }
        actualizarErrores()
    }

    fun onRolChange(rol: Rol) {
        _estado.update { it.copy(rol = rol, errorGeneral = null) }
        actualizarErrores()
    }

    fun alternarMostrarClave() {
        _estado.update { it.copy(mostrarClave = !it.mostrarClave) }
    }

    private fun calcularErrores(s: RegistroUiState) = RegistroErrores(
        nombre = Validadores.validarNombre(s.nombre),
        correo = Validadores.validarCorreo(s.correo),
        clave = Validadores.validarClaveNueva(s.clave),
        confirmarClave = Validadores.validarConfirmacionClave(s.clave, s.confirmarClave),
        rol = Validadores.validarRol(s.rol)
    )

    private fun actualizarErrores() {
        if (validarEnVivo) {
            _estado.update { it.copy(errores = calcularErrores(it)) }
        } else {
            _estado.update { it.copy(errores = RegistroErrores()) }
        }
    }

    /** Validación global del formulario: retorna true si todo es válido. */
    fun validarFormulario(): Boolean {
        validarEnVivo = true
        val errores = calcularErrores(_estado.value)
        _estado.update { it.copy(errores = errores) }
        return !errores.tieneErrores
    }

    fun registrar() {
        if (_estado.value.cargando) return
        if (!validarFormulario()) {
            registrarFallo()
            return
        }
        val actual = _estado.value
        val rol = actual.rol ?: return
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, errorGeneral = null) }
            when (val resultado = contenedor.usuarioRepository.registrar(actual.nombre, actual.correo, actual.clave, rol)) {
                is ResultadoLogin.Exito -> {
                    contenedor.retroalimentacion.exito()
                    _estado.update { it.copy(cargando = false) }
                    _eventos.emit(EventoUi.NavegarInicio)
                }
                is ResultadoLogin.Error -> {
                    _estado.update {
                        it.copy(
                            cargando = false,
                            errorGeneral = resultado.mensaje,
                            errores = it.errores.copy(correo = resultado.mensaje)
                        )
                    }
                    registrarFallo()
                }
                ResultadoLogin.Autenticando -> Unit
            }
        }
    }

    private fun registrarFallo() {
        _estado.update { it.copy(intentosFallidos = it.intentosFallidos + 1) }
        viewModelScope.launch { contenedor.retroalimentacion.error() }
    }
}
