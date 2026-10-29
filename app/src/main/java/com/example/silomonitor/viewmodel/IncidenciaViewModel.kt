package com.example.silomonitor.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.silomonitor.SiloMonitorApp
import com.example.silomonitor.domain.validation.Validadores
import com.example.silomonitor.model.EventoUi
import com.example.silomonitor.model.IncidenciaErrores
import com.example.silomonitor.model.IncidenciaUiState
import com.example.silomonitor.model.Prioridad
import com.example.silomonitor.model.TipoIncidencia
import com.example.silomonitor.navigation.Rutas
import com.example.silomonitor.util.ImagenHelper
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

/** Formulario de reporte de incidencias con foto desde cámara o galería (Guía 13). */
class IncidenciaViewModel(
    private val app: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(app) {

    private val contenedor = (app as SiloMonitorApp).contenedor
    private val repositorio = contenedor.siloRepository

    private val siloId: String = savedStateHandle.get<String>(Rutas.ARG_SILO_ID).orEmpty()

    private val _estado = MutableStateFlow(IncidenciaUiState())
    val estado: StateFlow<IncidenciaUiState> = _estado.asStateFlow()

    private val _eventos = MutableSharedFlow<EventoUi>()
    val eventos: SharedFlow<EventoUi> = _eventos.asSharedFlow()

    private var validarEnVivo = false

    init {
        viewModelScope.launch {
            repositorio.silo(siloId).collect { silo -> _estado.update { it.copy(silo = silo) } }
        }
        viewModelScope.launch {
            contenedor.usuarioRepository.sesion.collect { sesion -> _estado.update { it.copy(rol = sesion?.rol) } }
        }
    }

    // ---------------------------- Campos ----------------------------

    fun onTipoChange(tipo: TipoIncidencia) {
        _estado.update { it.copy(tipo = tipo) }
        actualizarErrores()
    }

    fun onPrioridadChange(prioridad: Prioridad) {
        _estado.update { it.copy(prioridad = prioridad) }
        actualizarErrores()
    }

    fun onDescripcionChange(valor: String) {
        _estado.update { it.copy(descripcion = valor) }
        actualizarErrores()
    }

    // ---------------------------- Cámara y galería ----------------------------

    /** Crea el archivo donde la cámara guardará la foto. Se guarda en SavedStateHandle por si el sistema cierra la app. */
    fun crearUriParaCamara(): Uri? {
        val uri = ImagenHelper.crearUriParaFoto(app, "incidencia")
        savedStateHandle[CLAVE_URI_CAMARA] = uri?.toString()
        return uri
    }

    /** Resultado de TakePicture(): si el usuario canceló, se borra el archivo vacío. */
    fun onResultadoCamara(exito: Boolean) {
        val uri = savedStateHandle.get<String>(CLAVE_URI_CAMARA)
        savedStateHandle[CLAVE_URI_CAMARA] = null
        if (uri == null) return
        if (exito) reemplazarFoto(uri) else ImagenHelper.eliminar(app, Uri.parse(uri))
    }

    /** Resultado de GetContent(): se copia la imagen a la carpeta privada de la app. */
    fun onImagenGaleria(uri: Uri) {
        viewModelScope.launch {
            _estado.update { it.copy(procesandoFoto = true) }
            val copia = ImagenHelper.copiarDesdeGaleria(app, uri, "incidencia")
            _estado.update { it.copy(procesandoFoto = false) }
            if (copia != null) {
                reemplazarFoto(copia.toString())
            } else {
                _eventos.emit(EventoUi.MostrarMensaje("No se pudo cargar la imagen seleccionada"))
            }
        }
    }

    fun quitarFoto() {
        val anterior = _estado.value.fotoUri
        _estado.update { it.copy(fotoUri = null) }
        anterior?.let { ImagenHelper.eliminar(app, Uri.parse(it)) }
        actualizarErrores()
    }

    fun mostrarMensaje(texto: String) {
        viewModelScope.launch { _eventos.emit(EventoUi.MostrarMensaje(texto)) }
    }

    private fun reemplazarFoto(nueva: String) {
        val anterior = _estado.value.fotoUri
        _estado.update { it.copy(fotoUri = nueva) }
        if (anterior != null && anterior != nueva) ImagenHelper.eliminar(app, Uri.parse(anterior))
        actualizarErrores()
    }

    // ---------------------------- Validación ----------------------------

    private fun calcularErrores(s: IncidenciaUiState) = IncidenciaErrores(
        tipo = Validadores.validarTipoIncidencia(s.tipo),
        descripcion = Validadores.validarDescripcion(s.descripcion),
        foto = Validadores.validarFotoIncidencia(s.prioridad, s.fotoUri)
    )

    private fun actualizarErrores() {
        if (validarEnVivo) {
            _estado.update { it.copy(errores = calcularErrores(it)) }
        } else {
            _estado.update { it.copy(errores = IncidenciaErrores()) }
        }
    }

    fun validarFormulario(): Boolean {
        validarEnVivo = true
        val errores = calcularErrores(_estado.value)
        _estado.update { it.copy(errores = errores) }
        return !errores.tieneErrores
    }

    fun guardar() {
        val actual = _estado.value
        if (actual.guardando || actual.guardadoExitoso || actual.procesandoFoto) return
        if (actual.rol?.puedeReportarIncidencias != true) {
            mostrarMensaje("Tu rol no permite reportar incidencias")
            return
        }
        if (!validarFormulario()) {
            _estado.update { it.copy(intentosFallidos = it.intentosFallidos + 1) }
            viewModelScope.launch { contenedor.retroalimentacion.error() }
            return
        }
        val tipo = actual.tipo ?: return

        viewModelScope.launch {
            _estado.update { it.copy(guardando = true) }
            val usuario = contenedor.usuarioRepository.sesion.first()?.nombre ?: "Usuario"
            val resultado = repositorio.reportarIncidencia(
                siloId = siloId,
                tipo = tipo,
                prioridad = actual.prioridad,
                descripcion = actual.descripcion.trim(),
                fotoUri = actual.fotoUri,
                usuario = usuario
            )
            if (resultado.isSuccess) {
                contenedor.retroalimentacion.exito()
                _estado.update { it.copy(guardando = false, guardadoExitoso = true) }
                delay(1_800)
                _eventos.emit(EventoUi.Volver)
            } else {
                contenedor.retroalimentacion.error()
                _estado.update { it.copy(guardando = false) }
                _eventos.emit(EventoUi.MostrarMensaje("No se pudo guardar la incidencia. Intenta nuevamente."))
            }
        }
    }

    /** Si se sale sin guardar, se elimina la foto para no dejar archivos huérfanos. */
    override fun onCleared() {
        super.onCleared()
        val actual = _estado.value
        if (!actual.guardadoExitoso) {
            actual.fotoUri?.let { ImagenHelper.eliminar(app, Uri.parse(it)) }
        }
    }

    private companion object {
        const val CLAVE_URI_CAMARA = "uri_camara_pendiente"
    }
}
