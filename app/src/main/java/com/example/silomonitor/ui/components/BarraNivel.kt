package com.example.silomonitor.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.silomonitor.model.EstadoSilo
import com.example.silomonitor.ui.theme.colores

/** Barra de nivel animada: el progreso y el color cambian suavemente al actualizarse el silo. */
@Composable
fun BarraNivel(
    porcentaje: Double,
    estado: EstadoSilo,
    modifier: Modifier = Modifier,
    alto: Dp = 10.dp
) {
    val progreso by animateFloatAsState(
        targetValue = (porcentaje / 100.0).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1_000, easing = FastOutSlowInEasing),
        label = "progreso_barra"
    )
    val color by animateColorAsState(estado.colores().principal, tween(600), label = "color_barra")

    LinearProgressIndicator(
        progress = { progreso },
        modifier = modifier
            .fillMaxWidth()
            .height(alto)
            .clip(RoundedCornerShape(alto / 2)),
        color = color,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
        strokeCap = StrokeCap.Round
    )
}
