package com.example.silomonitor.ui.screens.incidencia

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.silomonitor.domain.validation.Validadores
import com.example.silomonitor.model.EventoUi
import com.example.silomonitor.model.IncidenciaUiState
import com.example.silomonitor.model.Prioridad
import com.example.silomonitor.model.TipoIncidencia
import com.example.silomonitor.ui.components.CampoFormulario
import com.example.silomonitor.ui.components.EstadoChip
import com.example.silomonitor.ui.components.ImagenInteligente
import com.example.silomonitor.ui.components.MensajeErrorCampo
import com.example.silomonitor.ui.components.MensajeExito
import com.example.silomonitor.ui.components.SelectorImagen
import com.example.silomonitor.ui.components.rememberSacudida
import com.example.silomonitor.viewmodel.IncidenciaViewModel

/** Reporte de incidencias en terreno con foto de evidencia (cámara o galería). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportarIncidenciaScreen(
    snackbarHostState: SnackbarHostState,
    onVolver: () -> Unit,
    viewModel: IncidenciaViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val sacudida = rememberSacudida(estado.intentosFallidos)
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                is EventoUi.MostrarMensaje -> snackbarHostState.showSnackbar(evento.mensaje)
                EventoUi.Volver -> onVolver()
                else -> Unit
            }
        }
    }

    val puedeReportar = estado.rol?.puedeReportarIncidencias == true
    val habilitado = !estado.guardando && !estado.guardadoExitoso && puedeReportar

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            estado.silo?.let { silo ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(silo.nombre, style = MaterialTheme.typography.titleLarge)
                        Text(
                            silo.granjaNombre,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    EstadoChip(silo.estado)
                }
            }

            ElevatedCard(
                Modifier
                    .fillMaxWidth()
                    .graphicsLayer { translationX = sacudida.value }
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SelectorTipo(estado, habilitado, viewModel::onTipoChange)

                    Text("Prioridad *", style = MaterialTheme.typography.titleSmall)
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        Prioridad.entries.forEachIndexed { indice, prioridad ->
                            SegmentedButton(
                                selected = estado.prioridad == prioridad,
                                onClick = { viewModel.onPrioridadChange(prioridad) },
                                shape = SegmentedButtonDefaults.itemShape(indice, Prioridad.entries.size),
                                enabled = habilitado,
                                label = { Text(prioridad.etiqueta) }
                            )
                        }
                    }
                    Text(
                        if (estado.prioridad == Prioridad.ALTA) "La prioridad alta requiere una foto como evidencia." else "La foto es opcional para esta prioridad.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))

                    CampoFormulario(
                        valor = estado.descripcion,
                        onValorChange = viewModel::onDescripcionChange,
                        etiqueta = "Descripción *",
                        iconoInicio = Icons.Filled.Description,
                        error = estado.errores.descripcion,
                        textoAyuda = "Qué ocurrió, dónde y desde cuándo (mín. ${Validadores.DESCRIPCION_MIN} caracteres)",
                        habilitado = habilitado,
                        singleLine = false,
                        minLines = 3,
                        maxLength = Validadores.DESCRIPCION_MAX,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                    )
                }
            }

            // ---------- Evidencia fotográfica (recurso nativo: cámara y galería) ----------
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        if (estado.prioridad == Prioridad.ALTA) "Evidencia fotográfica *" else "Evidencia fotográfica",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Box {
                        ImagenInteligente(
                            uri = estado.fotoUri,
                            circular = false,
                            iconoPorDefecto = Icons.Filled.PhotoCamera,
                            descripcion = "Foto de la incidencia",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                        if (estado.procesandoFoto) {
                            CircularProgressIndicator(Modifier.align(Alignment.Center))
                        }
                        if (estado.fotoUri != null && habilitado) {
                            FilledTonalIconButton(
                                onClick = viewModel::quitarFoto,
                                shape = CircleShape,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Quitar foto")
                            }
                        }
                    }
                    SelectorImagen(
                        onCrearUriCamara = viewModel::crearUriParaCamara,
                        onResultadoCamara = viewModel::onResultadoCamara,
                        onImagenGaleria = viewModel::onImagenGaleria,
                        onMensaje = viewModel::mostrarMensaje,
                        habilitado = habilitado && !estado.procesandoFoto
                    )
                    MensajeErrorCampo(estado.errores.foto)
                }
            }

            MensajeExito(
                visible = estado.guardadoExitoso,
                texto = "Incidencia reportada. El supervisor podrá revisarla en el detalle del silo."
            )

            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.guardar()
                },
                enabled = habilitado && !estado.procesandoFoto,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                AnimatedContent(
                    targetState = estado.guardando,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "boton_incidencia"
                ) { guardando ->
                    if (guardando) {
                        CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.ReportProblem, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Enviar reporte")
                        }
                    }
                }
            }
            if (!puedeReportar) {
                Text(
                    "Tu rol no permite reportar incidencias.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/** Grupo de radio buttons accesible para elegir el tipo de incidencia. */
@Composable
private fun SelectorTipo(
    estado: IncidenciaUiState,
    habilitado: Boolean,
    onSeleccionar: (TipoIncidencia) -> Unit
) {
    Column {
        Text("Tipo de incidencia *", style = MaterialTheme.typography.titleSmall)
        Column(Modifier.selectableGroup()) {
            TipoIncidencia.entries.forEach { tipo ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = estado.tipo == tipo,
                            onClick = { onSeleccionar(tipo) },
                            enabled = habilitado,
                            role = Role.RadioButton
                        )
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = estado.tipo == tipo, onClick = null, enabled = habilitado)
                    Spacer(Modifier.width(8.dp))
                    Text(tipo.etiqueta, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        MensajeErrorCampo(estado.errores.tipo)
    }
}
