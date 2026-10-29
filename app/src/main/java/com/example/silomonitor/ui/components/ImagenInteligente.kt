package com.example.silomonitor.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * Componente reutilizable de la Guía 13: muestra la imagen elegida desde la cámara
 * o la galería. Si no hay imagen, muestra un ícono por defecto.
 * - [circular] = true: avatar de perfil.
 * - [circular] = false: vista previa rectangular (evidencia de incidencias).
 */
@Composable
fun ImagenInteligente(
    uri: String?,
    modifier: Modifier = Modifier,
    circular: Boolean = true,
    iconoPorDefecto: ImageVector = Icons.Filled.AccountCircle,
    descripcion: String = "Imagen"
) {
    val forma = if (circular) CircleShape else RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .clip(forma)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), forma),
        contentAlignment = Alignment.Center
    ) {
        // Transición suave al cambiar de imagen
        Crossfade(targetState = uri, animationSpec = tween(500), label = "imagen_inteligente") { actual ->
            if (actual != null) {
                AsyncImage(
                    model = actual,
                    contentDescription = descripcion,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = iconoPorDefecto,
                        contentDescription = "Sin imagen",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }
        }
    }
}
