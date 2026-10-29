package com.example.silomonitor.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.silomonitor.R
import com.example.silomonitor.navigation.Screen
import com.example.silomonitor.viewmodel.SplashViewModel
import kotlinx.coroutines.launch

/**
 * Pantalla de bienvenida: animación de entrada del logo (escala con rebote + fundido)
 * mientras se preparan los datos locales.
 */
@Composable
fun SplashScreen(
    onTerminado: (Screen) -> Unit,
    viewModel: SplashViewModel = viewModel()
) {
    val destino by viewModel.destino.collectAsState()
    val escala = remember { Animatable(0.5f) }
    val opacidad = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            escala.animateTo(
                1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        opacidad.animateTo(1f, animationSpec = tween(durationMillis = 900))
    }

    LaunchedEffect(destino) {
        destino?.let(onTerminado)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.background)
                )
            )
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_silo),
                contentDescription = "Logo SiloMonitor",
                modifier = Modifier
                    .size(170.dp)
                    .graphicsLayer {
                        scaleX = escala.value
                        scaleY = escala.value
                        alpha = opacidad.value
                    }
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "SiloMonitor",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.graphicsLayer { alpha = opacidad.value }
            )
            Text(
                text = "Monitoreo de alimento en silos",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.graphicsLayer { alpha = opacidad.value }
            )
            Spacer(Modifier.height(40.dp))
            CircularProgressIndicator()
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Preparando datos locales…",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
