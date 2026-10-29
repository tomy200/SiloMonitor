package com.example.silomonitor.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.silomonitor.SiloMonitorApp
import com.example.silomonitor.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Prepara la app al abrirla: carga los datos simulados (solo la primera vez)
 * y decide si ir al login o directamente al resumen (sesión recordada).
 */
class SplashViewModel(application: Application) : AndroidViewModel(application) {

    private val contenedor = (application as SiloMonitorApp).contenedor

    private val _destino = MutableStateFlow<Screen?>(null)
    val destino: StateFlow<Screen?> = _destino.asStateFlow()

    init {
        viewModelScope.launch {
            val inicio = System.currentTimeMillis()
            contenedor.siloRepository.inicializarSiEsNecesario()

            val sesion = contenedor.preferencias.sesion.first()
            val siguiente = if (sesion != null && sesion.recordar) {
                Screen.Resumen
            } else {
                if (sesion != null) contenedor.preferencias.cerrarSesion()
                Screen.Login
            }

            // Tiempo mínimo para que se aprecie la animación de entrada
            val transcurrido = System.currentTimeMillis() - inicio
            if (transcurrido < DURACION_MINIMA) delay(DURACION_MINIMA - transcurrido)
            _destino.value = siguiente
        }
    }

    private companion object {
        const val DURACION_MINIMA = 1_800L
    }
}
