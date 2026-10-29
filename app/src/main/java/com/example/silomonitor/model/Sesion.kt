package com.example.silomonitor.model

/** Datos de la sesión activa guardados en DataStore. */
data class Sesion(
    val correo: String,
    val nombre: String,
    val rol: Rol,
    val recordar: Boolean
)
