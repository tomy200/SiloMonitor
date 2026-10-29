package com.example.silomonitor.util

import java.security.MessageDigest

/** Utilidades de seguridad: las contraseñas nunca se guardan en texto plano. */
object Seguridad {

    fun sha256(texto: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(texto.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}
