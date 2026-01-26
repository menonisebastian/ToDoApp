package com.example.todoapp.data.firebase

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuthUserCollisionException

// Estados posibles de la autenticación para que la UI reaccione
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    // Lógica de Login
    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWithEmailAndPassword(email.trim(), pass.trim()).await()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(mapFirebaseError(e))
            }
        }
    }

    // Lógica de Registro (Auth + Firestore)
    fun registrar(email: String, pass: String, nombre: String, userName: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // 1. Crear usuario en Auth
                val authResult = auth.createUserWithEmailAndPassword(email.trim(), pass.trim()).await()
                val userId = authResult.user?.uid ?: throw Exception("Error obteniendo UID")
                val fechaAlta = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

                // 2. Guardar datos en Firestore
                val nuevoUsuario = hashMapOf(
                    "id" to userId,
                    "username" to userName.trim(),
                    "nombre" to nombre.trim(),
                    "email" to email.trim(),
                    "fechaalta" to fechaAlta
                )

                db.collection("users").document(userId).set(nuevoUsuario).await()

                // 3. Éxito
                _authState.value = AuthState.Success

            } catch (e: Exception) {
                _authState.value = AuthState.Error(mapFirebaseError(e))
            }
        }
    }

    fun loginWithGoogle(credential: AuthCredential, email: String, nombre: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val authResult = auth.signInWithCredential(credential).await()
                val userId = authResult.user?.uid ?: throw Exception("Error al obtener UID")

                val docSnapshot = db.collection("users").document(userId).get().await()

                if (!docSnapshot.exists()) {
                    val fechaAlta = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    val nuevoUsuario = hashMapOf(
                        "id" to userId,
                        "username" to email.split("@")[0],
                        "nombre" to nombre,
                        "email" to email,
                        "fechaalta" to fechaAlta
                    )
                    db.collection("users").document(userId).set(nuevoUsuario).await()
                }
                _authState.value = AuthState.Success

            } catch (e: Exception) {
                _authState.value = AuthState.Error(mapFirebaseError(e))
            }
        }
    }

    // Función auxiliar para mapear errores
    private fun mapFirebaseError(e: Exception): String {
        Log.e("AuthViewModel", "Error Firebase", e)

        return when (e) {
            // 1. excepción específica
            is FirebaseAuthUserCollisionException -> {
                "Ya existe una cuenta con este email. Usa tu contraseña o recupérala."
            }

            // 2. excepción general
            is FirebaseAuthException -> {
                when (e.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "El formato del correo no es válido."
                    "ERROR_WRONG_PASSWORD" -> "La contraseña es incorrecta."
                    "ERROR_USER_NOT_FOUND" -> "No existe ninguna cuenta con este correo."
                    "ERROR_USER_DISABLED" -> "Esta cuenta ha sido inhabilitada."
                    "ERROR_TOO_MANY_REQUESTS" -> "Demasiados intentos. Inténtalo más tarde."
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "El email ya está registrado."
                    "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> "Ya existe una cuenta con este email usando otro método."
                    else -> "Error de autenticación: ${e.errorCode}"
                }
            }

            // 3. Cualquier otra excepción
            else -> e.message ?: "Error desconocido"
        }
    }
}

// Factory para instanciar el ViewModel
class AuthViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}