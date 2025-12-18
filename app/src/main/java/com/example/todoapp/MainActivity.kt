@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.todoapp

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.todoapp.ui.theme.ToDoAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.viewmodel.compose.viewModel
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import java.time.ZoneId
import com.google.firebase.auth.FirebaseAuth
import android.app.Activity
import androidx.compose.ui.res.stringResource
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider

// ============ ACTIVITY ============
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent{
            val settingsPreferences = remember { SettingsPreferences(applicationContext) }

            // Instancia del ViewModel conectada a la BD
            val viewModel: TareasViewModel = viewModel(
                factory = TareasViewModelFactory()
            )

            val isDarkMode by settingsPreferences.isDarkMode.collectAsStateWithLifecycle(initialValue = false)

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
                // Pasamos el viewModel a la navegación
                AppNav(taskTextColor = taskTextColor, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AppNav(taskTextColor: Color, viewModel: TareasViewModel) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "login")
    {
        composable("login") {
            Login(
                onRegistrar = {
                    navController.navigate("register")},
                onLoginSuccess = {
                    navController.navigate("app")}
            )
        }

        composable("register") {
            Registrar(
                onRegistrar = {navController.navigate("login") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("app") {
            App(
                onBack = { navController.popBackStack() },
                taskTextColor = taskTextColor,
                viewModel = viewModel
            )
        }
    }
}

// ============ LOGIN SCREEN ============
@Composable
fun Login(onLoginSuccess: (String) -> Unit,
          onRegistrar: () -> Unit)
{
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val token = stringResource(R.string.token)

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(token)
        .requestEmail()
        .build()

    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    // Lanzador para el resultado de Google
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential)
                    .addOnSuccessListener {
                        showDialog = true
                        error = false
                    }
                    .addOnFailureListener { e ->
                        error = true
                        scope.launch { snackbarHostState.showSnackbar("Error Google: ${e.message}") }
                    }
            } catch (e: ApiException) {
                error = true
                scope.launch { snackbarHostState.showSnackbar("Error API Google: ${e.statusCode}") }
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState)
    {
            data ->
        // Aquí personalizamos el aspecto visual
        Snackbar(
            snackbarData = data,
            containerColor = if (error) Color.Red else MaterialTheme.colorScheme.surface,
            contentColor = if (error) Color.White else MaterialTheme.colorScheme.onSurface,
            actionColor = Color.Yellow,
            shape = RoundedCornerShape(30.dp),
            modifier = Modifier.padding(10.dp).width(400.dp)
        )} })
    {
        paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.cutlogoapp),
                modifier = Modifier.size(60.dp).padding(10.dp),
                contentDescription = "Logo"
            )
            Image(
                painter = painterResource(R.drawable.fontlogo),
                modifier = Modifier.width(150.dp).padding(top = 10.dp),
                contentDescription = "logo texto"
            )
            Spacer(Modifier.height(120.dp))
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
                CustomTextField(value = email, onValueChange = { email = it }, label = "Email", enabled = true)

                Spacer(Modifier.height(20.dp))
                CustomTextField(value = pass, onValueChange = { pass = it }, label = "Contraseña", enabled = true)

                Spacer(Modifier.height(20.dp))

                ElevatedButton(
                    enabled = !isLoading,
                    onClick = {
                        if (email.isNotBlank() && pass.isNotBlank())
                        {
                            isLoading = true

                            auth.signInWithEmailAndPassword(email.trim(), pass.trim())
                                .addOnSuccessListener { _ ->
                                    // 4. Importante: Desactivar carga al tener éxito
                                    isLoading = false
                                    showDialog = true
                                    error = false
                                }
                                .addOnFailureListener { e ->
                                    // 4. Importante: Desactivar carga también si falla
                                    isLoading = false
                                    error = true

                                    // Casteamos a FirebaseAuthException para leer el código de error exacto
                                    val mensajeError = if (e is com.google.firebase.auth.FirebaseAuthException) {
                                        when (e.errorCode) {
                                            "ERROR_INVALID_EMAIL" -> "El formato del correo no es válido."
                                            "ERROR_WRONG_PASSWORD" -> "La contraseña es incorrecta."
                                            "ERROR_USER_NOT_FOUND" -> "No existe ninguna cuenta con este correo."
                                            "ERROR_USER_DISABLED" -> "Esta cuenta ha sido inhabilitada."
                                            "ERROR_TOO_MANY_REQUESTS" -> "Demasiados intentos fallidos. Inténtalo más tarde."
                                            "ERROR_OPERATION_NOT_ALLOWED" -> "El inicio de sesión con correo y contraseña no está habilitado."
                                            else -> "El email o la contraseña son incorrectos."
                                        }
                                    } else {
                                        // Error genérico si no es de Firebase Auth (ej. falta de internet)
                                        "Error de conexión o desconocido. Inténtalo de nuevo."
                                    }

                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = mensajeError,
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                        } else {
                            scope.launch {
                                error = true
                                snackbarHostState.showSnackbar(
                                    message = "Por favor, introduce tu email y contraseña para continuar",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, disabledContainerColor = MaterialTheme.colorScheme.primary)
                ) {
                    // 5. Cambiamos visualmente el contenido del botón según el estado
                    if (isLoading) {
                        // Indicador de carga pequeño
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Text("Iniciar Sesión")
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 10.dp)){
                    TextButton(onClick = { onRegistrar() },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary))
                    { Text("Registrarme") }

                    TextButton(onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Por implementar",
                                duration = SnackbarDuration.Short
                            )
                        }
                    },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.inversePrimary))
                    { Text("Recuperar contraseña") }
                }
            }

            RowButtons(onGoogleClick = {googleLauncher.launch(googleSignInClient.signInIntent)},
                onFacebookClick = {},
                onGithubClick = {},
                onMicrosoftClick = {})

            Spacer(Modifier.weight(1f))

            Text(text = "Desarrollada por Sebastián Menoni", color = MaterialTheme.colorScheme.inversePrimary, fontStyle = FontStyle.Italic, fontSize = 12.sp)
        }
    }
    if (showDialog)
    {
        Dialog(onDismissRequest = {})
        {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background, RoundedCornerShape(20.dp))
                .padding(40.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally)
            {
                Text("INICIO DE SESIÓN EXITOSO", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 18.sp, fontStyle = FontStyle.Italic)
                Spacer(Modifier.height(20.dp))
                Icon(Icons.Default.CheckCircle, contentDescription = "ok", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(60.dp))
            }
            LaunchedEffect(Unit) {
                delay(1000) // Espera
                showDialog = false // Cierra el diálogo cambiando el estado
                onLoginSuccess(email)
            }
        }
    }
}

// ============ REGISTER SCREEN ============
@Composable
fun Registrar(onRegistrar: (String) -> Unit, onBack: () -> Unit)
{
    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var passConf by remember { mutableStateOf("") }
    val fechaAlta = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    var showDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val formularioValido = nombre.isNotBlank() && email.isNotBlank() &&
            userName.isNotBlank() && pass.isNotBlank() &&
            passConf.isNotBlank()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState)
    {
        data ->
        // Aquí personalizamos el aspecto visual
        Snackbar(
            snackbarData = data,
            containerColor = Color.Red,
            contentColor = Color.White,
            actionColor = Color.Yellow,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.padding(10.dp).width(400.dp)
        )} })
        {
            paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.cutlogoapp),
                    modifier = Modifier.size(60.dp).padding(10.dp),
                    contentDescription = "Logo"
                )
                Image(
                    painter = painterResource(R.drawable.fontlogo),
                    modifier = Modifier.width(150.dp).padding(top = 10.dp),
                    contentDescription = "logo texto"
                )

                Spacer(Modifier.height(20.dp))
                Column(
                    modifier = Modifier
                        .shadow(20.dp, RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
                        .padding(30.dp)
                        .width(300.dp),
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Registro",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(bottom = 10.dp),
                        color = MaterialTheme.colorScheme.onSurface)

                    CustomTextField(value = nombre, onValueChange = { nombre = it }, label = "Nombre", enabled = true)
                    Spacer(Modifier.height(20.dp))

                    CustomTextField(value = userName, onValueChange = { userName = it }, label = "Usuario", enabled = true)
                    Spacer(Modifier.height(20.dp))

                    CustomTextField(value = email, onValueChange = { email = it }, label = "Email", enabled = true)
                    Spacer(Modifier.height(20.dp))

                    CustomTextField(value = pass, onValueChange = { pass = it }, label = "Contraseña", enabled = true)
                    Spacer(Modifier.height(20.dp))

                    CustomTextField(value = passConf, onValueChange = { passConf = it }, label = "Confirmar contraseña", enabled = true)
                    Spacer(Modifier.height(20.dp))

                    ElevatedButton(
                        onClick = {
                            if (pass != passConf)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Las contraseñas no coinciden",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            else
                                isLoading = true
                                auth.createUserWithEmailAndPassword(email, pass)
                                    .addOnSuccessListener { authResult ->
                                        isLoading = false
                                        val userId = authResult.user?.uid ?: ""

                                        // 3. Preparar los datos para guardar en Base de Datos
                                        val nuevoUsuario = hashMapOf(
                                            "id" to userId,
                                            "username" to userName.trim(), // El usuario corto para mostrar
                                            "nombre" to nombre,
                                            "email" to email, // El email real (gmail/hotmail) para contactar
                                            "fechaalta" to fechaAlta
                                        )

                                        // 4. Guardar en Firestore: Colección "users", Documento = UID
                                        db.collection("users")
                                            .document(userId)
                                            .set(nuevoUsuario)
                                            .addOnSuccessListener {
                                                // Inicia la sesion
                                                showDialog = true
                                                // Pasamos el usuario limpio al login para que no tenga que escribirlo de nuevo
                                                onRegistrar(email)
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(context, "Error al guardar datos: ${e.message}", Toast.LENGTH_LONG).show()
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        isLoading = false
                                        // Errores al crear la cuenta (ej: usuario ya existe)
                                        val msg = if (e.message?.contains("email address is already in use") == true)
                                            "El email $email ya está registrado."
                                        else "Error: ${e.message}"

                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = msg,
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }

                        },
                        enabled = formularioValido,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = Color.Gray
                        )
                    ){
                        if (isLoading) {
                            // Indicador de carga pequeño
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        } else {
                            Text(text = "Crear cuenta", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                TextButton(onClick = { onBack() })
                {
                    Row(verticalAlignment = Alignment.CenterVertically)
                    {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Volver", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(15.dp))
                        Text("Ya tengo una cuenta", color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(start = 5.dp))
                    }
                }
                Spacer(Modifier.weight(1f))

                Text(text = "Desarrollada por Sebastián Menoni", color = MaterialTheme.colorScheme.inversePrimary, fontStyle = FontStyle.Italic, fontSize = 12.sp)
            }
        }

    if (showDialog)
    {
        Dialog(onDismissRequest = {})
        {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background, RoundedCornerShape(20.dp))
                .padding(40.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally)
            {
                Text("REGISTRO EXITOSO", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 18.sp, fontStyle = FontStyle.Italic)
                Spacer(Modifier.height(20.dp))
                Icon(Icons.Default.CheckCircle, contentDescription = "ok", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(60.dp))
            }
            LaunchedEffect(Unit) {
                delay(3000) // Espera
                showDialog = false // Cierra el diálogo cambiando el estado

            }
        }
    }
}

// ============ MAIN APP SCREEN ============
@Composable
fun App(
    taskTextColor: Color,
    onBack: () -> Unit,
    viewModel: TareasViewModel
) {
    // VARIABLES LOCALES
    var tarea by remember { mutableStateOf("") }

    // OBSERVAMOS LA BD DESDE EL VIEWMODEL
    val tareas by viewModel.listaTareas.collectAsStateWithLifecycle()
    val completadas by viewModel.listaCompletadas.collectAsStateWithLifecycle()
    val datosUsuario by viewModel.datosUsuario.collectAsStateWithLifecycle()

    var fecha by remember { mutableStateOf("") }
    var tareaEditando by remember { mutableStateOf<Tarea?>(null) }
    var tareaAEliminar by remember { mutableStateOf<Tarea?>(null) }
    var ultimaTareaEliminada by remember { mutableStateOf<Tarea?>(null) }

    var showAddTareaDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showCuentaDialog by remember { mutableStateOf(false) }
    val showPreferencesDialog = remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var tareaDetallada by remember { mutableStateOf<Tarea?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val filteredTareas = if (searchQuery.isBlank()) { tareas }
    else { tareas.filter { it.texto.contains(searchQuery, ignoreCase = true) } }

    val context = LocalContext.current
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    if (showPreferencesDialog.value) {
        PreferencesDialog(onDismiss = { showPreferencesDialog.value = false })
    }

    val shakeDetector = remember {
        ShakeDetector {
            ultimaTareaEliminada?.let { task ->
                viewModel.restaurarTarea(task)
                ultimaTareaEliminada = null
                Toast.makeText(context, "Tarea restaurada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    DisposableEffect(Unit) {
        sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI)
        onDispose {
            sensorManager.unregisterListener(shakeDetector)
        }
    }

    val inactivityNotifier = remember { InactivityNotifier(context) }
    var hasNotificationPermission by remember { mutableStateOf(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    )}

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasNotificationPermission = isGranted }
    )

    LaunchedEffect(key1 = hasNotificationPermission) {
        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(key1 = tareas.size) {
        delay(60000)
        if (hasNotificationPermission) {
            inactivityNotifier.showNotification()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTareaDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 20.dp, bottom = 20.dp),
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Añadir Tarea")
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState)
        {
                data ->
            // Aquí personalizamos el aspecto visual
            Snackbar(
                snackbarData = data,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onBackground,
                actionColor = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(10.dp)
            )
        }
        }
    ) {

        paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.fontlogo),
                modifier = Modifier.width(100.dp).padding(10.dp),
                contentDescription = "logo texto"
            )

            TopCard(
                viewModel = viewModel,
                listaTareas = tareas,
                onVaciarLista = {
                    if (tareas.isNotEmpty()) showClearDialog = true
                    else Toast.makeText(context, "No hay tareas para vaciar", Toast.LENGTH_SHORT).show()
                },
                onBack = onBack,
                onPreferences = { showPreferencesDialog.value = true },
                onHelp = { showHelpDialog = true },
                query = searchQuery,
                onQueryChange = { searchQuery = it ;},
                onCuenta = { showCuentaDialog = true},
                listaCompletadas = completadas
            )

            if (tareas.isNotEmpty()) {

                Spacer(modifier = Modifier.height(20.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    if (filteredTareas.isEmpty()) {
                        item { EmptySearchMessage() }
                    } else {
                        items(filteredTareas, key = { it.id }) { tareaItem ->
                            Spacer(Modifier.height(10.dp))
                            val priority = determinePriority(tareaItem)
                            if (priority == TaskPriority.EXPIRED)
                            {
                                viewModel.completarTarea(tareaItem)
                            }
                            TaskItem(
                                tarea = tareaItem,
                                onTaskClick = { tareaDetallada = tareaItem },
                                onEdit = { tareaEditando = tareaItem },
                                textColor = taskTextColor,
                                onDelete = { tareaAEliminar = tareaItem },
                                onCheck = {
                                    viewModel.completarTarea(tareaItem)
                                    scope.launch {
                                        val result = snackbarHostState
                                            .showSnackbar(
                                                message = "Tarea completada",
                                                actionLabel = "Deshacer",
                                                // Defaults to SnackbarDuration.Short
                                                duration = SnackbarDuration.Short
                                            )
                                        when (result) {
                                            SnackbarResult.ActionPerformed -> {
                                                /* Handle snackbar action performed */
                                                viewModel.descompletarTarea(tareaItem)
                                            }
                                            SnackbarResult.Dismissed -> {
                                                /* Handle snackbar dismissed */
                                            }
                                        }
                                    }
                                }
                            )
                        }
                        if (completadas.isNotEmpty())
                        {
                            item()
                            {
                                Spacer(Modifier.height(10.dp))
                                CompletedTasksList(completedTasks = completadas, viewModel = viewModel, scope = scope, snackbarHostState = snackbarHostState
                                )
                            }
                        }
                    }
                }
            }
            else
            {
                if (completadas.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    CompletedTasksList(completedTasks = completadas, viewModel = viewModel, scope = scope, snackbarHostState = snackbarHostState)
                }
                EmptyTasksMessage()
            }
        }
    }

    if (showAddTareaDialog) {
        AggTareaDialog(
            onDismiss = { showAddTareaDialog = false },
            tarea = tarea,
            onTareaChange = { tarea = it },
            fecha = fecha,
            onFechaChange = { fecha = it },
            onAddTarea = { nuevaTarea, nuevaFecha ->
                if(nuevaTarea.isNotBlank()) {
                    viewModel.agregarTarea(nuevaTarea, nuevaFecha)
                    if (nuevaFecha.isNotBlank())
                    {
                        scheduleTaskNotification(context, nuevaTarea, nuevaFecha)
                    }
                    tarea = ""
                    fecha = ""
                    showAddTareaDialog = false
                    Toast.makeText(context, "Tarea agregada correctamente", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    if (tareaEditando != null) {
        val tareaToEdit = tareaEditando!!
        EditTaskDialog(
            tarea = tareaToEdit,
            onDismiss = { tareaEditando = null },
            onSave = { nuevoTexto, nuevaFecha ->
                viewModel.actualizarTarea(tareaToEdit.copy(texto = nuevoTexto, fecha = nuevaFecha))
                if (nuevaFecha.isNotBlank())
                {
                    scheduleTaskNotification(context, nuevoTexto, nuevaFecha)
                }
                tareaEditando = null
                Toast.makeText(context, "Tarea actualizada", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (tareaAEliminar != null) {
        ConfirmDeleteDialog(
            onDismiss = { tareaAEliminar = null },
            onConfirm = {
                val taskToDelete = tareaAEliminar!!
                ultimaTareaEliminada = taskToDelete
                viewModel.eliminarTarea(taskToDelete)
                scope.launch {
                    val result = snackbarHostState
                        .showSnackbar(
                            message = "Tarea eliminada. \nClickea o agita para deshacer.",
                            actionLabel = "Deshacer",
                            // Defaults to SnackbarDuration.Short
                            duration = SnackbarDuration.Short
                        )
                    when (result) {
                        SnackbarResult.ActionPerformed -> {
                            /* Handle snackbar action performed */
                            viewModel.restaurarTarea(taskToDelete)
                        }
                        SnackbarResult.Dismissed -> {
                            /* Handle snackbar dismissed */
                        }
                    }
                }
                tareaAEliminar = null
            }
        )
    }

    if (showClearDialog) {
        ConfirmClearDialog(
            onDismiss = { showClearDialog = false },
            onConfirm = {
                viewModel.vaciarLista()
                ultimaTareaEliminada = null
                Toast.makeText(context, "La lista de tareas ha sido vaciada", Toast.LENGTH_SHORT).show()
                showClearDialog = false
            }
        )
    }

    if (showCuentaDialog)
    {
        CuentaDialog(usuario = datosUsuario,
            onDismiss = { showCuentaDialog = false })
    }

    if (tareaDetallada != null) {
        DetailTaskDialog(tarea = tareaDetallada!!, onDismiss = { tareaDetallada = null })
    }
    if (showHelpDialog) { HelpDialog(onDismiss = { showHelpDialog = false }) }
}

fun exportarTareas(context: Context, listaTareas: List<Tarea>, listaCompletadas: List<Tarea>) {
    val stringBuilder = StringBuilder()
    if (listaTareas.isNotEmpty())
    {
        stringBuilder.append("TAREAS PENDIENTES:\n")
        listaTareas.forEach { tarea ->
            stringBuilder.append(
                if (tarea.fecha.isNotBlank())
                    "Tarea: ${tarea.texto} - Fecha: ${tarea.fecha}\n"
                else
                    "Tarea: ${tarea.texto}\n"
            )
        }
    }

    if (listaCompletadas.isNotEmpty())
    {
        stringBuilder.append("\nTAREAS COMPLETADAS:\n")
        listaCompletadas.forEach { tarea ->
            stringBuilder.append(
                if (tarea.fecha.isNotBlank())
                    "Tarea: ${tarea.texto} - Fecha: ${tarea.fecha}\n"
                else
                    "Tarea: ${tarea.texto}\n"
            )
        }
    }

    val texto = stringBuilder.toString()
    val nombreArchivo = "tareas.txt"
    var mensaje = ""

    if (listaTareas.isEmpty() && listaCompletadas.isEmpty()) {
        mensaje = "No hay tareas para exportar"
    } else {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI

                // Buscar si el archivo ya existe
                val selection = "${MediaStore.Downloads.DISPLAY_NAME} = ?"
                val selectionArgs = arrayOf(nombreArchivo)

                var uriExistente: Uri? = null

                resolver.query(contentUri, arrayOf(MediaStore.Downloads._ID), selection, selectionArgs, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID)
                        val id = cursor.getLong(idColumn)
                        uriExistente = ContentUris.withAppendedId(contentUri, id)
                    }
                }

                // Si existe usar URI, si no, crear una nueva (insert)
                val uriFinal = uriExistente ?: run {
                    val values = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, nombreArchivo)
                        put(MediaStore.Downloads.MIME_TYPE, "text/plain")
                        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                    resolver.insert(contentUri, values)
                }

                // Escribir en el archivo (modo "wt" = Write & Truncate para borrar contenido previo)
                uriFinal?.let { uri ->
                    resolver.openOutputStream(uri, "wt")?.use { output ->
                        output.write(texto.toByteArray())
                    }
                    mensaje = if (uriExistente != null) "Archivo actualizado en Descargas" else "Archivo creado en Descargas"
                } ?: run {
                    mensaje = "Error: No se pudo acceder al archivo"
                }

            } else {
                // Lógica para Android 9 o inferior (Legacy)
                // FileOutputStream por defecto sobrescribe, así que esto ya funcionaba bien
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!dir.exists()) dir.mkdirs()
                val archivo = File(dir, nombreArchivo)
                FileOutputStream(archivo).use { it.write(texto.toByteArray()) }
                mensaje = "Guardado en: ${archivo.absolutePath}"
            }
        } catch (e: Exception) {
            mensaje = "Error al guardar: ${e.message}"
            e.printStackTrace()
        }
    }
    Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
}

fun determinePriority(tarea: Tarea): TaskPriority {
    // 1. PRIMERO: Si ya está hecha, no importa la fecha. Devolvemos COMPLETED y salimos.
    if (tarea.completada) {
        return TaskPriority.COMPLETED
    }

    return try {
        val dateString = tarea.fecha

        // Opcional: Si no hay fecha, definimos una prioridad por defecto (ej. LOW)
        if (dateString.isBlank()) return TaskPriority.UNKNOWN

        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        val dueDate = LocalDate.parse(dateString, formatter)
        val today = LocalDate.now()

        val daysUntil = ChronoUnit.DAYS.between(today, dueDate)

        // 2. Ahora evaluamos el resto de prioridades basadas en el tiempo
        when {
            daysUntil < 0 -> TaskPriority.EXPIRED
            daysUntil <= 7 -> TaskPriority.HIGH
            daysUntil <= 14 -> TaskPriority.MEDIUM
            else -> TaskPriority.LOW
        }
    } catch (e: Exception) {
        // Solo llegamos aquí si la fecha estaba mal formateada
        // e.printStackTrace() // Descomentar para depurar si es necesario
        TaskPriority.UNKNOWN
    }
}

fun scheduleTaskNotification(context: Context, taskName: String, taskDate: String) {
    try {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TaskNotificationReceiver::class.java).apply {
            putExtra("TASK_NAME", taskName)
        }

        // Creamos un ID único basado en el hash del nombre para no sobrescribir alarmas diferentes
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Parseamos la fecha (dd/MM/yyyy)
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        val date = LocalDate.parse(taskDate, formatter)

        // Configuramos la hora a las 9:00 AM
        val dateTime = date.atTime(12, 32)
        val triggerTime = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // Solo programar si la fecha es futura
        if (triggerTime > System.currentTimeMillis()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "No se pudo programar la notificación", Toast.LENGTH_SHORT).show()
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview()
{
    ToDoAppTheme {
        Registrar(onRegistrar = {}, onBack = {})
    }
}