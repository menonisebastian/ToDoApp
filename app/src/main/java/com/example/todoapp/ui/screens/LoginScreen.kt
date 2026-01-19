package com.example.todoapp.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todoapp.R
import com.example.todoapp.data.firebase.AuthState
import com.example.todoapp.data.firebase.AuthViewModel
import com.example.todoapp.ui.CustomTextField
import com.example.todoapp.ui.MainLogo
import com.example.todoapp.ui.RowButtons
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ============ LOGIN SCREEN ============
@Composable
fun Login(
    authViewModel: AuthViewModel,
    onLoginSuccess: (String) -> Unit,
    onRegistrar: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // VARIABLES DE TEXTO LOCALES
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    // Estado actual del ViewModel (Idle, Loading, Success, Error)
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    // CONFIGURACIÓN GOOGLE (Se mantiene en UI porque requiere Activity)
    val token = stringResource(R.string.token) // En strings.xml
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(token)
        .requestEmail()
        .build()
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    // Launcher de Google
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                val googleEmail = account.email ?: ""
                val googleName = account.displayName ?: "Usuario Google"

                authViewModel.loginWithGoogle(credential, googleEmail, googleName)

            } catch (e: ApiException) {
                scope.launch { snackbarHostState.showSnackbar("Error Google: ${e.statusCode}") }
            }
        }
    }

    // MANEJO DE ERRORES DEL VIEWMODEL
    LaunchedEffect(authState) {
        if (authState is AuthState.Error) {
            val msg = (authState as AuthState.Error).message
            snackbarHostState.showSnackbar(msg)
            authViewModel.resetState() // Volvemos a Idle para permitir reintentar
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = if (authState is AuthState.Error) Color.Red else MaterialTheme.colorScheme.secondary,
                    contentColor = if (authState is AuthState.Error) Color.White else MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(30.dp),
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
            // LOGO E IMAGEN
            MainLogo()

            Spacer(Modifier.height(60.dp))

            // TARJETA DE FORMULARIO
            Column(
                modifier = Modifier
                    .shadow(15.dp, RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
                    .padding(20.dp)
                    .width(300.dp),
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                CustomTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    isEnabled = authState !is AuthState.Loading
                )
                Spacer(Modifier.height(20.dp))

                CustomTextField(
                    value = pass,
                    onValueChange = { pass = it },
                    label = "Contraseña",
                    isEnabled = authState !is AuthState.Loading
                )
                Spacer(Modifier.height(20.dp))

                // BOTÓN DE LOGIN
                ElevatedButton(
                    enabled = authState !is AuthState.Loading,
                    onClick = {
                        if (email.isNotBlank() && pass.isNotBlank()) {
                            authViewModel.login(email, pass)
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("Introduce email y contraseña") }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Iniciar Sesión")
                    }
                }

                // LINKS TEXTO
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 10.dp)) {
                    TextButton(
                        onClick = { onRegistrar() },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                    ) { Text("Registrarme") }

                    TextButton(
                        onClick = { scope.launch { snackbarHostState.showSnackbar("Por implementar") } },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.inversePrimary)
                    ) { Text("Recuperar contraseña") }
                }
            }

            Text("Accede también con",
                color = MaterialTheme.colorScheme.inversePrimary,
                modifier = Modifier.padding(top = 20.dp),
                fontSize = 15.sp
            )

            // LOGIN CON REDES SOCIALES
            RowButtons(
                onGoogleClick = {
                    // Forzamos el cierre de sesión del cliente de Google para poder elegir cuenta
                    googleSignInClient.signOut().addOnCompleteListener {
                        googleLauncher.launch(googleSignInClient.signInIntent)
                    }
                },
                onFacebookClick = { scope.launch { snackbarHostState.showSnackbar("Facebook: Por implementar") } },
                onGithubClick = { scope.launch { snackbarHostState.showSnackbar("Github: Por implementar") } },
                onMicrosoftClick = { scope.launch { snackbarHostState.showSnackbar("Microsoft: Por implementar") } }
            )

            Spacer(Modifier.weight(1f))
            TextButton(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, "https://github.com/menonisebastian/ToDoApp".toUri())
                context.startActivity(intent)
            })
            { Row(verticalAlignment = Alignment.CenterVertically)
                {
                    Text("Visitar Github ", color = MaterialTheme.colorScheme.secondary)
                    Icon(Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Github",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
            Text(text = "Desarrollada por Sebastián Menoni", color = MaterialTheme.colorScheme.inversePrimary, fontStyle = FontStyle.Italic, fontSize = 12.sp)
        }
    }

    // DIÁLOGO DE ÉXITO Y NAVEGACIÓN
    if (authState is AuthState.Success) {
        Dialog(onDismissRequest = {}) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(20.dp))
                    .padding(40.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("INICIO DE SESIÓN EXITOSO", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 18.sp, fontStyle = FontStyle.Italic)
                Spacer(Modifier.height(20.dp))
                Icon(Icons.Default.CheckCircle, contentDescription = "ok", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(60.dp))
            }
        }

        LaunchedEffect(Unit) {
            delay(1500)
            onLoginSuccess(email)
            authViewModel.resetState()
        }
    }
}