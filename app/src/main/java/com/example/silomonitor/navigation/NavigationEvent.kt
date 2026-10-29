package com.example.silomonitor.navigation

/** Representa los distintos tipos de eventos de navegación (Guía 10). */
sealed class NavigationEvent {

    /**
     * Evento para navegar a un destino específico.
     * @param route destino al que se navega.
     * @param popUpToRoute si no es null, se eliminan de la pila las pantallas hasta esta ruta.
     * @param inclusive si es true, también se elimina [popUpToRoute].
     * @param singleTop evita copias del mismo destino en la parte superior de la pila.
     * @param limpiarPila borra toda la pila (login / cerrar sesión).
     * @param guardarEstado guarda y restaura el estado al cambiar de pestaña.
     */
    data class NavigateTo(
        val route: Screen,
        val popUpToRoute: Screen? = null,
        val inclusive: Boolean = false,
        val singleTop: Boolean = false,
        val limpiarPila: Boolean = false,
        val guardarEstado: Boolean = false
    ) : NavigationEvent()

    /** Volver a la pantalla anterior de la pila. */
    data object PopBackStack : NavigationEvent()

    /** Navegar "hacia arriba" en la jerarquía de la app. */
    data object NavigateUp : NavigationEvent()
}
