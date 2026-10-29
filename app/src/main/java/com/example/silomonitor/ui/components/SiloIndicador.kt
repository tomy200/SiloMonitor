package com.example.silomonitor.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.silomonitor.model.EstadoSilo
import com.example.silomonitor.ui.theme.colores
import kotlin.math.roundToInt

/**
 * Indicador gráfico de un silo dibujado con Canvas.
 * El alimento "sube" o "baja" con animación cuando cambia el porcentaje,
 * y el color cambia según el estado (verde, ámbar o rojo).
 */
@Composable
fun SiloIndicador(
    porcentaje: Double,
    estado: EstadoSilo,
    modifier: Modifier = Modifier,
    mostrarTexto: Boolean = true
) {
    val nivel by animateFloatAsState(
        targetValue = (porcentaje / 100.0).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1_200, easing = FastOutSlowInEasing),
        label = "nivel_silo"
    )
    val colorRelleno by animateColorAsState(estado.colores().principal, tween(800), label = "color_silo")
    val colorContorno = MaterialTheme.colorScheme.outline
    val colorFondo = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier.semantics {
            contentDescription = "Silo al ${porcentaje.roundToInt()} por ciento, estado ${estado.etiqueta}"
        },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val ancho = size.width
            val alto = size.height
            val techo = alto * 0.18f
            val cono = alto * 0.12f
            val izquierda = ancho * 0.06f
            val derecha = ancho * 0.94f

            // Silueta: techo cónico, cuerpo cilíndrico y fondo en forma de tolva
            val silueta = Path().apply {
                moveTo(ancho / 2f, 0f)
                lineTo(derecha, techo)
                lineTo(derecha, alto - cono)
                lineTo(ancho / 2f, alto)
                lineTo(izquierda, alto - cono)
                lineTo(izquierda, techo)
                close()
            }

            drawPath(silueta, colorFondo)
            clipPath(silueta) {
                val altoRelleno = alto * nivel
                drawRect(
                    color = colorRelleno,
                    topLeft = Offset(0f, alto - altoRelleno),
                    size = Size(ancho, altoRelleno)
                )
            }
            // Anillos del silo
            for (i in 1..3) {
                val y = techo + (alto - cono - techo) * i / 4f
                drawLine(
                    color = colorContorno.copy(alpha = 0.35f),
                    start = Offset(izquierda, y),
                    end = Offset(derecha, y),
                    strokeWidth = 1.dp.toPx()
                )
            }
            drawPath(silueta, colorContorno, style = Stroke(width = 2.dp.toPx(), join = StrokeJoin.Round))
        }

        if (mostrarTexto) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
            ) {
                Text(
                    text = "${porcentaje.roundToInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
