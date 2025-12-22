package com.example.todoapp

import com.example.todoapp.screens.Login
import com.example.todoapp.screens.Registrar
import com.example.todoapp.screens.App
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.todoapp.ui.theme.ToDoAppTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todoapp.firebase.AuthViewModel
import com.example.todoapp.firebase.AuthViewModelFactory
import com.example.todoapp.firebase.TareasViewModel
import com.example.todoapp.firebase.TareasViewModelFactory
import com.example.todoapp.resources.SettingsPreferences
import com.google.firebase.auth.FirebaseAuth

// ============ ACTIVITY ============
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent{
            val settingsPreferences = remember { SettingsPreferences(applicationContext) }

            // Instancia del ViewModel conectada a la BD
            val viewModel: TareasViewModel = viewModel(factory = TareasViewModelFactory())

            val isDarkMode by settingsPreferences.isDarkMode.collectAsStateWithLifecycle(initialValue = false)

            // 1. OBTENER USUARIO ACTUAL
            // Firebase recuerda la sesión automáticamente. Chequeamos si existe al iniciar la app.
            val auth = FirebaseAuth.getInstance()
            val usuarioActual = auth.currentUser

            val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory())

            // 2. DEFINIR DESTINO INICIAL
            // Si el usuario existe, vamos directo a "app", si no, a "login"
            val startDestination = if (usuarioActual != null) "app" else "login"

            ToDoAppTheme(darkTheme = isDarkMode)
            {
                val textColorName by settingsPreferences.taskTextColor.collectAsStateWithLifecycle(initialValue = "Default")

                val taskTextColor = when (textColorName)
                {
                    "Naranja" -> MaterialTheme.colorScheme.primary
                    "Azul" -> MaterialTheme.colorScheme.secondary
                    "Dinamico" -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.onSurface
                }

                AppNav(taskTextColor = taskTextColor,
                    viewModel = viewModel,
                    authViewModel = authViewModel,
                    startDestination = startDestination)
            }
        }
    }
}

@Composable
fun AppNav(
    taskTextColor: Color,
    viewModel: TareasViewModel,
    startDestination: String,
    authViewModel: AuthViewModel
) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = startDestination)
    {
        composable("login") {
            Login(
                authViewModel = authViewModel,
                onRegistrar = { navController.navigate("register")},
                onLoginSuccess = { navController.navigate("app") { popUpTo("login") { inclusive = true } } }
            )
        }

        composable("register") {
            Registrar(
                authViewModel = authViewModel,
                onRegistrar = {navController.navigate("login")},
                onBack = { navController.popBackStack() }
            )
        }

        composable("app") {
            App(
                onBack = { navController.navigate("login") { popUpTo("app") { inclusive = true } } },
                // -------------------
                taskTextColor = taskTextColor,
                viewModel = viewModel
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview()
{
    ToDoAppTheme {
        Registrar(onRegistrar = {}, onBack = {}, authViewModel = AuthViewModel())
    }
}