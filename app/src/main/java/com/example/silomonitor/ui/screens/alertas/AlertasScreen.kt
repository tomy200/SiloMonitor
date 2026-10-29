package com.example.silomonitor.ui.screens.alertas

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.silomonitor.data.local.entity.AlertaDetalle
import com.example.silomonitor.model.EstadoSilo
import com.example.silomonitor.model.EventoUi
import com.example.silomonitor.model.FiltroAlertas
import com.example.silomonitor.model.Rol
import com.example.silomonitor.ui.components.EstadoChip
import com.example.silomonitor.ui.components.EstadoVacio
import com.example.silomonitor.ui.theme.colores
import com.example.silomonitor.ui.theme.icono
import com.example.silomonitor.util.Formatos
import com.example.silomonitor.viewmodel.AlertasViewModel

/** Alertas de nivel bajo/crítico. El supervisor las atiende; operarios pueden registrar la carga. */
@Composable
fun AlertasScreen(
    snackbarHostState: SnackbarHostState,
    onVerSilo: (String) -> Unit,
    onRegistrarCarga: (String) -> Unit,
    viewModel: AlertasViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    // rememberSaveable: el diálogo sobrevive a la rotación de pantalla
    var alertaPorConfirmar by rememberSaveable { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) {
        viewModel.eventos.collect { evento ->
            if (evento is EventoUi.MostrarMensaje) snackbarHostState.showSnackbar(evento.mensaje)
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FiltroAlertas.entries.forEach { filtro ->
                val cantidad = when (filtro) {
                    FiltroAlertas.ACTIVAS -> " (${estado.activas})"
                    FiltroAlertas.ATENDIDAS -> " (${estado.atendidas})"
                    FiltroAlertas.TODAS -> ""
                }
                FilterChip(
                    selected = estado.filtro == filtro,
                    onClick = { viewModel.cambiarFiltro(filtro) },
                    label = { Text(filtro.etiqueta + cantidad) }
                )
            }
        }

        when {
            estado.cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            estado.alertas.isEmpty() -> EstadoVacio(
                icono = Icons.Filled.NotificationsNone,
                titulo = if (estado.filtro == FiltroAlertas.ACTIVAS) "Sin alertas activas" else "Sin alertas",
                descripcion = "Recibirás una notificación cuando un silo baje del 30 % de su capacidad."
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(estado.alertas, key = { it.alerta.id }) { detalle ->
                    TarjetaAlerta(
                        detalle = detalle,
                        rol = estado.rol,
                        onVerSilo = { onVerSilo(detalle.alerta.siloId) },
                        onRegistrarCarga = { onRegistrarCarga(detalle.alerta.siloId) },
                        onAtender = { alertaPorConfirmar = detalle.alerta.id },
                        // Al atender una alerta, la tarjeta sale de la lista con animación
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }

    alertaPorConfirmar?.let { id ->
        AlertDialog(
            onDismissRequest = { alertaPorConfirmar = null },
            icon = { Icon(Icons.Filled.DoneAll, contentDescription = null) },
            title = { Text("¿Marcar como atendida?") },
            text = { Text("Confirma que la reposición fue coordinada. La alerta pasará al historial de atendidas.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.atender(id)
                    alertaPorConfirmar = null
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { alertaPorConfirmar = null }) { Text("Cancelar") }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TarjetaAlerta(
    detalle: AlertaDetalle,
    rol: Rol,
    onVerSilo: () -> Unit,
    onRegistrarCarga: () -> Unit,
    onAtender: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alerta = detalle.alerta
    val estado = EstadoSilo.desde(alerta.nivel)
    val colores = estado.colores()

    val colorFranja = if (alerta.atendida) MaterialTheme.colorScheme.outlineVariant else colores.principal

    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // Franja lateral con el color del estado
                .drawBehind {
                    drawRect(color = colorFranja, topLeft = Offset.Zero, size = Size(6.dp.toPx(), size.height))
                }
                .padding(start = 22.dp, end = 16.dp, top = 16.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (alerta.atendida) Icons.Filled.DoneAll else estado.icono(),
                    contentDescription = null,
                    tint = if (alerta.atendida) MaterialTheme.colorScheme.outline else colores.principal,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "${detalle.siloNombre} · ${detalle.granjaNombre}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                EstadoChip(estado)
            }
            Text(alerta.mensaje, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${Formatos.fechaHora(alerta.fecha)} · ${Formatos.tiempoRelativo(alerta.fecha)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (alerta.atendida) {
                Text(
                    "Atendida por ${alerta.atendidaPor.orEmpty()}" +
                        (alerta.fechaAtencion?.let { " el ${Formatos.fechaHora(it)}" } ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            // FlowRow: si no caben los botones en pantallas angostas, pasan a otra línea
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TextButton(onClick = onVerSilo) { Text("Ver silo") }
                if (!alerta.atendida && rol.puedeRegistrarMovimientos) {
                    FilledTonalButton(onClick = onRegistrarCarga) { Text("Registrar carga") }
                }
                if (!alerta.atendida && rol.puedeAtenderAlertas) {
                    Button(onClick = onAtender) {
                        Icon(Icons.Filled.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Atender")
                    }
                }
            }
        }
    }
}
