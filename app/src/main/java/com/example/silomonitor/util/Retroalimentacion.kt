package com.example.silomonitor.util

import android.content.Context
import com.example.silomonitor.data.preferences.PreferenciasDataStore
import kotlinx.coroutines.flow.first

/**
 * Retroalimentación háptica que respeta la preferencia "Vibración" del perfil.
 * Los ViewModels la usan para no depender directamente del servicio de vibración.
 */
class Retroalimentacion(
    private val context: Context,
    private val preferencias: PreferenciasDataStore
) {
    suspend fun exito() = vibrarSiEstaActiva(TipoVibracion.EXITO)
    suspend fun error() = vibrarSiEstaActiva(TipoVibracion.ERROR)
    suspend fun alerta() = vibrarSiEstaActiva(TipoVibracion.ALERTA)

    /** Vibra aunque la preferencia esté desactivada (para probarla al activarla). */
    fun probar() = VibracionHelper.vibrar(context, TipoVibracion.EXITO)

    private suspend fun vibrarSiEstaActiva(tipo: TipoVibracion) {
        if (preferencias.vibracionActiva.first()) {
            VibracionHelper.vibrar(context, tipo)
        }
    }
}
