package com.example.silomonitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.silomonitor.navigation.AppNavigation
import com.example.silomonitor.ui.theme.SiloMonitorTheme
import com.example.silomonitor.ui.utils.obtenerWindowSizeClass
import com.example.silomonitor.viewmodel.MainViewModel

/** Única Activity de la app: todo se construye con Jetpack Compose. */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // ViewModel principal con el estado global y los eventos de navegación
            val mainViewModel: MainViewModel = viewModel()
            val temaOscuro by mainViewModel.temaOscuro.collectAsState()

            SiloMonitorTheme(darkTheme = temaOscuro ?: isSystemInDarkTheme()) {
                val windowSizeClass = obtenerWindowSizeClass()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        mainViewModel = mainViewModel,
                        windowSizeClass = windowSizeClass
                    )
                }
            }
        }
    }
}
