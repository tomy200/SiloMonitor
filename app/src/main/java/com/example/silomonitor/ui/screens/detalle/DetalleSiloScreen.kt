package com.example.silomonitor.ui.screens.detalle

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.silomonitor.data.local.entity.IncidenciaEntity
import com.example.silomonitor.data.local.entity.MovimientoEntity
import com.example.silomonitor.domain.CalculosSilo
import com.example.silomonitor.model.EstadoSilo
import com.example.silomonitor.model.Prioridad
import com.example.silomonitor.model.PuntoNivel
import com.example.silomonitor.model.Rol
import com.example.silomonitor.model.SiloItem
import com.example.silomonitor.model.TipoIncidencia
import com.example.silomonitor.model.TipoMovimiento
import com.example.silomonitor.ui.components.EstadoChip
import com.example.silomonitor.ui.components.EstadoVacio
import com.example.silomonitor.ui.components.FilaDato
import com.example.silomonitor.ui.components.GraficoHistorial
import com.example.silomonitor.ui.components.ImagenInteligente
import com.example.silomonitor.ui.components.SiloIndicador
import com.example.silomonitor.ui.components.TituloSeccion
import com.example.silomonitor.ui.theme.colores
import com.example.silomonitor.ui.utils.esExpandida
import com.example.silomonitor.util.Formatos
import com.example.silomonitor.viewmodel.DetalleSiloViewModel

/** Detalle de un silo: estado actual, autonomía, historial de niveles, movimientos e incidencias. */
@Composable
fun DetalleSiloScreen(
    windowSizeClass: WindowSizeClass,
    onRegistrarMovimiento: (String) -> Unit,
    onReportarIncidencia: (String) -> Unit,
    viewModel: DetalleSiloViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val silo = estado.silo

    when {
        estado.cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        silo == null -> EstadoVacio(
            icono = Icons.Filled.Search,
            titulo = "Silo no encontrado",
            descripcion = "Es posible que haya sido eliminado o que los datos se hayan restablecido."
        )
        windowSizeClass.esExpandida -> {
            // Tablet: dos paneles
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CabeceraSilo(silo)
                    DatosOperativos(silo)
                    Acciones(estado.rol, { onRegistrarMovimiento(silo.id) }, { onReportarIncidencia(silo.id) })
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TarjetaHistorial(estado.historial)
                    TarjetaMovimientos(estado.movimientos, silo.capacidadKg)
                    TarjetaIncidencias(estado.incidencias)
                }
            }
        }
        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CabeceraSilo(silo)
                Acciones(estado.rol, { onRegistrarMovimiento(silo.id) }, { onReportarIncidencia(silo.id) })
                DatosOperativos(silo)
                TarjetaHistorial(estado.historial)
                TarjetaMovimientos(estado.movimientos, silo.capacidadKg)
                TarjetaIncidencias(estado.incidencias)
            }
        }
    }
}

@Composable
private fun CabeceraSilo(silo: SiloItem) {
    val colores = silo.estado.colores()
    ElevatedCard(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SiloIndicador(
                porcentaje = silo.porcentaje,
                estado = silo.estado,
                modifier = Modifier.size(width = 104.dp, height = 156.dp)
            )
            Spacer(Modifier.width(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(silo.nombre, style = MaterialTheme.typography.headlineSmall)
                Text(
                    silo.granjaNombre,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                EstadoChip(silo.estado)
                Text(
                    Formatos.porcentaje(silo.porcentaje),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = colores.principal
                )
                Text(
                    "${Formatos.kg(silo.nivelKg)} de ${Formatos.kg(silo.capacidadKg)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun DatosOperativos(silo: SiloItem) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            TituloSeccion("Datos operativos")
            Spacer(Modifier.height(4.dp))
            FilaDato(Icons.Filled.Grain, "Tipo de alimento", silo.tipoAlimento)
            FilaDato(Icons.Filled.Restaurant, "Consumo diario estimado", "${Formatos.kg(silo.consumoDiarioKg)}/día")
            FilaDato(Icons.Filled.Schedule, "Autonomía", Formatos.diasRestantes(silo.diasRestantes))
            FilaDato(Icons.Filled.Storage, "Capacidad libre", Formatos.kg(silo.capacidadLibreKg))
            FilaDato(
                Icons.Filled.History,
                "Última actualización",
                "${Formatos.fechaHora(silo.ultimaActualizacion)} (${Formatos.tiempoRelativo(silo.ultimaActualizacion)})"
            )
        }
    }
}

@Composable
private fun Acciones(rol: Rol, onRegistrarMovimiento: () -> Unit, onReportarIncidencia: () -> Unit) {
    if (!rol.puedeRegistrarMovimientos && !rol.puedeReportarIncidencias) {
        Card(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Text(
                    "Tu perfil de ${rol.etiqueta} es de consulta: puedes revisar niveles, históricos y alertas.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        return
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        if (rol.puedeRegistrarMovimientos) {
            Button(onClick = onRegistrarMovimiento, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.AddCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Registrar movimiento")
            }
        }
        if (rol.puedeReportarIncidencias) {
            OutlinedButton(onClick = onReportarIncidencia, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.ReportProblem, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Incidencia")
            }
        }
    }
}

@Composable
private fun TarjetaHistorial(historial: List<PuntoNivel>) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TituloSeccion("Historial de nivel (últimos 14 días)")
            GraficoHistorial(
                puntos = historial,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LeyendaUmbral(EstadoSilo.ADVERTENCIA.colores().principal, "Advertencia ${CalculosSilo.UMBRAL_ADVERTENCIA.toInt()} %")
                LeyendaUmbral(EstadoSilo.CRITICO.colores().principal, "Crítico ${CalculosSilo.UMBRAL_CRITICO.toInt()} %")
            }
        }
    }
}

@Composable
private fun LeyendaUmbral(color: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(width = 20.dp, height = 2.dp)) {
            drawLine(
                color = color,
                start = Offset(0f, size.height / 2),
                end = Offset(size.width, size.height / 2),
                strokeWidth = size.height,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
            )
        }
        Spacer(Modifier.width(6.dp))
        Text(texto, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun TarjetaMovimientos(movimientos: List<MovimientoEntity>, capacidadKg: Double) {
    var expandido by rememberSaveable { mutableStateOf(false) }
    val visibles = if (expandido) movimientos.take(40) else movimientos.take(5)

    // animateContentSize: la tarjeta crece/se reduce suavemente al ver más movimientos
    ElevatedCard(
        Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(Modifier.padding(vertical = 12.dp)) {
            TituloSeccion(
                texto = "Últimos movimientos",
                modifier = Modifier.padding(horizontal = 16.dp),
                accion = if (movimientos.size > 5) {
                    {
                        TextButton(onClick = { expandido = !expandido }) {
                            Text(if (expandido) "Ver menos" else "Ver más")
                        }
                    }
                } else {
                    null
                }
            )
            if (movimientos.isEmpty()) {
                Text(
                    "Sin movimientos registrados",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            visibles.forEachIndexed { indice, movimiento ->
                if (indice > 0) HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                FilaMovimiento(movimiento, capacidadKg)
            }
        }
    }
}

@Composable
private fun FilaMovimiento(movimiento: MovimientoEntity, capacidadKg: Double) {
    val tipo = TipoMovimiento.desde(movimiento.tipo) ?: TipoMovimiento.CONSUMO
    val (icono, color, signo) = when (tipo) {
        TipoMovimiento.CARGA -> Triple(Icons.Filled.AddCircle, EstadoSilo.NORMAL.colores().principal, "+")
        TipoMovimiento.DESCARGA -> Triple(Icons.Filled.RemoveCircle, EstadoSilo.ADVERTENCIA.colores().principal, "−")
        TipoMovimiento.CONSUMO -> Triple(Icons.Filled.Restaurant, MaterialTheme.colorScheme.tertiary, "−")
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = tipo.etiqueta, tint = color)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "${tipo.etiqueta} · $signo${Formatos.kg(movimiento.cantidadKg)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "${Formatos.fechaHora(movimiento.fecha)} · ${movimiento.usuario}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (movimiento.observacion.isNotBlank()) {
                Text(movimiento.observacion, style = MaterialTheme.typography.bodySmall)
            }
        }
        Text(
            Formatos.porcentaje(CalculosSilo.porcentaje(movimiento.nivelResultanteKg, capacidadKg)),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun TarjetaIncidencias(incidencias: List<IncidenciaEntity>) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(vertical = 12.dp)) {
            TituloSeccion("Incidencias reportadas", Modifier.padding(horizontal = 16.dp))
            if (incidencias.isEmpty()) {
                Text(
                    "Sin incidencias reportadas para este silo",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            incidencias.forEachIndexed { indice, incidencia ->
                if (indice > 0) HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                FilaIncidencia(incidencia)
            }
        }
    }
}

@Composable
private fun FilaIncidencia(incidencia: IncidenciaEntity) {
    val prioridad = Prioridad.desde(incidencia.prioridad)
    val colorPrioridad = when (prioridad) {
        Prioridad.ALTA -> EstadoSilo.CRITICO.colores().principal
        Prioridad.MEDIA -> EstadoSilo.ADVERTENCIA.colores().principal
        Prioridad.BAJA -> EstadoSilo.NORMAL.colores().principal
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        ImagenInteligente(
            uri = incidencia.fotoUri,
            circular = false,
            iconoPorDefecto = Icons.Filled.ReportProblem,
            descripcion = "Foto de la incidencia",
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                TipoIncidencia.desde(incidencia.tipo).etiqueta,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Prioridad ${prioridad.etiqueta.lowercase()}",
                style = MaterialTheme.typography.labelMedium,
                color = colorPrioridad
            )
            Text(incidencia.descripcion, style = MaterialTheme.typography.bodySmall)
            Text(
                "${Formatos.fechaHora(incidencia.fecha)} · ${incidencia.usuario}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
