package com.example.todoapp.data.firebase

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
    // 5. OPERACIONES CRUD (ACCIONES PÚBLICAS)
    // ===============================================================
    fun agregarTarea(texto: String, fecha: String) {
        val uid = currentUserId ?: return

        // 1. Extraer la última palabra para buscar el Pokémon
        val posiblePokemon = texto.trim().substringAfterLast(" ").lowercase()

        viewModelScope.launch {
            var pokeName = ""
            var pokeType = ""
            var pokeStats = ""
            var pokeImg = ""

            // 2. Intentar buscar en la API en un hilo IO (segundo plano)
            if (posiblePokemon.isNotEmpty()) {
                try {
                    val response = withContext(Dispatchers.IO) {
                        RetrofitClient.instance.getPokemon(posiblePokemon)
                    }

                    // 3. Si hay éxito, formateamos los datos
                    pokeName = response.name.replaceFirstChar { it.uppercase() }
                    pokeImg = response.sprites.front_default ?: ""

                    // Formatear tipos (ej: "fire, flying")
                    pokeType = response.types.joinToString(", ") { it.type.name.replaceFirstChar { char -> char.uppercase() } }

                    // Formatear stats (ej: "HP: 45 | Atk: 49")
                    pokeStats = response.stats.joinToString(" | ") {
                        val statName = when(it.stat.name) {
                            "hp" -> "HP"
                            "attack" -> "Atk"
                            "defense" -> "Def"
                            "special-attack" -> "SpA"
                            "special-defense" -> "SpD"
                            "speed" -> "Spd"
                            else -> it.stat.name
                        }
                        "$statName: ${it.base_stat}"
                    }

                } catch (e: Exception) {
                    // Si falla (404 no encontrado o error de red), simplemente ignoramos
                    // y guardamos la tarea sin datos de Pokémon.
                    Log.d("PokeAPI", "No se encontró el Pokémon o hubo error: ${e.message}")
                }
            }

            // 4. Crear el objeto Tarea con los datos (vacíos o rellenos)
            val nuevaTarea = Tarea(
                id = "",
                texto = texto,
                fecha = fecha,
                completada = false,
                pokeName = pokeName,
                pokeType = pokeType,
                pokeStats = pokeStats,
                pokeImg = pokeImg
            )

            // 5. Guardar en Firebase
            db.collection("users").document(uid).collection("tareas").add(nuevaTarea)
        }
    }

    fun completarTarea(tarea: Tarea) = actualizarEstadoTarea(tarea, true)

    fun descompletarTarea(tarea: Tarea) = actualizarEstadoTarea(tarea, false)

    fun actualizarTarea(tarea: Tarea) {
        val uid = currentUserId ?: return
        if (tarea.id.isNotEmpty()) {
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