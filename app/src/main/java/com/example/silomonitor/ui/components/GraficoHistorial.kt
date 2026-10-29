package com.example.silomonitor.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.example.silomonitor.domain.CalculosSilo
import com.example.silomonitor.model.EstadoSilo
import com.example.silomonitor.model.PuntoNivel
import com.example.silomonitor.ui.theme.colores
import com.example.silomonitor.util.Formatos

/**
 * Gráfico de línea del historial de nivel (%), dibujado con Canvas.
 * Incluye las líneas de umbral (30 % advertencia y 20 % crítico) y una animación
 * que "dibuja" la línea de izquierda a derecha al abrir la pantalla.
 */
@Composable
fun GraficoHistorial(puntos: List<PuntoNivel>, modifier: Modifier = Modifier) {
    if (puntos.size < 2) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text(
                "Aún no hay datos suficientes para el gráfico",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val medidorTexto = rememberTextMeasurer()
    val progreso = remember { Animatable(0f) }
    LaunchedEffect(puntos.size) {
        progreso.snapTo(0f)
        progreso.animateTo(1f, tween(durationMillis = 1_400, easing = FastOutSlowInEasing))
    }

    val colorLinea = MaterialTheme.colorScheme.primary
    val colorArea = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
    val colorAdvertencia = EstadoSilo.ADVERTENCIA.colores().principal
    val colorCritico = EstadoSilo.CRITICO.colores().principal
    val colorGrilla = MaterialTheme.colorScheme.outlineVariant
    val estiloTexto = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)

    Canvas(
        modifier = modifier.semantics {
            contentDescription = "Gráfico del nivel del silo en los últimos días. " +
                "Nivel actual ${puntos.last().porcentaje.toInt()} por ciento."
        }
    ) {
        val margenIzquierdo = 36.dp.toPx()
        val margenInferior = 20.dp.toPx()
        val margenSuperior = 8.dp.toPx()
        val ancho = size.width - margenIzquierdo
        val alto = size.height - margenInferior - margenSuperior

        val fechaMin = puntos.first().fecha
        val fechaMax = puntos.last().fecha
        val rango = (fechaMax - fechaMin).coerceAtLeast(1L)

        fun x(fecha: Long): Float = margenIzquierdo + ancho * ((fecha - fechaMin).toFloat() / rango)
        fun y(porcentaje: Float): Float = margenSuperior + alto * (1f - porcentaje / 100f)

        // Grilla horizontal con etiquetas 0 %, 50 %, 100 %
        listOf(0f, 50f, 100f).forEach { valor ->
            drawLine(colorGrilla, Offset(margenIzquierdo, y(valor)), Offset(size.width, y(valor)), 1.dp.toPx())
            val etiqueta = medidorTexto.measure("${valor.toInt()}%", estiloTexto)
            drawText(etiqueta, topLeft = Offset(0f, y(valor) - etiqueta.size.height / 2f))
        }

        // Umbrales del caso
        val guiones = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))
        val yAdv = y(CalculosSilo.UMBRAL_ADVERTENCIA.toFloat())
        val yCrit = y(CalculosSilo.UMBRAL_CRITICO.toFloat())
        drawLine(colorAdvertencia, Offset(margenIzquierdo, yAdv), Offset(size.width, yAdv), 1.5.dp.toPx(), pathEffect = guiones)
        drawLine(colorCritico, Offset(margenIzquierdo, yCrit), Offset(size.width, yCrit), 1.5.dp.toPx(), pathEffect = guiones)

        // Línea y área hasta el punto que indica la animación
        val limite = fechaMin + (rango * progreso.value).toLong()
        val linea = Path()
        val area = Path()
        var ultimo: Offset? = null
        puntos.filter { it.fecha <= limite }.forEachIndexed { indice, punto ->
            val o = Offset(x(punto.fecha), y(punto.porcentaje))
            if (indice == 0) {
                linea.moveTo(o.x, o.y)
                area.moveTo(o.x, y(0f))
                area.lineTo(o.x, o.y)
            } else {
                linea.lineTo(o.x, o.y)
                area.lineTo(o.x, o.y)
            }
            ultimo = o
        }
        ultimo?.let { punto ->
            area.lineTo(punto.x, y(0f))
            area.close()
            drawPath(area, colorArea)
            drawPath(
                linea,
                colorLinea,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            drawCircle(colorLinea, radius = 4.dp.toPx(), center = punto)
        }

        // Fechas de inicio y fin
        val yFechas = size.height - margenInferior + 4.dp.toPx()
        val inicio = medidorTexto.measure(Formatos.fechaCorta(fechaMin), estiloTexto)
        drawText(inicio, topLeft = Offset(margenIzquierdo, yFechas))
        val fin = medidorTexto.measure(Formatos.fechaCorta(fechaMax), estiloTexto)
        drawText(fin, topLeft = Offset(size.width - fin.size.width, yFechas))
    }
}
