package com.example.todoapp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todoapp.firebase.AuthState
import com.example.todoapp.firebase.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ============ REGISTER SCREEN ============
@Composable
fun Registrar(
    authViewModel: AuthViewModel, // Inyectamos el ViewModel
    onRegistrar: (String) -> Unit, // Callback para ir al login tras registro
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // VARIABLES DE TEXTO LOCALES
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var passConf by remember { mutableStateOf("") }

    // ESTADO DEL VIEWMODEL
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    val formularioValido = nombre.isNotBlank() && email.isNotBlank() &&
            userName.isNotBlank() && pass.isNotBlank() && passConf.isNotBlank()

    // MANEJO DE ERRORES
    LaunchedEffect(authState) {
        if (authState is AuthState.Error) {
            val msg = (authState as AuthState.Error).message
            snackbarHostState.showSnackbar(msg)
            authViewModel.resetState()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color.Red,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // LOGOS
            MainLogo()

            Spacer(Modifier.height(20.dp))

            // TARJETA DE REGISTRO
            Column(
                modifier = Modifier
                    .shadow(20.dp, RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
                    .padding(30.dp)
                    .width(300.dp),
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Registro",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 10.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )

                CustomTextField(value = nombre, onValueChange = { nombre = it }, label = "Nombre", isEnabled = authState !is AuthState.Loading)
                Spacer(Modifier.height(20.dp))

                CustomTextField(value = userName, onValueChange = { userName = it }, label = "Usuario", isEnabled = authState !is AuthState.Loading)
                Spacer(Modifier.height(20.dp))

                CustomTextField(value = email, onValueChange = { email = it }, label = "Email", isEnabled = authState !is AuthState.Loading)
                Spacer(Modifier.height(20.dp))

                CustomTextField(value = pass, onValueChange = { pass = it }, label = "Contraseña", isEnabled = authState !is AuthState.Loading)
                Spacer(Modifier.height(20.dp))

                CustomTextField(value = passConf, onValueChange = { passConf = it }, label = "Confirmar contraseña", isEnabled = authState !is AuthState.Loading)
                Spacer(Modifier.height(20.dp))

                // BOTÓN DE REGISTRAR
                ElevatedButton(
                    onClick = {
                        if (pass != passConf) {
                            scope.launch { snackbarHostState.showSnackbar("Las contraseñas no coinciden") }
                        } else {
                            // LLAMADA AL VIEWMODEL (Sin lógica de Firestore aquí)
                            authViewModel.registrar(email, pass, nombre, userName)
                        }
                    },
                    enabled = formularioValido && authState !is AuthState.Loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = Color.Gray
                    )
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(text = "Crear cuenta", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // BOTÓN VOLVER
            TextButton(onClick = { onBack() }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Volver", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(15.dp))
                    Text("Ya tengo una cuenta", color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(start = 5.dp))
                }
            }
            Spacer(Modifier.weight(1f))

            Text(text = "Desarrollada por Sebastián Menoni", color = MaterialTheme.colorScheme.inversePrimary, fontStyle = FontStyle.Italic, fontSize = 12.sp)
        }
    }

    // DIÁLOGO DE ÉXITO
    if (authState is AuthState.Success) {
        Dialog(onDismissRequest = {}) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(20.dp))
                    .padding(40.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("REGISTRO EXITOSO", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 18.sp, fontStyle = FontStyle.Italic)
                Spacer(Modifier.height(20.dp))
                Icon(Icons.Default.CheckCircle, contentDescription = "ok", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(60.dp))
            }
        }

        LaunchedEffect(Unit) {
            delay(2000)
            onRegistrar(email) // Pasamos el email al login para autocompletar si quieres
            authViewModel.resetState()
        }
    }
}