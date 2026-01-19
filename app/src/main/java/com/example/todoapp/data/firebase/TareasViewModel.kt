package com.example.todoapp.data.firebase

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.model.InfoPokemon
import com.example.todoapp.data.model.Tarea
import com.example.todoapp.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.todoapp.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TareasViewModel : ViewModel() {

    // ===============================================================
    // 1. DEPENDENCIAS Y VARIABLES INTERNAS
    // ===============================================================
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var snapshotListener: ListenerRegistration? = null
    private var currentUserId: String? = null

    // ===============================================================
    // 2. ESTADO (STATEFLOWS)
    // ===============================================================

    // --- Todas las tareas crudas desde Firebase ---
    private val _todasLasTareas = MutableStateFlow<List<Tarea>>(emptyList())

    // --- Datos del Usuario ---
    private val _datosUsuario = MutableStateFlow<User?>(null)
    val datosUsuario: StateFlow<User?> = _datosUsuario.asStateFlow()


    // --- Listas Filtradas para la UI (Derivadas) ---

    // Tareas PENDIENTES
    val listaTareas: StateFlow<List<Tarea>> = _todasLasTareas
        .map { list -> list.filter { !it.completada } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tareas COMPLETADAS
    val listaCompletadas: StateFlow<List<Tarea>> = _todasLasTareas
        .map { list -> list.filter { it.completada } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ===============================================================
    // 3. INICIALIZACIÓN
    // ===============================================================
    init {
        // Detectar cambios en la sesión de usuario (Login/Logout)
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // Usuario logueado: Cargamos todo
                currentUserId = user.uid
                escucharTareas(user.uid)
                obtenerDatosUsuario(user.uid)
            } else {
                // Usuario deslogueado: Limpiamos todo
                limpiarEstado()
            }
        }
    }

    private fun limpiarEstado() {
        currentUserId = null
        _todasLasTareas.value = emptyList()
        _datosUsuario.value = null
        snapshotListener?.remove()
    }

    // ===============================================================
    // 4. LÓGICA DE CARGA DE DATOS (PRIVADA)
    // ===============================================================

    private fun escucharTareas(userId: String) {
        snapshotListener?.remove()

        val tareasRef = db.collection("users").document(userId).collection("tareas")

        snapshotListener = tareasRef
            .orderBy("fecha", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("Firebase", "Error al escuchar tareas", error)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val lista = snapshots.documents.mapNotNull { doc ->
                        val t = doc.toObject(Tarea::class.java)
                        t?.id = doc.id
                        t
                    }
                    _todasLasTareas.value = lista
                }
            }
    }

    private fun obtenerDatosUsuario(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val usuario = document.toObject(User::class.java)
                    _datosUsuario.value = usuario
                }
            }
    }

    // ===============================================================
    // 4.5 LLAMADA A LA API
    // ===============================================================

    private suspend fun buscarDatosPokemon(texto: String): InfoPokemon {
        // 1. Limpieza del texto
        val posiblePokemon = texto.trim()
            .substringAfterLast(" ")
            .lowercase()
            .trim { !it.isLetterOrDigit() }

        // Si no hay palabra válida, devolvemos vacío inmediatamente
        if (posiblePokemon.isEmpty()) return InfoPokemon()

        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.getPokemon(posiblePokemon)

                // Formateo de datos
                val pName = response.name.replaceFirstChar { it.uppercase() }
                val pImg = response.sprites.front_default ?: ""

                val pType = response.types.joinToString(", ") {
                    it.type.name.replaceFirstChar { c -> c.uppercase() }
                }

                val pStats = response.stats.joinToString(" | ") { slot ->
                    val statName = when (slot.stat.name) {
                        "hp" -> "HP"
                        "attack" -> "Atk"
                        "defense" -> "Def"
                        "special-attack" -> "SpA"
                        "special-defense" -> "SpD"
                        "speed" -> "Spd"
                        else -> slot.stat.name
                    }
                    "$statName: ${slot.base_stat}"
                }

                // Retornamos el objeto lleno
                InfoPokemon(pName, pType, pStats, pImg)

            } catch (e: Exception) {
                Log.d("PokeAPI", "Error buscando '$posiblePokemon': ${e.message}")
                // Retornamos vacío si falla
                InfoPokemon()
            }
        }
    }

    // ===============================================================
    // 5. OPERACIONES CRUD (ACCIONES PÚBLICAS)
    // ===============================================================
    fun agregarTarea(texto: String, fecha: String) {
        val uid = currentUserId ?: return

        viewModelScope.launch {
            // LLAMADA UNIFICADA
            val infoPoke = buscarDatosPokemon(texto)

            val nuevaTarea = Tarea(
                id = "",
                texto = texto,
                fecha = fecha,
                completada = false,
                // Usamos los datos obtenidos
                pokeName = infoPoke.name,
                pokeType = infoPoke.type,
                pokeStats = infoPoke.stats,
                pokeImg = infoPoke.img
            )

            db.collection("users").document(uid).collection("tareas").add(nuevaTarea)
        }
    }

    fun completarTarea(tarea: Tarea) = actualizarEstadoTarea(tarea, true)

    fun descompletarTarea(tarea: Tarea) = actualizarEstadoTarea(tarea, false)

    fun editarTarea(tarea: Tarea, nuevoTexto: String, nuevaFecha: String) {
        val uid = currentUserId ?: return

        viewModelScope.launch {
            // LLAMADA UNIFICADA
            val infoPoke = buscarDatosPokemon(nuevoTexto)

            val actualizaciones = mapOf(
                "texto" to nuevoTexto,
                "fecha" to nuevaFecha,
                // Usamos los datos obtenidos (si infoPoke está vacío, esto borrará los datos viejos en Firebase)
                "pokeName" to infoPoke.name,
                "pokeType" to infoPoke.type,
                "pokeStats" to infoPoke.stats,
                "pokeImg" to infoPoke.img
            )

            if (tarea.id.isNotEmpty()) {
                db.collection("users").document(uid).collection("tareas")
                    .document(tarea.id)
                    .update(actualizaciones)
            }
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
        agregarTarea(tarea.texto, tarea.fecha)
    }

    fun vaciarLista() {
        val uid = currentUserId ?: return
        val batch = db.batch()

        _todasLasTareas.value.forEach { tarea ->
            val docRef = db.collection("users").document(uid).collection("tareas").document(tarea.id)
            batch.delete(docRef)
        }
        batch.commit()
    }

    private fun actualizarEstadoTarea(tarea: Tarea, estaCompletada: Boolean) {
        val uid = currentUserId ?: return
        if (tarea.id.isNotEmpty()) {
            db.collection("users").document(uid).collection("tareas")
                .document(tarea.id)
                .update("completada", estaCompletada)
        }
    }

    // ===============================================================
    // 6. CICLO DE VIDA
    // ===============================================================

    override fun onCleared() {
        super.onCleared()
        snapshotListener?.remove()
    }
}

// ===============================================================
// FACTORY
// ===============================================================
class TareasViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TareasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TareasViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}