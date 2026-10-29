package com.example.silomonitor.ui.screens.resumen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.silomonitor.domain.CalculosSilo
import com.example.silomonitor.model.EstadoSilo
import com.example.silomonitor.model.EventoUi
import com.example.silomonitor.model.ResumenGranja
import com.example.silomonitor.model.ResumenUiState
import com.example.silomonitor.model.SiloItem
import com.example.silomonitor.ui.components.BarraNivel
import com.example.silomonitor.ui.components.EstadoChip
import com.example.silomonitor.ui.components.EstadoVacio
import com.example.silomonitor.ui.components.KpiCard
import com.example.silomonitor.ui.components.SiloIndicador
import com.example.silomonitor.ui.components.TituloSeccion
import com.example.silomonitor.ui.preview.DatosPreview
import com.example.silomonitor.ui.theme.SiloMonitorTheme
import com.example.silomonitor.ui.theme.colores
import com.example.silomonitor.util.Formatos
import com.example.silomonitor.viewmodel.ResumenViewModel

/** Pantalla con estado (conectada al ViewModel). */
@Composable
fun ResumenScreen(
    anchoVentana: WindowWidthSizeClass,
    snackbarHostState: SnackbarHostState,
    onVerSilo: (String) -> Unit,
    onVerAlertas: () -> Unit,
    viewModel: ResumenViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.eventos.collect { evento ->
            if (evento is EventoUi.MostrarMensaje) snackbarHostState.showSnackbar(evento.mensaje)
        }
    }

    ResumenContenido(
        estado = estado,
        anchoVentana = anchoVentana,
        onSincronizar = viewModel::sincronizar,
        onVerSilo = onVerSilo,
        onVerAlertas = onVerAlertas
    )
}

/**
 * Contenido sin estado (state hoisting): recibe todo por parámetros,
 * lo que permite previsualizarlo en los tres tamaños de ventana (Guía 9).
 */
@Composable
fun ResumenContenido(
    estado: ResumenUiState,
    anchoVentana: WindowWidthSizeClass,
    onSincronizar: () -> Unit,
    onVerSilo: (String) -> Unit,
    onVerAlertas: () -> Unit
) {
    if (estado.cargando) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        EncabezadoResumen(estado, onSincronizar)

        when (anchoVentana) {
            WindowWidthSizeClass.Expanded -> {
                // Tablet: dos columnas
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Indicadores(estado, columnas = 4, onVerAlertas = onVerAlertas)
                        TarjetaDisponibilidad(estado)
                        EstadoPorGranja(estado.granjas)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        PrioridadReposicion(estado.prioridadReposicion, onVerSilo)
                    }
                }
            }
            WindowWidthSizeClass.Medium -> {
                Indicadores(estado, columnas = 4, onVerAlertas = onVerAlertas)
                TarjetaDisponibilidad(estado)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(Modifier.weight(1f)) { PrioridadReposicion(estado.prioridadReposicion, onVerSilo) }
                    Column(Modifier.weight(1f)) { EstadoPorGranja(estado.granjas) }
                }
            }
            else -> {
                // Celular: una columna
                Indicadores(estado, columnas = 2, onVerAlertas = onVerAlertas)
                TarjetaDisponibilidad(estado)
                PrioridadReposicion(estado.prioridadReposicion, onVerSilo)
                EstadoPorGranja(estado.granjas)
            }
        }
    }
}

@Composable
private fun EncabezadoResumen(estado: ResumenUiState, onSincronizar: () -> Unit) {
    // Ícono de sincronización que gira mientras se consulta la API
    val transicion = rememberInfiniteTransition(label = "giro_sync")
    val giro by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1_000, easing = LinearEasing), RepeatMode.Restart),
        label = "rotacion"
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = if (estado.nombreUsuario.isNotBlank()) "Hola, ${estado.nombreUsuario.substringBefore(" ")}" else "Hola",
            style = MaterialTheme.typography.headlineSmall
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            AssistChip(onClick = {}, label = { Text(estado.rol.etiqueta) })
            Spacer(Modifier.width(8.dp))
            Text(
                text = estado.rol.descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }
        Card {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.CloudDone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Última sincronización", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = estado.ultimaSincronizacion?.let { Formatos.tiempoRelativo(it) } ?: "Aún no sincronizado",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilledTonalButton(onClick = onSincronizar, enabled = !estado.sincronizando) {
                    Icon(
                        Icons.Filled.Sync,
                        contentDescription = null,
                        modifier = Modifier
                            .size(18.dp)
                            .graphicsLayer { rotationZ = if (estado.sincronizando) -giro else 0f }
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(if (estado.sincronizando) "Actualizando" else "Actualizar")
                }
            }
        }
    }
}

@Composable
private fun Indicadores(estado: ResumenUiState, columnas: Int, onVerAlertas: () -> Unit) {
    val normal = EstadoSilo.NORMAL.colores()
    val advertencia = EstadoSilo.ADVERTENCIA.colores()
    val critico = EstadoSilo.CRITICO.colores()

    val indicadores = listOf(
        DatosKpi(
            "Silos monitoreados", estado.totalSilos, Icons.Filled.Storage,
            MaterialTheme.colorScheme.onPrimaryContainer, MaterialTheme.colorScheme.primaryContainer
        ),
        DatosKpi("En estado normal", estado.normales, Icons.Filled.CheckCircle, normal.principal, normal.contenedor),
        DatosKpi(
            "En advertencia", estado.advertencias, Icons.Filled.Warning,
            advertencia.principal, advertencia.contenedor, abreAlertas = true
        ),
        DatosKpi(
            "En nivel crítico", estado.criticos, Icons.Filled.Error,
            critico.principal, critico.contenedor, abreAlertas = true
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        indicadores.chunked(columnas).forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                fila.forEach { kpi ->
                    KpiCard(
                        titulo = kpi.titulo,
                        valor = kpi.valor,
                        icono = kpi.icono,
                        colorIcono = kpi.color,
                        colorFondoIcono = kpi.fondo,
                        modifier = Modifier.weight(1f),
                        onClick = if (kpi.abreAlertas) onVerAlertas else null
                    )
                }
            }
        }
    }
}

private data class DatosKpi(
    val titulo: String,
    val valor: Int,
    val icono: ImageVector,
    val color: Color,
    val fondo: Color,
    val abreAlertas: Boolean = false
)

@Composable
private fun TarjetaDisponibilidad(estado: ResumenUiState) {
    val estadoGlobal = CalculosSilo.estado(estado.porcentajeGlobal)
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Alimento disponible en todas las granjas", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(Formatos.toneladas(estado.disponibleKg), style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.width(8.dp))
                Text(
                    "de ${Formatos.toneladas(estado.capacidadKg)} (${Formatos.porcentaje(estado.porcentajeGlobal)})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            BarraNivel(porcentaje = estado.porcentajeGlobal, estado = estadoGlobal, alto = 12.dp)
        }
    }
}

@Composable
private fun PrioridadReposicion(silos: List<SiloItem>, onVerSilo: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TituloSeccion("Prioridad de reposición")
        ElevatedCard(Modifier.fillMaxWidth()) {
            if (silos.isEmpty()) {
                EstadoVacio(
                    icono = Icons.Filled.CheckCircle,
                    titulo = "Sin reposiciones urgentes",
                    descripcion = "Todos los silos están sobre el 30 % y con más de 5 días de alimento."
                )
            } else {
                silos.forEachIndexed { indice, silo ->
                    if (indice > 0) HorizontalDivider()
                    ListItem(
                        headlineContent = { Text(silo.nombre, style = MaterialTheme.typography.titleSmall) },
                        supportingContent = {
                            Text("${silo.granjaNombre} · ${Formatos.diasRestantes(silo.diasRestantes)}")
                        },
                        leadingContent = {
                            SiloIndicador(
                                porcentaje = silo.porcentaje,
                                estado = silo.estado,
                                mostrarTexto = false,
                                modifier = Modifier.size(width = 30.dp, height = 44.dp)
                            )
                        },
                        trailingContent = {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(Formatos.porcentaje(silo.porcentaje), style = MaterialTheme.typography.titleSmall)
                                Spacer(Modifier.height(2.dp))
                                EstadoChip(silo.estado)
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { onVerSilo(silo.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EstadoPorGranja(granjas: List<ResumenGranja>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TituloSeccion("Estado por granja")
        granjas.forEach { granja ->
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(granja.nombre, style = MaterialTheme.typography.titleMedium)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Place,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "${granja.ubicacion} · ${granja.totalSilos} silos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        EstadoChip(granja.peorEstado)
                    }
                    BarraNivel(porcentaje = granja.porcentaje, estado = CalculosSilo.estado(granja.porcentaje))
                    Text(
                        text = "Llenado promedio ${Formatos.porcentaje(granja.porcentaje)} · " +
                            "${granja.criticos} crítico(s) · ${granja.advertencias} en advertencia",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

// ---------------------------- Previews (Guía 9) ----------------------------

@Preview(name = "Compact", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun PreviewResumenCompacto() {
    SiloMonitorTheme {
        ResumenContenido(DatosPreview.resumen, WindowWidthSizeClass.Compact, {}, {}, {})
    }
}

@Preview(name = "Medium", widthDp = 700, heightDp = 900, showBackground = true)
@Composable
private fun PreviewResumenMediano() {
    SiloMonitorTheme {
        ResumenContenido(DatosPreview.resumen, WindowWidthSizeClass.Medium, {}, {}, {})
    }
}

@Preview(name = "Expanded", widthDp = 1100, heightDp = 800, showBackground = true)
@Composable
private fun PreviewResumenExpandido() {
    SiloMonitorTheme(darkTheme = true) {
        ResumenContenido(DatosPreview.resumen, WindowWidthSizeClass.Expanded, {}, {}, {})
    }
}
