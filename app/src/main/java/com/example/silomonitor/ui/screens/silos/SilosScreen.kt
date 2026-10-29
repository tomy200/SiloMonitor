package com.example.silomonitor.ui.screens.silos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.silomonitor.model.EstadoSilo
import com.example.silomonitor.model.EventoUi
import com.example.silomonitor.model.SilosUiState
import com.example.silomonitor.ui.components.EstadoVacio
import com.example.silomonitor.ui.components.SiloCard
import com.example.silomonitor.ui.theme.icono
import com.example.silomonitor.ui.utils.columnas
import com.example.silomonitor.viewmodel.SilosViewModel
import kotlinx.coroutines.launch

/**
 * Lista de silos organizada por granja. Se adapta al ancho (1, 2 o 3 columnas),
 * permite filtrar por granja y estado, y "deslizar para actualizar" desde la API simulada.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SilosScreen(
    windowSizeClass: WindowSizeClass,
    snackbarHostState: SnackbarHostState,
    onVerSilo: (String) -> Unit,
    viewModel: SilosViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    // derivedStateOf: solo recompone cuando cambia el resultado (no en cada scroll)
    val mostrarBotonArriba by remember {
        derivedStateOf { gridState.firstVisibleItemIndex > 2 }
    }

    LaunchedEffect(Unit) {
        viewModel.eventos.collect { evento ->
            if (evento is EventoUi.MostrarMensaje) snackbarHostState.showSnackbar(evento.mensaje)
        }
    }

    Column(Modifier.fillMaxSize()) {
        // ---------- Filtros ----------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.FilterList, contentDescription = "Filtros", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            FiltroChip("Todas las granjas", estado.granjaSeleccionada == null) { viewModel.seleccionarGranja(null) }
            estado.granjas.forEach { granja ->
                FiltroChip(granja.nombre.removePrefix("Granja "), estado.granjaSeleccionada == granja.id) {
                    viewModel.seleccionarGranja(granja.id)
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FiltroChip("Todos los estados", estado.estadoSeleccionado == null) { viewModel.seleccionarEstado(null) }
            EstadoSilo.entries.forEach { opcion ->
                FilterChip(
                    selected = estado.estadoSeleccionado == opcion,
                    onClick = { viewModel.seleccionarEstado(opcion) },
                    label = { Text(opcion.etiqueta) },
                    leadingIcon = { Icon(opcion.icono(), contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                )
            }
        }

        ListaSilos(
            estado = estado,
            columnas = windowSizeClass.columnas,
            gridState = gridState,
            mostrarBotonArriba = mostrarBotonArriba,
            onSincronizar = viewModel::sincronizar,
            onLimpiarFiltros = viewModel::limpiarFiltros,
            onVerSilo = onVerSilo,
            onVolverArriba = { scope.launch { gridState.animateScrollToItem(0) } },
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
        )
    }
}

/** Lista con pull-to-refresh (separada en su propia función para mantener legible la pantalla). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListaSilos(
    estado: SilosUiState,
    columnas: Int,
    gridState: LazyGridState,
    mostrarBotonArriba: Boolean,
    onSincronizar: () -> Unit,
    onLimpiarFiltros: () -> Unit,
    onVerSilo: (String) -> Unit,
    onVolverArriba: () -> Unit,
    modifier: Modifier = Modifier
) {
    PullToRefreshBox(
        isRefreshing = estado.sincronizando,
        onRefresh = onSincronizar,
        modifier = modifier
    ) {
        when {
            estado.cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            estado.silos.isEmpty() -> EstadoSinResultados(onLimpiarFiltros)
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(columnas),
                state = gridState,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = if (estado.hayFiltros) {
                            "${estado.silos.size} de ${estado.totalSinFiltro} silos"
                        } else {
                            "${estado.totalSinFiltro} silos · desliza hacia abajo para actualizar"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(estado.silos, key = { it.id }) { silo ->
                    SiloCard(
                        silo = silo,
                        onClick = { onVerSilo(silo.id) },
                        // Anima la aparición y reordenamiento al cambiar filtros
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }

        // Botón "volver arriba" que aparece con animación al bajar en la lista
        AnimatedVisibility(
            visible = mostrarBotonArriba,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            SmallFloatingActionButton(onClick = onVolverArriba) {
                Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Volver arriba")
            }
        }
    }
}

@Composable
private fun EstadoSinResultados(onLimpiarFiltros: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()), // permite el gesto de pull-to-refresh
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EstadoVacio(
            icono = Icons.Filled.FilterList,
            titulo = "No hay silos con estos filtros",
            descripcion = "Prueba con otra granja u otro estado."
        )
        TextButton(onClick = onLimpiarFiltros) { Text("Limpiar filtros") }
    }
}

@Composable
private fun FiltroChip(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = seleccionado,
        onClick = onClick,
        label = { Text(texto) },
        leadingIcon = if (seleccionado) {
            { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
        } else {
            null
        }
    )
}
