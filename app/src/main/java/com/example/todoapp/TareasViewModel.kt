package com.example.todoapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TareasViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var snapshotListener: ListenerRegistration? = null
    private var currentUserId: String? = null

    // 1. Fuente de verdad: Todas las tareas crudas desde Firebase
    private val _todasLasTareas = MutableStateFlow<List<Tarea>>(emptyList())

    // 2. Filtros para la UI (Derivados de _todasLasTareas)

    // Lista de tareas PENDIENTES (completada == false)
    val listaTareas: StateFlow<List<Tarea>> = _todasLasTareas
        .map { list -> list.filter { !it.completada } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Lista de tareas COMPLETADAS (completada == true)
    val listaCompletadas: StateFlow<List<Tarea>> = _todasLasTareas
        .map { list -> list.filter { it.completada } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Detectar automáticamente el usuario
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                currentUserId = user.uid
                escucharTareas(user.uid)
            } else {
                currentUserId = null
                _todasLasTareas.value = emptyList()
                snapshotListener?.remove()
            }
        }
    }

    private fun escucharTareas(userId: String) {
        snapshotListener?.remove()

        // Referencia: users -> UID -> tareas
        val tareasRef = db.collection("users").document(userId).collection("tareas")

        snapshotListener = tareasRef
            .orderBy("fecha", Query.Direction.ASCENDING) // Puedes cambiar el orden
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("Firebase", "Error al escuchar tareas", error)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val lista = snapshots.documents.mapNotNull { doc ->
                        val t = doc.toObject(Tarea::class.java)
                        t?.id = doc.id // Asignamos el ID del documento al objeto
                        t
                    }
                    _todasLasTareas.value = lista
                }
            }
    }

    // ============ OPERACIONES CRUD ============

    fun agregarTarea(texto: String, fecha: String) {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            val nuevaTarea = Tarea(
                id = "", // Firebase pondrá el ID
                texto = texto,
                fecha = fecha,
                completada = false
            )
            db.collection("users").document(uid).collection("tareas").add(nuevaTarea)
        }
    }

    fun completarTarea(tarea: Tarea) {
        actualizarEstadoTarea(tarea, true)
    }

    fun descompletarTarea(tarea: Tarea) {
        actualizarEstadoTarea(tarea, false)
    }

    // Función auxiliar privada para actualizar
    private fun actualizarEstadoTarea(tarea: Tarea, estaCompletada: Boolean) {
        val uid = currentUserId ?: return
        if (tarea.id.isNotEmpty()) {
            db.collection("users").document(uid).collection("tareas")
                .document(tarea.id)
                .update("completada", estaCompletada)
        }
    }

    fun actualizarTarea(tarea: Tarea) {
        val uid = currentUserId ?: return
        if (tarea.id.isNotEmpty()) {
            // .set sobrescribe, .update actualiza campos específicos
            db.collection("users").document(uid).collection("tareas")
                .document(tarea.id)
                .set(tarea)
        }
    }

    fun eliminarTarea(tarea: Tarea) {
        val uid = currentUserId ?: return
        if (tarea.id.isNotEmpty()) {
            db.collection("users").document(uid).collection("tareas")
                .document(tarea.id)
                .delete()
        }
    }

    fun restaurarTarea(tarea: Tarea) {
        // Simplemente la volvemos a crear
        agregarTarea(tarea.texto, tarea.fecha)
    }

    fun vaciarLista() {
        // Borramos SOLO las tareas visibles (pendientes o todas, según tu lógica).
        // Aquí borramos TODAS las del usuario
        val uid = currentUserId ?: return
        val batch = db.batch()

        // Nota: Firestore tiene limites en batch, pero para una app personal está bien
        _todasLasTareas.value.forEach { tarea ->
            val docRef = db.collection("users").document(uid).collection("tareas").document(tarea.id)
            batch.delete(docRef)
        }
        batch.commit()
    }

    override fun onCleared() {
        super.onCleared()
        snapshotListener?.remove()
    }
}

// Factory actualizada (Ya no necesita 'application' ni DAO)
class TareasViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TareasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TareasViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}