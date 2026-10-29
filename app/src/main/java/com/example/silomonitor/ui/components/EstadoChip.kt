package com.example.silomonitor.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.silomonitor.model.EstadoSilo
import com.example.silomonitor.ui.theme.colores
import com.example.silomonitor.ui.theme.icono

/**
 * Etiqueta de estado (Normal / Advertencia / Crítico) con color e ícono.
 * En estado crítico el ícono "pulsa" para llamar la atención.
 */
@Composable
fun EstadoChip(estado: EstadoSilo, modifier: Modifier = Modifier) {
    val colores = estado.colores()
    val fondo by animateColorAsState(colores.contenedor, tween(500), label = "fondo_chip")
    val texto by animateColorAsState(colores.sobreContenedor, tween(500), label = "texto_chip")

    val transicion = rememberInfiniteTransition(label = "pulso_estado")
    val pulso by transicion.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "alpha_pulso"
    )

    Surface(
        color = fondo,
        contentColor = texto,
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = estado.icono(),
                contentDescription = null,
                tint = colores.principal,
                modifier = Modifier
                    .size(16.dp)
                    .graphicsLayer { alpha = if (estado == EstadoSilo.CRITICO) pulso else 1f }
            )
            Spacer(Modifier.width(4.dp))
            Text(estado.etiqueta, style = MaterialTheme.typography.labelMedium)
        }
    }
}
