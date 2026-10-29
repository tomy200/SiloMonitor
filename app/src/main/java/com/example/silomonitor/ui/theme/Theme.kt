package com.example.silomonitor.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val EsquemaClaro = lightColorScheme(
    primary = VerdeCampo,
    onPrimary = SobreVerdeCampo,
    primaryContainer = VerdeCampoContenedor,
    onPrimaryContainer = SobreVerdeCampoContenedor,
    secondary = Trigo,
    onSecondary = SobreTrigo,
    secondaryContainer = TrigoContenedor,
    onSecondaryContainer = SobreTrigoContenedor,
    tertiary = Acero,
    onTertiary = SobreAcero,
    tertiaryContainer = AceroContenedor,
    onTertiaryContainer = SobreAceroContenedor,
    background = FondoClaro,
    onBackground = SobreFondoClaro,
    surface = FondoClaro,
    onSurface = SobreFondoClaro,
    surfaceVariant = VarianteClara,
    onSurfaceVariant = SobreVarianteClara,
    outline = ContornoClaro,
    outlineVariant = ContornoVarianteClaro,
    surfaceDim = Color(0xFFD7DBD3),
    surfaceBright = FondoClaro,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF1F5EC),
    surfaceContainer = Color(0xFFEBEFE6),
    surfaceContainerHigh = Color(0xFFE6E9E1),
    surfaceContainerHighest = Color(0xFFE0E4DB)
)

private val EsquemaOscuro = darkColorScheme(
    primary = VerdeCampoOscuro,
    onPrimary = SobreVerdeCampoOscuro,
    primaryContainer = VerdeCampoContenedorOscuro,
    onPrimaryContainer = SobreVerdeCampoContenedorOscuro,
    secondary = TrigoOscuro,
    onSecondary = SobreTrigoOscuro,
    secondaryContainer = TrigoContenedorOscuro,
    onSecondaryContainer = SobreTrigoContenedorOscuro,
    tertiary = AceroOscuro,
    onTertiary = SobreAceroOscuro,
    tertiaryContainer = AceroContenedorOscuro,
    onTertiaryContainer = SobreAceroContenedorOscuro,
    background = FondoOscuro,
    onBackground = SobreFondoOscuro,
    surface = FondoOscuro,
    onSurface = SobreFondoOscuro,
    surfaceVariant = VarianteOscura,
    onSurfaceVariant = SobreVarianteOscura,
    outline = ContornoOscuro,
    outlineVariant = ContornoVarianteOscuro,
    surfaceDim = FondoOscuro,
    surfaceBright = Color(0xFF353A35),
    surfaceContainerLowest = Color(0xFF0B0F0B),
    surfaceContainerLow = Color(0xFF181D18),
    surfaceContainer = Color(0xFF1C211C),
    surfaceContainerHigh = Color(0xFF262B26),
    surfaceContainerHighest = Color(0xFF313631)
)

/**
 * Tema Material 3 de la app. No usa colores dinámicos (Android 12+) para mantener
 * la identidad visual y los colores de estado siempre reconocibles.
 */
@Composable
fun SiloMonitorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro
    val estadoColores = if (darkTheme) EstadoColoresOscuro else EstadoColoresClaro

    // Íconos de la barra de estado legibles según el tema elegido en la app
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val controlador = WindowCompat.getInsetsController(window, view)
            controlador.isAppearanceLightStatusBars = !darkTheme
            controlador.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalEstadoColores provides estadoColores) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
