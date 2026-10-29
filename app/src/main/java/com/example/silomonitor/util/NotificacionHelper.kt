package com.example.silomonitor.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.silomonitor.MainActivity
import com.example.silomonitor.R

/**
 * Recurso nativo: notificaciones locales del sistema.
 * Se usan para avisar cuando un silo baja a nivel de advertencia o crítico.
 */
object NotificacionHelper {

    const val CANAL_ALERTAS = "alertas_silos"

    /** Android 8+ exige un canal de notificaciones. Se crea al iniciar la app. */
    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL_ALERTAS,
                context.getString(R.string.canal_alertas_nombre),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.canal_alertas_descripcion)
                enableVibration(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(canal)
        }
    }

    /** En Android 13+ el usuario debe otorgar POST_NOTIFICATIONS en tiempo de ejecución. */
    fun tienePermiso(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission") // el permiso se verifica justo antes con tienePermiso()
    fun mostrarAlerta(context: Context, id: Int, titulo: String, mensaje: String) {
        if (!tienePermiso(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificacion = NotificationCompat.Builder(context, CANAL_ALERTAS)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(id, notificacion)
        } catch (e: SecurityException) {
            // El usuario revocó el permiso mientras la app estaba abierta: se ignora sin cerrar la app
        }
    }
}
