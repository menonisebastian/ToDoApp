package com.example.todoapp

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TareasViewModel(private val dao: TareaDao) : ViewModel() {

    // StateFlow convierte el Flow de Room en un estado observable para Compose
    val listaTareas: StateFlow<List<Tarea>> = dao.obtenerTodas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val listaCompletadas: StateFlow<List<Tarea>> = dao.obtenerCompletadas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    fun agregarTarea(texto: String, fecha: String) {
        viewModelScope.launch {
            dao.insertar(Tarea(id = 0, texto = texto, fecha = fecha))
        }
    }

    fun completarTarea(tarea: Tarea) {
        viewModelScope.launch {
            dao.actualizar(tarea.copy(completada = !tarea.completada))
        }
    }

    fun descompletarTarea(tarea: Tarea) {
        viewModelScope.launch {
            dao.actualizar(tarea.copy(completada = false))
        }
    }

    fun actualizarTarea(tarea: Tarea) {
        viewModelScope.launch {
            dao.actualizar(tarea)
        }
    }

    fun eliminarTarea(tarea: Tarea) {
        viewModelScope.launch {
            dao.eliminar(tarea)
        }
    }

    fun restaurarTarea(tarea: Tarea) {
        viewModelScope.launch {
            // Al restaurar, usamos id=0 para que Room genere uno nuevo y evitar conflictos
            dao.insertar(tarea.copy(id = 0))
        }
    }

    fun vaciarLista() {
        viewModelScope.launch {
            dao.eliminarTodas()
        }
    }
}

// Factory para inyectar la dependencia de la Base de Datos
class TareasViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TareasViewModel::class.java)) {
            val db = TareasDatabase.getDatabase(application)
            @Suppress("UNCHECKED_CAST")
            return TareasViewModel(db.tareaDao()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}