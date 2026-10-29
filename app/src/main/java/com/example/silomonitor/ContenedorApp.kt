package com.example.silomonitor

import android.content.Context
import com.example.silomonitor.data.local.SiloDatabase
import com.example.silomonitor.data.preferences.PreferenciasDataStore
import com.example.silomonitor.data.remote.SiloApiSimulada
import com.example.silomonitor.repository.SiloRepository
import com.example.silomonitor.repository.UsuarioRepository
import com.example.silomonitor.util.Retroalimentacion

/**
 * Contenedor de dependencias (inyección manual): crea una sola vez la base de datos,
 * las preferencias y los repositorios, y los comparte con todos los ViewModels.
 */
class ContenedorApp(context: Context) {

    private val appContext = context.applicationContext

    val baseDatos: SiloDatabase by lazy { SiloDatabase.obtener(appContext) }

    val preferencias: PreferenciasDataStore by lazy { PreferenciasDataStore(appContext) }

    private val api: SiloApiSimulada by lazy { SiloApiSimulada() }

    val siloRepository: SiloRepository by lazy {
        SiloRepository(appContext, baseDatos, api, preferencias)
    }

    val usuarioRepository: UsuarioRepository by lazy {
        UsuarioRepository(baseDatos.usuarioDao(), preferencias)
    }

    val retroalimentacion: Retroalimentacion by lazy {
        Retroalimentacion(appContext, preferencias)
    }
}
