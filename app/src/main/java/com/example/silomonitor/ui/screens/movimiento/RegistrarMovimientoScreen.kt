package com.example.silomonitor.ui.screens.movimiento

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.silomonitor.domain.CalculosSilo
import com.example.silomonitor.domain.validation.Validadores
import com.example.silomonitor.model.EventoUi
import com.example.silomonitor.model.SiloItem
import com.example.silomonitor.model.TipoMovimiento
import com.example.silomonitor.ui.components.BarraNivel
import com.example.silomonitor.ui.components.CampoFormulario
import com.example.silomonitor.ui.components.EstadoChip
import com.example.silomonitor.ui.components.MensajeError
import com.example.silomonitor.ui.components.MensajeExito
import com.example.silomonitor.ui.components.SiloIndicador
import com.example.silomonitor.ui.components.rememberSacudida
import com.example.silomonitor.ui.theme.colores
import com.example.silomonitor.util.Formatos
import com.example.silomonitor.viewmodel.MovimientoViewModel

/** Formulario para registrar cargas, descargas y consumos de alimento. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarMovimientoScreen(
    snackbarHostState: SnackbarHostState,
    onVolver: () -> Unit,
    viewModel: MovimientoViewModel = viewModel()
) {
    val estadoState = viewModel.estado.collectAsState()
    val estado by estadoState
    val sacudida = rememberSacudida(estado.intentosFallidos)
    val focusManager = LocalFocusManager.current

    // derivedStateOf (Guía 12): la vista previa solo se recalcula cuando cambia el resultado
    val porcentajeProyectado by remember {
        derivedStateOf { estadoState.value.porcentajeProyectado }
    }

    LaunchedEffect(Unit) {
        viewModel.eventos.collect { evento ->
            when (evento) {
                is EventoUi.MostrarMensaje -> snackbarHostState.showSnackbar(evento.mensaje)
                EventoUi.Volver -> onVolver()
                else -> Unit
            }
        }
    }

    val silo = estado.silo
    if (silo == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val puedeRegistrar = estado.rol?.puedeRegistrarMovimientos == true
    val habilitado = !estado.guardando && !estado.guardadoExitoso && puedeRegistrar

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
            ResumenSiloFormulario(silo)

            // ---------- Formulario ----------
            ElevatedCard(
                Modifier
                    .fillMaxWidth()
                    .graphicsLayer { translationX = sacudida.value }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Tipo de movimiento *", style = MaterialTheme.typography.titleSmall)
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        TipoMovimiento.entries.forEachIndexed { indice, tipo ->
                            SegmentedButton(
                                selected = estado.tipo == tipo,
                                onClick = { viewModel.onTipoChange(tipo) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = indice,
                                    count = TipoMovimiento.entries.size
                                ),
                                enabled = habilitado,
                                label = { Text(tipo.etiqueta) }
                            )
                        }
                    }
                    AnimatedContent(
                        targetState = estado.tipo,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "descripcion_tipo"
                    ) { tipo ->
                        Text(
                            tipo.descripcion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(4.dp))

                    CampoFormulario(
                        valor = estado.cantidad,
                        onValorChange = viewModel::onCantidadChange,
                        etiqueta = "Cantidad *",
                        iconoInicio = Icons.Filled.Speed,
                        sufijo = "kg",
                        error = estado.errores.cantidad,
                        textoAyuda = "Máximo permitido: ${Formatos.kg(estado.maximoPermitidoKg)}",
                        habilitado = habilitado,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        )
                    )

                    CampoFormulario(
                        valor = estado.observacion,
                        onValorChange = viewModel::onObservacionChange,
                        etiqueta = if (estado.observacionObligatoria) "Motivo de la descarga *" else "Observación (opcional)",
                        iconoInicio = Icons.Filled.Edit,
                        error = estado.errores.observacion,
                        textoAyuda = if (estado.observacionObligatoria) {
                            "Ej: traslado a otra granja, merma, devolución"
                        } else {
                            "Información adicional para el equipo"
                        },
                        habilitado = habilitado,
                        singleLine = false,
                        minLines = 3,
                        maxLength = Validadores.OBSERVACION_MAX,
                        mostrarValido = estado.observacionObligatoria,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                    )
                }
            }

            VistaPreviaNivel(silo, porcentajeProyectado)

            MensajeError(estado.errorGeneral)
            MensajeExito(visible = estado.guardadoExitoso, texto = estado.mensajeExito.orEmpty())

            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.guardar()
                },
                enabled = habilitado,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                AnimatedContent(
                    targetState = estado.guardando,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "boton_guardar"
                ) { guardando ->
                    if (guardando) {
                        CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Guardar movimiento")
                    }
                }
            }
            if (!puedeRegistrar) {
                Text(
                    "Tu rol no permite registrar movimientos.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                "* Campos obligatorios",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ResumenSiloFormulario(silo: SiloItem) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            SiloIndicador(
                porcentaje = silo.porcentaje,
                estado = silo.estado,
                mostrarTexto = false,
                modifier = Modifier.size(width = 44.dp, height = 66.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(silo.nombre, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    EstadoChip(silo.estado)
                }
                Text(
                    "${silo.granjaNombre} · ${silo.tipoAlimento}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                BarraNivel(silo.porcentaje, silo.estado, alto = 8.dp)
                Text(
                    "Disponible ${Formatos.kg(silo.nivelKg)} · Libre ${Formatos.kg(silo.capacidadLibreKg)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/** Vista previa animada de cómo quedará el silo antes de guardar. */
@Composable
private fun VistaPreviaNivel(silo: SiloItem, porcentajeProyectado: Double?) {
    val estadoProyectado = porcentajeProyectado?.let { CalculosSilo.estado(it) }
    val empeora = estadoProyectado != null && estadoProyectado.gravedad > silo.estado.gravedad

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Vista previa del nivel", style = MaterialTheme.typography.titleSmall)
            Row(verticalAlignment = Alignment.CenterVertically) {
                SiloIndicador(
                    porcentaje = porcentajeProyectado ?: silo.porcentaje,
                    estado = estadoProyectado ?: silo.estado,
                    modifier = Modifier.size(width = 60.dp, height = 90.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Actual: ${Formatos.porcentaje(silo.porcentaje)} (${silo.estado.etiqueta})",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = if (porcentajeProyectado != null && estadoProyectado != null) {
                            "Después de guardar: ${Formatos.porcentaje(porcentajeProyectado)} (${estadoProyectado.etiqueta})"
                        } else {
                            "Ingresa una cantidad para ver cómo quedará el silo"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = estadoProyectado?.colores()?.principal ?: MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            AnimatedVisibility(
                visible = empeora,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Este movimiento dejará el silo en estado ${estadoProyectado?.etiqueta.orEmpty()} " +
                            "y se generará una alerta para el equipo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
