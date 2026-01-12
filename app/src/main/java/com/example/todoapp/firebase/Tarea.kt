package com.example.todoapp.firebase

data class Tarea(
    var id: String = "",
    val texto: String = "",
    val fecha: String = "",
    val pokeName: String = "",
    val pokeType: String = "",
    val pokeStats: String = "",
    val pokeImg: String = "",
    val completada: Boolean = false
)