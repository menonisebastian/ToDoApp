package com.example.todoapp.resources

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.example.todoapp.data.model.Tarea
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.collections.forEach

// ============ FUNCIONES ============ //

// ============ EXPORTAR TAREAS ============ //
fun exportarTareas(context: Context, listaTareas: List<Tarea>, listaCompletadas: List<Tarea>) {

    val stringBuilder = StringBuilder()

    // --- HELPER LOCAL PARA FORMATEAR CADA TAREA ---
    fun appendTareaInfo(tarea: Tarea) {
        // 1. Título y Fecha
        stringBuilder.append("• Tarea: ${tarea.texto}")
        if (tarea.fecha.isNotBlank()) {
            stringBuilder.append(" | Fecha: ${formatearFechaParaMostrar(tarea.fecha)}")
        }
        stringBuilder.append("\n")

        // 2. Datos del Pokémon (Solo si existen)
        if (tarea.pokeName.isNotBlank()) {
            stringBuilder.append("    [Datos del Pokémon]\n")
            stringBuilder.append("    Nombre: ${tarea.pokeName}\n")
            stringBuilder.append("    Tipo: ${tarea.pokeType}\n")
            stringBuilder.append("    Stats: ${tarea.pokeStats}\n")
        }

        stringBuilder.append("--------------------------------------------------\n")
    }
    // ---------------------------------------------

    if (listaTareas.isNotEmpty()) {
        stringBuilder.append("=== TAREAS PENDIENTES ===\n\n")
        listaTareas.forEach { tarea ->
            appendTareaInfo(tarea)
        }
        stringBuilder.append("\n")
    }

    if (listaCompletadas.isNotEmpty()) {
        stringBuilder.append("=== TAREAS COMPLETADAS ===\n\n")
        listaCompletadas.forEach { tarea ->
            appendTareaInfo(tarea)
        }
    }

    val texto = stringBuilder.toString()
    val nombreArchivo = "tareas.txt"
    var mensaje = ""

    if (listaTareas.isEmpty() && listaCompletadas.isEmpty()) {
        mensaje = "No hay tareas para exportar"
    } else {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI

                // Buscar si el archivo ya existe
                val selection = "${MediaStore.Downloads.DISPLAY_NAME} = ?"
                val selectionArgs = arrayOf(nombreArchivo)

                var uriExistente: Uri? = null

                resolver.query(contentUri, arrayOf(MediaStore.Downloads._ID), selection, selectionArgs, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID)
                        val id = cursor.getLong(idColumn)
                        uriExistente = ContentUris.withAppendedId(contentUri, id)
                    }
                }

                // Si existe usar URI, si no, crear una nueva (insert)
                val uriFinal = uriExistente ?: run {
                    val values = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, nombreArchivo)
                        put(MediaStore.Downloads.MIME_TYPE, "text/plain")
                        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                    resolver.insert(contentUri, values)
                }

                // Escribir en el archivo (modo "wt" = Write & Truncate para borrar contenido previo)
                uriFinal?.let { uri ->
                    resolver.openOutputStream(uri, "wt")?.use { output ->
                        output.write(texto.toByteArray())
                    }
                    mensaje = if (uriExistente != null) "Archivo actualizado en Descargas" else "Archivo creado en Descargas"
                } ?: run {
                    mensaje = "Error: No se pudo acceder al archivo"
                }

            } else {
                // Lógica para Android 9 o inferior (Legacy) - NO FUNCIONA EN MI DISPOSITIVO
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!dir.exists()) dir.mkdirs()
                val archivo = File(dir, nombreArchivo)
                FileOutputStream(archivo).use { it.write(texto.toByteArray()) }
                mensaje = "Guardado en: ${archivo.absolutePath}"
            }
        } catch (e: Exception) {
            mensaje = "Error al guardar: ${e.message}"
            e.printStackTrace()
        }
    }
    Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
}


// ============ FORMATEO DE FECHA ============ //
fun formatearFechaParaMostrar(fechaIso: String): String {
    if (fechaIso.isBlank()) return ""
    return try {
        // Divide "2025/01/01" y reordena
        val partes = fechaIso.split("/")
        "${partes[2]}/${partes[1]}/${partes[0]}" // Retorna "01/01/2025"
    } catch (e: Exception)
    {
        e.printStackTrace()
        fechaIso // Si falla, devuelve la original
    }
}

fun compararFechaActual(fecha: String): Boolean {
    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    val fechaActual = LocalDate.now()
    return LocalDate.parse(fecha, formatter) <= fechaActual
}

fun formatearStatsPokemon(stats: String): String
{
    val statSlot = stats.split("|")
    var stat = ""

    statSlot.forEach { item ->
        val slot = item.split(": ")
        stat += slot[1].trim() +"\n"
    }
    return stat
}

// ============ PRIORIDAD DE LA TAREA ============ //
fun determinePriority(tarea: Tarea): TaskPriority {
    // 1. Si ya está completada, no importa la fecha. Devolvemos COMPLETED y salimos.
    if (tarea.completada) {
        return TaskPriority.COMPLETED
    }

    return try {
        val dateString = tarea.fecha

        // Opcional: Si no hay fecha, definimos una prioridad por defecto
        if (dateString.isBlank()) return TaskPriority.UNKNOWN

        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        val dueDate = LocalDate.parse(dateString, formatter)
        val today = LocalDate.now()

        val daysUntil = ChronoUnit.DAYS.between(today, dueDate)

        // 2. Evaluamos el resto de prioridades basadas en el tiempo
        when {
            daysUntil < 0 -> TaskPriority.EXPIRED   //Expirada
            daysUntil <= 7 -> TaskPriority.HIGH     //Alta
            daysUntil <= 14 -> TaskPriority.MEDIUM  //Media
            else -> TaskPriority.LOW                //Baja
        }
    } catch (e: Exception) {
        // Solo llegamos aquí si la fecha estaba mal formateada
        e.printStackTrace() // Descomentar para depurar si es necesario
        TaskPriority.UNKNOWN
    }
}

// ============ NOTIFICACIÓN ============ //
fun scheduleTaskNotification(context: Context, taskName: String, taskDate: String) {
    try {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskNotificationReceiver::class.java).apply {
            putExtra("TASK_NAME", taskName)
        }

        // Creamos un ID único basado en el hash del nombre para no sobrescribir alarmas diferentes
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Parseamos la fecha (dd/MM/yyyy)
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        val date = LocalDate.parse(taskDate, formatter)

        // Configuramos la hora a las 9:00 AM
        val dateTime = date.atTime(9, 0, 0)
        val triggerTime = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // Solo programar si la fecha es futura
        if (triggerTime > System.currentTimeMillis()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "No se pudo programar la notificación", Toast.LENGTH_SHORT).show()
    }
}