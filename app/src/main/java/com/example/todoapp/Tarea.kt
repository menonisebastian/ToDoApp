package com.example.todoapp

data class Tarea(
    var id: String = "",
    val texto: String = "",
    val fecha: String = "",
    val completada: Boolean = false
)