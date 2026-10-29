package com.example.silomonitor.ui.utils

import android.app.Activity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Calcula el tamaño de ventana actual (Guía 9) para adaptar la interfaz:
 * Compact (celular), Medium (celular horizontal / tablet pequeña) y Expanded (tablet).
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun obtenerWindowSizeClass(): WindowSizeClass {
    val activity = LocalContext.current as Activity
    return calculateWindowSizeClass(activity)
}

/** Atajos legibles para usar en las pantallas. */
val WindowSizeClass.esCompacta: Boolean
    get() = widthSizeClass == WindowWidthSizeClass.Compact

val WindowSizeClass.esExpandida: Boolean
    get() = widthSizeClass == WindowWidthSizeClass.Expanded

/** Número de columnas para grillas según el ancho disponible. */
val WindowSizeClass.columnas: Int
    get() = when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> 1
        WindowWidthSizeClass.Medium -> 2
        else -> 3
    }
