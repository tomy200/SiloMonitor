package com.example.silomonitor.model

/**
 * Resultado de la autenticación modelado con una sealed class (Guía 5):
 * un conjunto cerrado de estados posibles que se maneja con `when`.
 */
sealed class ResultadoLogin {
    data object Autenticando : ResultadoLogin()
    data class Exito(val sesion: Sesion) : ResultadoLogin()
    data class Error(val mensaje: String) : ResultadoLogin()
}
