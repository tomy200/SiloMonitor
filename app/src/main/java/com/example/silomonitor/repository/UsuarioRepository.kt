package com.example.silomonitor.repository

import com.example.silomonitor.data.local.dao.UsuarioDao
import com.example.silomonitor.data.local.entity.UsuarioEntity
import com.example.silomonitor.data.preferences.PreferenciasDataStore
import com.example.silomonitor.model.ResultadoLogin
import com.example.silomonitor.model.Rol
import com.example.silomonitor.model.Sesion
import com.example.silomonitor.util.Seguridad
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

/** Autenticación contra los usuarios pre-registrados y manejo de la sesión. */
class UsuarioRepository(
    private val usuarioDao: UsuarioDao,
    private val preferencias: PreferenciasDataStore
) {

    val sesion: Flow<Sesion?> = preferencias.sesion

    fun observarUsuario(correo: String): Flow<UsuarioEntity?> = usuarioDao.observar(correo)

    /**
     * Simula la verificación contra un servidor (Guía 5: suspend + delay).
     * El mensaje de error es genérico para no revelar qué correos existen.
     */
    suspend fun autenticar(correo: String, clave: String, recordar: Boolean): ResultadoLogin {
        delay(1_200)
        val usuario = usuarioDao.buscarPorCorreo(correo.trim().lowercase())
        if (usuario == null || usuario.claveHash != Seguridad.sha256(clave)) {
            return ResultadoLogin.Error("Correo o contraseña incorrectos")
        }
        val sesion = Sesion(
            correo = usuario.correo,
            nombre = usuario.nombre,
            rol = Rol.desde(usuario.rol),
            recordar = recordar
        )
        preferencias.guardarSesion(sesion)
        return ResultadoLogin.Exito(sesion)
    }

    /**
     * Crea una cuenta nueva con el cargo elegido y deja la sesión iniciada.
     * El correo se guarda en minúsculas para que el login no distinga mayúsculas.
     */
    suspend fun registrar(nombre: String, correo: String, clave: String, rol: Rol): ResultadoLogin {
        delay(1_000) // simula la llamada al servidor
        val correoNormalizado = correo.trim().lowercase()
        if (usuarioDao.buscarPorCorreo(correoNormalizado) != null) {
            return ResultadoLogin.Error("Ya existe una cuenta con ese correo")
        }
        val nombreLimpio = nombre.trim().replace(Regex("\\s+"), " ")
        usuarioDao.insertarNuevo(
            UsuarioEntity(
                correo = correoNormalizado,
                nombre = nombreLimpio,
                rol = rol.name,
                claveHash = Seguridad.sha256(clave)
            )
        )
        val sesion = Sesion(correo = correoNormalizado, nombre = nombreLimpio, rol = rol, recordar = true)
        preferencias.guardarSesion(sesion)
        return ResultadoLogin.Exito(sesion)
    }

    suspend fun actualizarFoto(correo: String, fotoUri: String?) {
        usuarioDao.actualizarFoto(correo, fotoUri)
    }

    suspend fun cerrarSesion() {
        preferencias.cerrarSesion()
    }
}
