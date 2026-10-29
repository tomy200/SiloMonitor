package com.example.silomonitor.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/** Patrones de vibración usados como retroalimentación háptica. */
enum class TipoVibracion { EXITO, ERROR, ALERTA }

/**
 * Recurso nativo: vibrador del dispositivo.
 * Confirma acciones (guardar), avisa errores de validación y alertas.
 */
object VibracionHelper {

    private fun obtenerVibrador(context: Context): Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

    fun vibrar(context: Context, tipo: TipoVibracion) {
        val vibrador = obtenerVibrador(context) ?: return
        if (!vibrador.hasVibrator()) return

        val efecto = when (tipo) {
            TipoVibracion.EXITO -> VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE)
            TipoVibracion.ERROR -> VibrationEffect.createWaveform(longArrayOf(0, 60, 80, 60), -1)
            TipoVibracion.ALERTA -> VibrationEffect.createWaveform(longArrayOf(0, 250, 120, 250), -1)
        }
        vibrador.vibrate(efecto)
    }
}
