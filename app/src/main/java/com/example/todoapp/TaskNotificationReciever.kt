package com.example.todoapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class TaskNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskName = intent.getStringExtra("TASK_NAME") ?: "Tienes una tarea pendiente"

        val channelId = "task_reminders"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal de notificación para Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Recordatorios de Tareas",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

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

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.applogo) // Puedes cambiarlo por R.drawable.cutlogoapp
            .setContentTitle("Recordatorio de Tarea")
            .setContentText("Hoy tienes: $taskName")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // Vincula el click con la Activity
            .build()

        notificationManager.notify(taskName.hashCode(), notification)
    }
}