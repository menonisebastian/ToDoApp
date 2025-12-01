package com.example.todoapp

import androidx.compose.runtime.saveable.listSaver

// Saver para la clase Tarea
// Se ha añadido 'it.fecha' a la lista que se guarda.
// Se ha añadido 'it[2] as String' al restaurar para el campo 'fecha'.
val TareaSaver = listSaver<Tarea, Any>(
    save = { listOf(it.id, it.texto, it.fecha) }, // <-- EDITADO
    restore = { Tarea(it[0] as Int, it[1] as String, it[2] as String) } // <-- EDITADO
)

// Saver para Pair<Int, Tarea>?
// También se actualiza para guardar y restaurar la fecha de la Tarea dentro del Pair.
val UltimaTareaSaver = listSaver<Pair<Int, Tarea>?, Any>(
    save = { pair ->
        if (pair == null) {
            emptyList()
        } else {
            // Se añade pair.second.fecha a la lista que se guarda
            listOf(pair.first, pair.second.id, pair.second.texto, pair.second.fecha) // <-- EDITADO
        }
    },
    restore = { list ->
        if (list.isEmpty()) {
            null
        } else {
            Pair(
                list[0] as Int,
                // Se añade list[3] para restaurar la fecha en la Tarea
                Tarea(id = list[1] as Int, texto = list[2] as String, fecha = list[3] as String) // <-- EDITADO
            )
        }
    }
)
