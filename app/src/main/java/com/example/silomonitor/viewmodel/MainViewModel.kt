package com.example.silomonitor.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.silomonitor.SiloMonitorApp
import com.example.silomonitor.model.Sesion
import com.example.silomonitor.navigation.NavigationEvent
import com.example.silomonitor.navigation.Screen
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel principal (Guía 10): centraliza los eventos de navegación y el estado global
 * (tema, sesión y cantidad de alertas activas para el badge de la barra inferior).
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val contenedor = (application as SiloMonitorApp).contenedor

    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    // Expone los eventos como un flujo de solo lectura para que la UI los observe
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()

    val temaOscuro: StateFlow<Boolean?> = contenedor.preferencias.temaOscuro
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val sesion: StateFlow<Sesion?> = contenedor.usuarioRepository.sesion
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val alertasActivas: StateFlow<Int> = contenedor.siloRepository.alertasActivas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    /** Emite el evento de navegación hacia la ruta deseada. */
    fun navigateTo(
        screen: Screen,
        popUpToRoute: Screen? = null,
        inclusive: Boolean = false,
        singleTop: Boolean = false,
        limpiarPila: Boolean = false,
        guardarEstado: Boolean = false
    ) {
        viewModelScope.launch {
            _navigationEvents.emit(
                NavigationEvent.NavigateTo(
                    route = screen,
                    popUpToRoute = popUpToRoute,
                    inclusive = inclusive,
                    singleTop = singleTop,
                    limpiarPila = limpiarPila,
                    guardarEstado = guardarEstado
                )
            )
        }
    }

    /** Cambio de pestaña en la barra inferior / lateral, conservando el estado de cada una. */
    fun navegarPestana(screen: Screen) {
        navigateTo(screen, popUpToRoute = Screen.Resumen, singleTop = true, guardarEstado = true)
    }

    /** Volver atrás. */
    fun navigateBack() {
        viewModelScope.launch { _navigationEvents.emit(NavigationEvent.PopBackStack) }
    }

    /** Navegar hacia arriba (padre). */
    fun navigateUp() {
        viewModelScope.launch { _navigationEvents.emit(NavigationEvent.NavigateUp) }
    }
}
