package com.example.todoapp

import androidx.compose.ui.graphics.Color

enum class TaskPriority(val label: String, val color: Color) {
    HIGH("Alta", Color(0xFFE53935)),   // Rojo
    MEDIUM("Media", Color(0xFFFF9800)), // Naranja
    LOW("Baja", Color(0xFF43A047)),    // Verde
    EXPIRED("Vencida", Color.Gray),
    COMPLETED("Completada", Color.Blue),
    UNKNOWN("N/A", Color.Gray)         // En caso de error de formato
}