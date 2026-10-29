package com.example.silomonitor.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.silomonitor.SiloMonitorApp
import com.example.silomonitor.data.local.entity.UsuarioEntity
import com.example.silomonitor.model.EventoUi
import com.example.silomonitor.model.PerfilUiState
import com.example.silomonitor.model.Rol
import com.example.silomonitor.model.Sesion
import com.example.silomonitor.util.ImagenHelper
import com.example.silomonitor.util.NotificacionHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Perfil del usuario: foto (cámara/galería), preferencias guardadas en DataStore
 * con mensaje animado de confirmación (Guía 12) y cierre de sesión.
 */
class PerfilViewModel(
    private val app: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(app) {

    private val contenedor = (app as SiloMonitorApp).contenedor
    private val preferencias = contenedor.preferencias
    private val usuarios = contenedor.usuarioRepository

    @OptIn(ExperimentalCoroutinesApi::class)
    private val sesionYUsuario: Flow<Pair<Sesion?, UsuarioEntity?>> =
        usuarios.sesion.flatMapLatest { sesion ->
            if (sesion == null) {
                flowOf<Pair<Sesion?, UsuarioEntity?>>(null to null)
            } else {
                usuarios.observarUsuario(sesion.correo).map { usuario -> sesion to usuario }
            }
        }

    val estado: StateFlow<PerfilUiState> = combine(
        sesionYUsuario,
        preferencias.temaOscuro,
        preferencias.notificacionesActivas,
        preferencias.vibracionActiva,
        preferencias.simularSinConexion
    ) { (sesion, usuario), tema, notificaciones, vibracion, sinConexion ->
        PerfilUiState(
            cargando = false,
            nombre = sesion?.nombre.orEmpty(),
            correo = sesion?.correo.orEmpty(),
            rol = sesion?.rol ?: Rol.OPERARIO,
            fotoUri = usuario?.fotoUri,
            temaOscuro = tema,
            notificaciones = notificaciones,
            vibracion = vibracion,
            simularSinConexion = sinConexion
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PerfilUiState())

    // Mensaje animado "Preferencia guardada" (se oculta solo después de 2 segundos)
    private val _mensajeGuardado = MutableStateFlow<String?>(null)
    val mensajeGuardado: StateFlow<String?> = _mensajeGuardado.asStateFlow()
    private var trabajoMensaje: Job? = null

    private val _eventos = MutableSharedFlow<EventoUi>()
    val eventos: SharedFlow<EventoUi> = _eventos.asSharedFlow()

    private val _procesandoFoto = MutableStateFlow(false)
    val procesandoFoto: StateFlow<Boolean> = _procesandoFoto.asStateFlow()

    // ---------------------------- Preferencias ----------------------------

    fun cambiarTema(oscuro: Boolean) = guardarPreferencia("Tema actualizado") {
        preferencias.guardarTemaOscuro(oscuro)
    }

    fun cambiarNotificaciones(activas: Boolean) = guardarPreferencia(
        if (activas) "Notificaciones activadas" else "Notificaciones desactivadas"
    ) {
        preferencias.guardarNotificaciones(activas)
    }

    fun cambiarVibracion(activa: Boolean) = guardarPreferencia(
        if (activa) "Vibración activada" else "Vibración desactivada"
    ) {
        preferencias.guardarVibracion(activa)
        if (activa) contenedor.retroalimentacion.probar()
    }

    fun cambiarSimulacionSinConexion(valor: Boolean) = guardarPreferencia(
        if (valor) "Modo sin conexión simulado activado" else "Conexión simulada restablecida"
    ) {
        preferencias.guardarSimularSinConexion(valor)
    }

    private fun guardarPreferencia(mensaje: String, accion: suspend () -> Unit) {
        viewModelScope.launch {
            accion()
            mostrarConfirmacion(mensaje)
        }
    }

    private fun mostrarConfirmacion(texto: String) {
        trabajoMensaje?.cancel()
        trabajoMensaje = viewModelScope.launch {
            _mensajeGuardado.value = texto
            delay(2_000)
            _mensajeGuardado.value = null
        }
    }

    // ---------------------------- Notificaciones ----------------------------

    fun enviarNotificacionPrueba() {
        viewModelScope.launch {
            if (!NotificacionHelper.tienePermiso(app)) {
                _eventos.emit(EventoUi.MostrarMensaje("Debes permitir las notificaciones para recibir alertas"))
                return@launch
            }
            NotificacionHelper.mostrarAlerta(
                context = app,
                id = ID_NOTIFICACION_PRUEBA,
                titulo = "Notificación de prueba",
                mensaje = "Así recibirás las alertas cuando un silo baje del 30 % o del 20 % de su capacidad."
            )
            _eventos.emit(EventoUi.MostrarMensaje("Notificación enviada"))
        }
    }

    // ---------------------------- Foto de perfil ----------------------------

    fun crearUriParaCamara(): Uri? {
        val uri = ImagenHelper.crearUriParaFoto(app, "perfil")
        savedStateHandle[CLAVE_URI_CAMARA] = uri?.toString()
        return uri
    }

    fun onResultadoCamara(exito: Boolean) {
        val uri = savedStateHandle.get<String>(CLAVE_URI_CAMARA)
        savedStateHandle[CLAVE_URI_CAMARA] = null
        if (uri == null) return
        if (exito) guardarFoto(uri) else ImagenHelper.eliminar(app, Uri.parse(uri))
    }

    fun onImagenGaleria(uri: Uri) {
        viewModelScope.launch {
            _procesandoFoto.value = true
            val copia = ImagenHelper.copiarDesdeGaleria(app, uri, "perfil")
            _procesandoFoto.value = false
            if (copia != null) {
                guardarFoto(copia.toString())
            } else {
                _eventos.emit(EventoUi.MostrarMensaje("No se pudo cargar la imagen seleccionada"))
            }
        }
    }

    fun quitarFoto() = guardarFoto(null)

    private fun guardarFoto(nueva: String?) {
        viewModelScope.launch {
            // Se lee desde la fuente (y no desde la UI) por si el sistema recreó la pantalla al volver de la cámara
            val sesion = usuarios.sesion.first() ?: return@launch
            val anterior = usuarios.observarUsuario(sesion.correo).first()?.fotoUri
            usuarios.actualizarFoto(sesion.correo, nueva)
            if (anterior != null && anterior != nueva) ImagenHelper.eliminar(app, Uri.parse(anterior))
            contenedor.retroalimentacion.exito()
            mostrarConfirmacion(if (nueva != null) "Foto de perfil actualizada" else "Foto eliminada")
        }
    }

    fun mostrarMensaje(texto: String) {
        viewModelScope.launch { _eventos.emit(EventoUi.MostrarMensaje(texto)) }
    }

    // ---------------------------- Demostración y sesión ----------------------------

    fun restablecerDatos() {
        viewModelScope.launch {
            contenedor.siloRepository.restablecerDatos()
            _eventos.emit(EventoUi.MostrarMensaje("Datos de demostración restablecidos"))
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            usuarios.cerrarSesion()
            _eventos.emit(EventoUi.SesionCerrada)
        }
    }

    private companion object {
        const val CLAVE_URI_CAMARA = "uri_camara_perfil"
        const val ID_NOTIFICACION_PRUEBA = 9_001
    }
}
