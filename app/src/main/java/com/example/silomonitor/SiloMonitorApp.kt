package com.example.silomonitor

import android.app.Application
import com.example.silomonitor.util.NotificacionHelper

/** Clase Application: se crea antes que cualquier pantalla. */
class SiloMonitorApp : Application() {

    lateinit var contenedor: ContenedorApp
        private set

    override fun onCreate() {
        super.onCreate()
        contenedor = ContenedorApp(this)
        NotificacionHelper.crearCanal(this)
    }
}
