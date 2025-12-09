package com.example.todoapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class InactivityNotifier(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "inactivity_channel"
        const val NOTIFICATION_ID = 1
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recordatorio de Inactividad"
            val descriptionText = "Canal para notificaciones de inactividad."
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showNotification()
    {
        // 1. Crear el Intent que abre tu MainActivity
        val intent = Intent(context, MainActivity::class.java).apply {
            // Estos flags aseguran que si la app ya está abierta, la traiga al frente
            // o reinicie la tarea según prefieras.
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // 2. Crear el PendingIntent (Necesario para Android 12+)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE // Importante para seguridad en versiones nuevas
        )
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.applogo) // Asegúrate de tener un ícono en drawable
            .setContentTitle("¿Te has olvidado de algo?")
            .setContentText("Hace rato que no añades ninguna tarea.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // La notificación desaparece al pulsarla
            .setContentIntent(pendingIntent) // Vincula el click con la Activity

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }
}
