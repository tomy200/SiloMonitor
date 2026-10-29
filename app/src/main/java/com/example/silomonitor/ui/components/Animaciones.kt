package com.example.silomonitor.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

/** Animación de "sacudida" horizontal: retroalimentación visual cuando un formulario es inválido. */
suspend fun Animatable<Float, AnimationVector1D>.sacudir() {
    val desplazamientos = listOf(22f, -18f, 14f, -10f, 6f, -3f, 0f)
    for (desplazamiento in desplazamientos) {
        animateTo(desplazamiento, animationSpec = tween(durationMillis = 45))
    }
}

/**
 * Devuelve un Animatable que se sacude cada vez que cambia [disparador]
 * (por ejemplo, el contador de intentos fallidos del UiState).
 * Se aplica con Modifier.graphicsLayer { translationX = sacudida.value }.
 */
@Composable
fun rememberSacudida(disparador: Int): Animatable<Float, AnimationVector1D> {
    val animacion = remember { Animatable(0f) }
    LaunchedEffect(disparador) {
        if (disparador > 0) animacion.sacudir()
    }
    return animacion
}
