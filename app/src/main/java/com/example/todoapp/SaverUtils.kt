package com.example.todoapp

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver

// Saver para la clase Tarea
val TareaSaver = listSaver<Tarea, Any>(
    save = { listOf(it.id, it.texto) },
    restore = { Tarea(it[0] as Int, it[1] as String) }
)

// Saver para Pair<Int, Tarea>?
val UltimaTareaSaver = listSaver<Pair<Int, Tarea>?, Any>(
    save = { pair ->
        if (pair == null) emptyList()
        else listOf(pair.first, pair.second.id, pair.second.texto)
    },
    restore = { list ->
        if (list.isEmpty()) null
        else Pair(
            list[0] as Int,
            Tarea(id = list[1] as Int, texto = list[2] as String)
        )
    }
)
