package com.example.silomonitor.ui.screens.perfil

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.silomonitor.model.EstadoSilo
import com.example.silomonitor.model.EventoUi
import com.example.silomonitor.model.PerfilUiState
import com.example.silomonitor.ui.components.ImagenInteligente
import com.example.silomonitor.ui.components.MensajeExito
import com.example.silomonitor.ui.components.SelectorImagen
import com.example.silomonitor.ui.components.TituloSeccion
import com.example.silomonitor.ui.theme.colores
import com.example.silomonitor.util.NotificacionHelper
import com.example.silomonitor.viewmodel.PerfilViewModel

/** Perfil: foto con cámara/galería, permisos del rol, preferencias y cierre de sesión. */
@Composable
fun PerfilScreen(
    snackbarHostState: SnackbarHostState,
    onSesionCerrada: () -> Unit,
    viewModel: PerfilViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val mensajeGuardado by viewModel.mensajeGuardado.collectAsState()
    val procesandoFoto by viewModel.procesandoFoto.collectAsState()
    val context = LocalContext.current

    var confirmarSalida by rememberSaveable { mutableStateOf(false) }
    var confirmarRestablecer by rememberSaveable { mutableStateOf(false) }

    // Android 13+: al activar las notificaciones se pide el permiso en tiempo de ejecución
    val permisoNotificaciones = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        viewModel.cambiarNotificaciones(concedido)
        if (!concedido) viewModel.mostrarMensaje("Sin permiso no se pueden mostrar alertas en el teléfono")
    }

    LaunchedEffect(Unit) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                is EventoUi.MostrarMensaje -> snackbarHostState.showSnackbar(evento.mensaje)
                EventoUi.SesionCerrada -> onSesionCerrada()
                else -> Unit
            }
        }
    }

    if (estado.cargando) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ---------- Encabezado con foto (Guía 13) ----------
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        ImagenInteligente(
                            uri = estado.fotoUri,
                            descripcion = "Foto de perfil",
                            iconoPorDefecto = Icons.Filled.Person,
                            modifier = Modifier.size(120.dp)
                        )
                        if (procesandoFoto) CircularProgressIndicator()
                    }
                    Text(estado.nombre, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                    Text(
                        estado.correo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AssistChip(onClick = {}, label = { Text(estado.rol.etiqueta) })
                    SelectorImagen(
                        onCrearUriCamara = viewModel::crearUriParaCamara,
                        onResultadoCamara = viewModel::onResultadoCamara,
                        onImagenGaleria = viewModel::onImagenGaleria,
                        onMensaje = viewModel::mostrarMensaje,
                        habilitado = !procesandoFoto
                    )
                    if (estado.fotoUri != null) {
                        TextButton(onClick = viewModel::quitarFoto) {
                            Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Quitar foto")
                        }
                    }
                }
            }

            // Mensaje animado de confirmación (Guía 12)
            MensajeExito(visible = mensajeGuardado != null, texto = mensajeGuardado.orEmpty())

            PermisosDelRol(estado)

            // ---------- Preferencias (DataStore) ----------
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(vertical = 8.dp)) {
                    TituloSeccion("Preferencias", Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    FilaSwitch(
                        icono = Icons.Filled.Brightness4,
                        titulo = "Tema oscuro",
                        descripcion = "Reduce el brillo en terreno y de noche",
                        activo = estado.temaOscuro ?: isSystemInDarkTheme(),
                        onCambio = viewModel::cambiarTema
                    )
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                    FilaSwitch(
                        icono = Icons.Filled.Notifications,
                        titulo = "Notificaciones de alertas",
                        descripcion = "Aviso cuando un silo baja del 30 % o del 20 %",
                        activo = estado.notificaciones,
                        onCambio = { activar ->
                            val requierePermiso = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                !NotificacionHelper.tienePermiso(context)
                            if (activar && requierePermiso) {
                                permisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.cambiarNotificaciones(activar)
                            }
                        }
                    )
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                    FilaSwitch(
                        icono = Icons.Filled.Vibration,
                        titulo = "Vibración",
                        descripcion = "Confirma acciones y avisa errores con vibración",
                        activo = estado.vibracion,
                        onCambio = viewModel::cambiarVibracion
                    )
                    Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        OutlinedButton(
                            onClick = viewModel::enviarNotificacionPrueba,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Enviar notificación de prueba")
                        }
                    }
                }
            }

            // ---------- Opciones de demostración ----------
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(vertical = 8.dp)) {
                    TituloSeccion("Opciones de demostración", Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    FilaSwitch(
                        icono = Icons.Filled.CloudOff,
                        titulo = "Simular sin conexión",
                        descripcion = "La API falla y la app trabaja con los datos guardados en el teléfono",
                        activo = estado.simularSinConexion,
                        onCambio = viewModel::cambiarSimulacionSinConexion
                    )
                    Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        OutlinedButton(
                            onClick = { confirmarRestablecer = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Restablecer datos simulados")
                        }
                    }
                }
            }

            Button(
                onClick = { confirmarSalida = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Filled.ExitToApp, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión")
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            icon = { Icon(Icons.Filled.ExitToApp, contentDescription = null) },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Los datos de los silos seguirán guardados en el dispositivo.") },
            confirmButton = {
                Button(onClick = {
                    confirmarSalida = false
                    viewModel.cerrarSesion()
                }) { Text("Cerrar sesión") }
            },
            dismissButton = { TextButton(onClick = { confirmarSalida = false }) { Text("Cancelar") } }
        )
    }

    if (confirmarRestablecer) {
        AlertDialog(
            onDismissRequest = { confirmarRestablecer = false },
            icon = { Icon(Icons.Filled.Refresh, contentDescription = null) },
            title = { Text("¿Restablecer datos?") },
            text = { Text("Se borrarán los movimientos, alertas, incidencias y cuentas creadas, y se cargarán nuevamente los datos simulados.") },
            confirmButton = {
                Button(onClick = {
                    confirmarRestablecer = false
                    viewModel.restablecerDatos()
                }) { Text("Restablecer") }
            },
            dismissButton = { TextButton(onClick = { confirmarRestablecer = false }) { Text("Cancelar") } }
        )
    }
}

/** Lista visible de lo que puede y no puede hacer el rol actual (permisos diferenciados). */
@Composable
private fun PermisosDelRol(estado: PerfilUiState) {
    val rol = estado.rol
    val permisos = listOf(
        "Consultar niveles, históricos y alertas" to true,
        "Registrar cargas, descargas y consumos" to rol.puedeRegistrarMovimientos,
        "Reportar incidencias con foto" to rol.puedeReportarIncidencias,
        "Marcar alertas como atendidas" to rol.puedeAtenderAlertas
    )
    val permitido = EstadoSilo.NORMAL.colores().principal

    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TituloSeccion("Permisos de tu rol")
            Text(rol.descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            permisos.forEach { (texto, tiene) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (tiene) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                        contentDescription = if (tiene) "Permitido" else "No permitido",
                        tint = if (tiene) permitido else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        texto,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (tiene) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
private fun FilaSwitch(
    icono: ImageVector,
    titulo: String,
    descripcion: String,
    activo: Boolean,
    onCambio: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.bodyLarge)
            Text(descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(8.dp))
        Switch(checked = activo, onCheckedChange = onCambio)
    }
}
