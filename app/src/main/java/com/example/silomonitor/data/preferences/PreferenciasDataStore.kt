package com.example.silomonitor.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.silomonitor.model.Rol
import com.example.silomonitor.model.Sesion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// Declaramos el DataStore con nombre "preferencias_silomonitor" (Guía 12)
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferencias_silomonitor")

/**
 * Persistencia de datos pequeños clave-valor con DataStore Preferences:
 * sesión del usuario y preferencias de la app (tema, notificaciones, vibración).
 */
class PreferenciasDataStore(private val context: Context) {

    private object Claves {
        val CORREO = stringPreferencesKey("sesion_correo")
        val NOMBRE = stringPreferencesKey("sesion_nombre")
        val ROL = stringPreferencesKey("sesion_rol")
        val RECORDAR = booleanPreferencesKey("sesion_recordar")
        val TEMA_OSCURO = booleanPreferencesKey("tema_oscuro")
        val NOTIFICACIONES = booleanPreferencesKey("notificaciones_activas")
        val VIBRACION = booleanPreferencesKey("vibracion_activa")
        val SIMULAR_SIN_CONEXION = booleanPreferencesKey("simular_sin_conexion")
        val ULTIMA_SINCRONIZACION = longPreferencesKey("ultima_sincronizacion")
    }

    /** Si el archivo no se puede leer, se usan valores por defecto en vez de cerrar la app. */
    private val datos: Flow<Preferences> = context.dataStore.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }

    // ---------------------------- Sesión ----------------------------

    val sesion: Flow<Sesion?> = datos.map { prefs ->
        val correo = prefs[Claves.CORREO] ?: return@map null
        Sesion(
            correo = correo,
            nombre = prefs[Claves.NOMBRE] ?: "",
            rol = Rol.desde(prefs[Claves.ROL]),
            recordar = prefs[Claves.RECORDAR] ?: false
        )
    }

    suspend fun guardarSesion(sesion: Sesion) {
        context.dataStore.edit { prefs ->
            prefs[Claves.CORREO] = sesion.correo
            prefs[Claves.NOMBRE] = sesion.nombre
            prefs[Claves.ROL] = sesion.rol.name
            prefs[Claves.RECORDAR] = sesion.recordar
        }
    }

    suspend fun cerrarSesion() {
        context.dataStore.edit { prefs ->
            prefs.remove(Claves.CORREO)
            prefs.remove(Claves.NOMBRE)
            prefs.remove(Claves.ROL)
            prefs.remove(Claves.RECORDAR)
        }
    }

    // ---------------------------- Preferencias ----------------------------

    /** null = seguir el tema del sistema. */
    val temaOscuro: Flow<Boolean?> = datos.map { it[Claves.TEMA_OSCURO] }

    val notificacionesActivas: Flow<Boolean> = datos.map { it[Claves.NOTIFICACIONES] ?: true }

    val vibracionActiva: Flow<Boolean> = datos.map { it[Claves.VIBRACION] ?: true }

    /** Opción de demostración: fuerza a la API simulada a fallar para mostrar el modo sin conexión. */
    val simularSinConexion: Flow<Boolean> = datos.map { it[Claves.SIMULAR_SIN_CONEXION] ?: false }

    val ultimaSincronizacion: Flow<Long?> = datos.map { it[Claves.ULTIMA_SINCRONIZACION] }

    suspend fun guardarTemaOscuro(valor: Boolean) {
        context.dataStore.edit { it[Claves.TEMA_OSCURO] = valor }
    }

    suspend fun guardarNotificaciones(valor: Boolean) {
        context.dataStore.edit { it[Claves.NOTIFICACIONES] = valor }
    }

    suspend fun guardarVibracion(valor: Boolean) {
        context.dataStore.edit { it[Claves.VIBRACION] = valor }
    }

    suspend fun guardarSimularSinConexion(valor: Boolean) {
        context.dataStore.edit { it[Claves.SIMULAR_SIN_CONEXION] = valor }
    }

    suspend fun guardarUltimaSincronizacion(fecha: Long) {
        context.dataStore.edit { it[Claves.ULTIMA_SINCRONIZACION] = fecha }
    }
}
