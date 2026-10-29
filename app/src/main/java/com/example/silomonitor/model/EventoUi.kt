package com.example.silomonitor.model

/** Eventos de una sola vez que un ViewModel envía a su pantalla (mensajes, volver, etc.). */
sealed class EventoUi {
    data class MostrarMensaje(val mensaje: String) : EventoUi()
    data object Volver : EventoUi()
    data object NavegarInicio : EventoUi()
    data object SesionCerrada : EventoUi()
}
