package com.example.silomonitor.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.silomonitor.SiloMonitorApp
import com.example.silomonitor.data.seed.DatosSimulados
import com.example.silomonitor.domain.validation.Validadores
import com.example.silomonitor.model.EventoUi
import com.example.silomonitor.model.LoginErrores
import com.example.silomonitor.model.LoginUiState
import com.example.silomonitor.model.ResultadoLogin
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Lógica del formulario de inicio de sesión. La pantalla solo observa [estado]. */
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val contenedor = (application as SiloMonitorApp).contenedor

    // Estado interno mutable / estado expuesto a la UI (Guía 11)
    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    private val _eventos = MutableSharedFlow<EventoUi>()
    val eventos: SharedFlow<EventoUi> = _eventos.asSharedFlow()

    /** Después del primer intento de envío, los errores se recalculan mientras el usuario escribe. */
    private var validarEnVivo = false

    val usuariosDemo = DatosSimulados.usuariosDemo

    fun onCorreoChange(valor: String) {
        _estado.update {
            it.copy(
                correo = valor,
                errorGeneral = null,
                errores = it.errores.copy(correo = if (validarEnVivo) Validadores.validarCorreo(valor) else null)
            )
        }
    }

    fun onClaveChange(valor: String) {
        _estado.update {
            it.copy(
                clave = valor,
                errorGeneral = null,
                errores = it.errores.copy(clave = if (validarEnVivo) Validadores.validarClave(valor) else null)
            )
        }
    }

    fun onRecordarChange(valor: Boolean) {
        _estado.update { it.copy(recordar = valor) }
    }

    fun alternarMostrarClave() {
        _estado.update { it.copy(mostrarClave = !it.mostrarClave) }
    }

    /** Autocompleta las credenciales de un usuario de prueba (facilita la demostración). */
    fun usarUsuarioDemo(usuario: DatosSimulados.UsuarioDemo) {
        _estado.update {
            it.copy(correo = usuario.correo, clave = usuario.clave, errores = LoginErrores(), errorGeneral = null)
        }
    }

    /** Validación global del formulario: actualiza los errores y retorna true si todo es válido. */
    fun validarFormulario(): Boolean {
        validarEnVivo = true
        val actual = _estado.value
        val errores = LoginErrores(
            correo = Validadores.validarCorreo(actual.correo),
            clave = Validadores.validarClave(actual.clave)
        )
        _estado.update { it.copy(errores = errores) }
        return !errores.tieneErrores
    }

    fun iniciarSesion() {
        if (_estado.value.cargando) return
        if (!validarFormulario()) {
            registrarFallo()
            return
        }
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, errorGeneral = null) }
            val actual = _estado.value
            when (val resultado = contenedor.usuarioRepository.autenticar(actual.correo, actual.clave, actual.recordar)) {
                is ResultadoLogin.Exito -> {
                    contenedor.retroalimentacion.exito()
                    _estado.update { it.copy(cargando = false) }
                    _eventos.emit(EventoUi.NavegarInicio)
                }
                is ResultadoLogin.Error -> {
                    _estado.update { it.copy(cargando = false, errorGeneral = resultado.mensaje) }
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
