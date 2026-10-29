package com.example.silomonitor.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.silomonitor.model.EstadoSilo

/** Colores semánticos de un estado: color principal, fondo y texto sobre el fondo. */
@Immutable
data class ColoresEstado(
    val principal: Color,
    val contenedor: Color,
    val sobreContenedor: Color
)

@Immutable
data class EstadoColores(
    val normal: ColoresEstado,
    val advertencia: ColoresEstado,
    val critico: ColoresEstado
)

val EstadoColoresClaro = EstadoColores(
    normal = ColoresEstado(Color(0xFF2E7D32), Color(0xFFDCF1DC), Color(0xFF0F3D14)),
    advertencia = ColoresEstado(Color(0xFFB26A00), Color(0xFFFFE9B8), Color(0xFF4A2C00)),
    critico = ColoresEstado(Color(0xFFC62828), Color(0xFFFFDAD6), Color(0xFF5F1412))
)

val EstadoColoresOscuro = EstadoColores(
    normal = ColoresEstado(Color(0xFF7BD58A), Color(0xFF1E4426), Color(0xFFC9F2CF)),
    advertencia = ColoresEstado(Color(0xFFFFC857), Color(0xFF4F3800), Color(0xFFFFE6B0)),
    critico = ColoresEstado(Color(0xFFFF8A80), Color(0xFF6B1A17), Color(0xFFFFDAD6))
)

/** Se entrega desde SiloMonitorTheme para que cambie con el tema claro/oscuro. */
val LocalEstadoColores = staticCompositionLocalOf { EstadoColoresClaro }

@Composable
fun EstadoSilo.colores(): ColoresEstado {
    val paleta = LocalEstadoColores.current
    return when (this) {
        EstadoSilo.NORMAL -> paleta.normal
        EstadoSilo.ADVERTENCIA -> paleta.advertencia
        EstadoSilo.CRITICO -> paleta.critico
    }
}

/** Ícono de cada estado: el color nunca es la única señal (accesibilidad). */
fun EstadoSilo.icono(): ImageVector = when (this) {
    EstadoSilo.NORMAL -> Icons.Filled.CheckCircle
    EstadoSilo.ADVERTENCIA -> Icons.Filled.Warning
    EstadoSilo.CRITICO -> Icons.Filled.Error
}
