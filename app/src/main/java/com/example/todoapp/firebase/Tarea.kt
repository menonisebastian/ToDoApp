package com.example.todoapp.firebase

data class Tarea(
    var id: String = "",
    val texto: String = "",
    val fecha: String = "",
    val completada: Boolean = false
)