@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.todoapp

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import java.util.Locale
import android.hardware.Sensor
import android.hardware.SensorManager
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.viewmodel.compose.viewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// ============ ACTIVITY ============
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent{
            val settingsPreferences = remember { SettingsPreferences(applicationContext) }

            // Instancia del ViewModel conectada a la BD
            val viewModel: TareasViewModel = viewModel(
                factory = TareasViewModelFactory(application)
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
            Login(onEnviar = { nombre, pass ->
                navController.currentBackStackEntry?.savedStateHandle?.apply {
                    set("nombre", nombre.trim())
                    set("pass", pass)
                }
                navController.navigate("app")
            },
                onRegistrar = {
                    navController.navigate("register")}
            )
        }

        composable("register") {
            Registrar(onRegistrar = { listaDatos ->
                navController.currentBackStackEntry?.savedStateHandle?.apply {
                    set("listaDatos", listaDatos)
                }
                navController.navigate("login")
            },
                onBack = { navController.popBackStack() })
        }
        composable("app") {
            val prev = navController.previousBackStackEntry?.savedStateHandle
            val nombre = prev?.get<String>("nombre").orEmpty()

            App(
                nombre = nombre,
                onBack = { navController.popBackStack() },
                taskTextColor = taskTextColor,
                viewModel = viewModel
            )
        }
    }
}

// ============ LOGIN SCREEN ============
@Composable
fun Login(onEnviar: (String, String) -> Unit, onRegistrar: () -> Unit)
{
    var nombres by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold()
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
            //Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
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
            Spacer(Modifier.height(150.dp))
            Column(
                modifier = Modifier
                    .shadow(15.dp, RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
                    .padding(20.dp)
                    .width(300.dp),
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = nombres,
                    onValueChange = { nombres = it },
                    singleLine = true,
                    shape = RoundedCornerShape(30.dp),
                    label = { Text("Usuario") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.inversePrimary
                    ),
                    modifier = Modifier.padding(top = 20.dp),
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Usuario") }
                )
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(
                    value = pass,
                    onValueChange = { pass = it },
                    singleLine = true,
                    shape = RoundedCornerShape(30.dp),
                    label = { Text("Contraseña") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.inversePrimary
                    ),
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Contraseña") },
                    trailingIcon =
                        {
                            if (pass.isNotBlank())
                            {
                                IconButton(onClick = {
                                    showPassword = !showPassword
                                }
                                ) {
                                    if (!showPassword)
                                        Icon(Icons.Default.Visibility, contentDescription = "Limpiar", tint = MaterialTheme.colorScheme.inversePrimary)
                                    else
                                        Icon(Icons.Default.VisibilityOff, contentDescription = "Limpiar", tint = MaterialTheme.colorScheme.inversePrimary)
                                    /*
                                    Iconos de material-icons-extended

                                    TOML:
                                    materialIconsExtended = "1.7.8"
                                    androidx-compose-material-icons-extended = { module = "androidx.compose.material:material-icons-extended", version.ref = "materialIconsExtended" }

                                    BUILD.GRADLE:
                                    implementation(libs.androidx.compose.material.icons.extended)*/
                                }
                            }
                        }
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (nombres.isNotBlank() && pass.isNotBlank()) {
                            showDialog = true
                        } else {
                            Toast.makeText(context, "Introduce usuario y contraseña para continuar", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(text = "Iniciar sesión", fontWeight = FontWeight.Bold)
                }

                TextButton(onClick = {
                    onRegistrar()
                },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.secondary))
                { Text("Registrarme") }
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
                Text("INICIO DE SESIÓN EXITOSO", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 18.sp, fontStyle = FontStyle.Italic)
                Spacer(Modifier.height(20.dp))
                Icon(Icons.Default.CheckCircle, contentDescription = "ok", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(60.dp))
            }
            LaunchedEffect(Unit) {
                delay(1000) // Espera
                showDialog = false // Cierra el diálogo cambiando el estado
                onEnviar(nombres, pass)
            }
        }
    }
}

// ============ REGISTER SCREEN ============
@Composable
fun Registrar(onRegistrar: (List<String>) -> Unit, onBack: () -> Unit)
{
    var nombres by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var passConf by remember { mutableStateOf("") }
    val fechaAlta = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    val listaDatos = listOf(nombres, email, userName, pass, fechaAlta)
    var showPassword by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    val formularioValido = nombres.isNotBlank() && email.isNotBlank() &&
            userName.isNotBlank() && pass.isNotBlank() &&
            passConf.isNotBlank()
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
            shape = RoundedCornerShape(10.dp)
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
                //Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
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
                    TextField(
                        value = nombres,
                        onValueChange = { nombres = it },
                        singleLine = true,
                        shape = RoundedCornerShape(30.dp),
                        label = { Text("Nombre") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.inversePrimary
                        ),
                        leadingIcon = { Icon(Icons.Default.AppRegistration, contentDescription = "Nombre") }
                    )
                    Spacer(Modifier.height(20.dp))
                    TextField(
                        value = userName,
                        onValueChange = { userName = it },
                        singleLine = true,
                        shape = RoundedCornerShape(30.dp),
                        label = { Text("Usuario") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.inversePrimary
                        ),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Usuario") }
                    )
                    Spacer(Modifier.height(20.dp))
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        singleLine = true,
                        shape = RoundedCornerShape(30.dp),
                        label = { Text("Email") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.inversePrimary
                        ),
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") }
                    )
                    Spacer(Modifier.height(20.dp))
                    TextField(
                        value = pass,
                        onValueChange = { pass = it },
                        singleLine = true,
                        shape = RoundedCornerShape(30.dp),
                        label = { Text("Contraseña") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.inversePrimary
                        ),
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Contraseña") },
                        trailingIcon =
                            {
                                if (pass.isNotBlank())
                                {
                                    IconButton(onClick = {
                                        showPassword = !showPassword
                                    }
                                    ) {
                                        if (!showPassword)
                                            Icon(Icons.Default.Visibility, contentDescription = "Limpiar", tint = MaterialTheme.colorScheme.inversePrimary)
                                        else
                                            Icon(Icons.Default.VisibilityOff, contentDescription = "Limpiar", tint = MaterialTheme.colorScheme.inversePrimary)
                                    }
                                }
                            }
                    )
                    Spacer(Modifier.height(20.dp))
                    TextField(
                        value = passConf,
                        onValueChange = { passConf = it },
                        singleLine = true,
                        shape = RoundedCornerShape(30.dp),
                        label = { Text("Confirmar contraseña") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.inversePrimary
                        ),
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Contraseña") },
                        trailingIcon =
                            {
                                if (passConf.isNotBlank())
                                {
                                    IconButton(onClick = {
                                        showPassword = !showPassword
                                    }
                                    ) {
                                        if (!showPassword)
                                            Icon(Icons.Default.Visibility, contentDescription = "Limpiar", tint = MaterialTheme.colorScheme.inversePrimary)
                                        else
                                            Icon(Icons.Default.VisibilityOff, contentDescription = "Limpiar", tint = MaterialTheme.colorScheme.inversePrimary)
                                    }
                                }
                            }
                    )
                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (pass != passConf)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Las contraseñas no coinciden",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            else
                                showDialog = true
                        },
                        enabled = formularioValido,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = Color.Gray
                        )
                    ){
                        Text(text = "Crear cuenta", fontWeight = FontWeight.Bold)
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
                onRegistrar(listaDatos)
            }
        }
    }
}

// ============ MAIN APP SCREEN ============
@Composable
fun App(
    nombre: String,
    taskTextColor: Color,
    onBack: () -> Unit,
    viewModel: TareasViewModel
) {
    // VARIABLES LOCALES
    var tarea by remember { mutableStateOf("") }

    // OBSERVAMOS LA BD DESDE EL VIEWMODEL
    val tareas by viewModel.listaTareas.collectAsStateWithLifecycle()

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
        }
    ) { paddingValues ->
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
                nombre = nombre,
                listaTareas = tareas,
                onVaciarLista = {
                    if (tareas.isNotEmpty()) showClearDialog = true
                    else Toast.makeText(context, "No hay tareas para vaciar", Toast.LENGTH_SHORT).show()
                },
                onBack = onBack,
                onPreferences = { showPreferencesDialog.value = true },
                onHelp = { showHelpDialog = true },
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onCuenta = { showCuentaDialog = true}
            )

            if (tareas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    if (filteredTareas.isEmpty()) {
                        item { EmptySearchMessage() }
                    } else {
                        items(filteredTareas, key = { it.id }) { tareaItem ->
                            Spacer(Modifier.height(10.dp))
                            TaskItem(
                                tarea = tareaItem,
                                onTaskClick = { tareaDetallada = tareaItem },
                                onEdit = { tareaEditando = tareaItem },
                                textColor = taskTextColor,
                                onDelete = { tareaAEliminar = tareaItem }
                            )
                        }
                    }
                }
            } else {
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
                Toast.makeText(context, "Tarea eliminada. Agita para deshacer.", Toast.LENGTH_SHORT).show()
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
        CuentaDialog(onDismiss = { showCuentaDialog = false })
    }

    if (tareaDetallada != null) {
        DetailTaskDialog(tarea = tareaDetallada!!, onDismiss = { tareaDetallada = null })
    }
    if (showHelpDialog) { HelpDialog(onDismiss = { showHelpDialog = false }) }
}

// ============ UI COMPONENTES ============ //
// (Se pueden mover estos también a un archivo Components.kt para limpiar el codigo)

// ============ PREFERENCIAS DEL USUARIO ============ //

@Composable
fun PreferencesDialog(onDismiss: () -> Unit) {
    val colorTexto = remember { mutableListOf("Naranja", "Azul", "Dinamico") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settingsPreferences = remember { SettingsPreferences(context) }
    val isDarkMode by settingsPreferences.isDarkMode.collectAsStateWithLifecycle(initialValue = false)
    val colorSeleccionado by settingsPreferences.taskTextColor.collectAsStateWithLifecycle(initialValue = "Default")

    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier.background(MaterialTheme.colorScheme.background,
                RoundedCornerShape(20.dp))
                .padding(horizontal = 60.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(painter = painterResource(id = R.drawable.fontlogo),
                modifier = Modifier
                    .width(80.dp)
                    .padding(top = 10.dp),
                contentDescription = "logo texto")

            Text("Preferencias",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 15.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.shadow(10.dp,
                    RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(10.dp))
                    .padding(10.dp)
                    .width(180.dp))
            {
                Text("Colores del Texto",
                    modifier = Modifier.padding(10.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface)

                colorTexto.forEach { color ->
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 20.dp).width(120.dp))
                    {
                        RadioButton(
                            selected = colorSeleccionado == color,
                            onClick = { scope.launch { settingsPreferences.setTaskTextColor(color) } },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = when (color) {
                                    "Naranja" -> MaterialTheme.colorScheme.primary
                                    "Azul" -> MaterialTheme.colorScheme.secondary
                                    "Dinamico" -> MaterialTheme.colorScheme.onSurface
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        )
                        Text(color, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            Column (horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.shadow(10.dp, RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(10.dp))
                    .padding(10.dp)
                    .width(180.dp))
            {
                Text(text = "Modo Oscuro",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(top = 10.dp).padding(horizontal = 20.dp))

                Switch(checked = isDarkMode,
                    onCheckedChange = { nuevoValor -> scope.launch { settingsPreferences.setDarkMode(nuevoValor) } },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onSurface,
                        checkedTrackColor = MaterialTheme.colorScheme.background,
                        checkedBorderColor = MaterialTheme.colorScheme.onSurface,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                        uncheckedTrackColor = MaterialTheme.colorScheme.background,
                        uncheckedBorderColor = MaterialTheme.colorScheme.onSurface))
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = { onDismiss() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary))
            { Text("Cerrar") }
        }
    }
}

// ============ CUENTA DEL USUARIO ============ //

@Composable
fun CuentaDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier.background(MaterialTheme.colorScheme.background,
                RoundedCornerShape(20.dp))
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(painter = painterResource(id = R.drawable.fontlogo),
                modifier = Modifier
                    .width(80.dp)
                    .padding(top = 10.dp),
                contentDescription = "logo texto")

            Text("Datos Personales",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 15.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.shadow(10.dp,
                    RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(20.dp))
                    .padding(20.dp))
            {
                // 1. Nombre
                OutlinedTextField(
                    value = "Sebastián Menoni",
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = Color.Transparent, // Borde visible
                        disabledLabelColor = MaterialTheme.colorScheme.inversePrimary, // Label legible
                        disabledContainerColor = Color.Transparent // Fondo transparente
                    )
                )

                Spacer(modifier = Modifier.height(5.dp))

                // 2. Usuario
                OutlinedTextField(
                    value = "arturomenoni",
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Usuario") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = Color.Transparent,
                        disabledLabelColor = MaterialTheme.colorScheme.inversePrimary
                    )
                )

                Spacer(modifier = Modifier.height(5.dp))

                // 3. Email
                OutlinedTextField(
                    value = "arturomenoni@gmail.com",
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = Color.Transparent,
                        disabledLabelColor = MaterialTheme.colorScheme.inversePrimary
                    )
                )

                Spacer(modifier = Modifier.height(5.dp))

                // 4. Fecha de Registro
                OutlinedTextField(
                    value = "10/11/2025",
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Fecha de Registro") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = Color.Transparent,
                        disabledLabelColor = MaterialTheme.colorScheme.inversePrimary
                    )
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth())
            {
                Column (horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.shadow(10.dp, RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(20.dp))
                        .padding(10.dp)
                        .width(100.dp))
                {
                    Text(text = "Editar",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(top = 10.dp).padding(horizontal = 20.dp))

                    IconButton(onClick = {})
                    {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Eliminar Cuenta",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Column (horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.shadow(10.dp, RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(20.dp))
                        .padding(10.dp)
                        .width(100.dp))
                {
                    Text(text = "Eliminar",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(top = 10.dp).padding(horizontal = 20.dp))

                    IconButton(onClick = {})
                    {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar Cuenta",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = { onDismiss() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary))
            { Text("Cerrar") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AggTareaDialog(
    onDismiss: () -> Unit,
    tarea: String,
    onTareaChange: (String) -> Unit,
    fecha: String,
    onFechaChange: (String) -> Unit,
    onAddTarea: (String, String) -> Unit
) {
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis =
        System.currentTimeMillis(),
        selectableDates = object : SelectableDates
        {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean
            { return utcTimeMillis >= System.currentTimeMillis() - 86400000 }
        }
    )

    if (showDatePicker)
    {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick =
                    {
                        showDatePicker = false
                        datePickerState.selectedDateMillis?.let {
                            millis -> val sdf = SimpleDateFormat("dd/MM/yyyy",
                            Locale.getDefault())
                            onFechaChange(sdf.format(millis))
                        }
                    }
                )
                { Icon(Icons.Filled.Check, contentDescription = "Aceptar", tint = MaterialTheme.colorScheme.onPrimary) }
            },
            dismissButton = { Button(onClick = { showDatePicker = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.inversePrimary))
            { Icon(Icons.Filled.Close, contentDescription = "Cancelar", tint = MaterialTheme.colorScheme.onPrimary) } }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text("Seleccionar fecha",
                    modifier = Modifier
                        .padding(start = 24.dp, end = 12.dp, top = 16.dp, bottom = 12.dp)
                    )
                }
            )
        }
    }

    Dialog(onDismissRequest = onDismiss)
    {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(20.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally)
        {
            Image(
                painter = painterResource(id = R.drawable.fontlogo),
                modifier = Modifier.width(80.dp).padding(top = 10.dp, bottom = 20.dp),
                contentDescription = "logo texto")

            Text("Añadir Tarea",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 15.dp),
                color = MaterialTheme.colorScheme.onSurface)

            OutlinedTextField(
                value = tarea,
                onValueChange = onTareaChange,
                label = { Text("Describe tu tarea") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                singleLine = true)

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = fecha,
                onValueChange = onFechaChange,
                label = { Text("Fecha (Opcional)") },
                placeholder = { Text("dd/MM/yyyy") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true })
                    {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Seleccionar fecha")
                    }
               },
                shape = RoundedCornerShape(30.dp)
            )
            Spacer(Modifier.height(20.dp))

            Row()
            {
                IconButton(onClick = { onDismiss() },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.inversePrimary), modifier = Modifier.size(60.dp))
                {
                    Icon(Icons.Filled.Close, contentDescription = "Cancelar", tint = MaterialTheme.colorScheme.onPrimary)
                }

                Spacer(modifier = Modifier.width(20.dp))

                IconButton(onClick = {
                    if (tarea.isNotBlank())
                    {
                        onAddTarea(tarea.trim(), fecha)
                    }
                    else
                    {
                        Toast.makeText(context, "La descripción de la tarea no puede estar vacía", Toast.LENGTH_SHORT).show()
                    }
                },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary), modifier = Modifier.size(60.dp))
                {
                    Icon(Icons.Filled.Check, contentDescription = "Añadir", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@Composable
fun TopCard(
    nombre: String,
    listaTareas: List<Tarea>,
    onVaciarLista: () -> Unit,
    onBack: () -> Unit,
    onPreferences: () -> Unit,
    onCuenta: () -> Unit,
    onHelp: () -> Unit,
    query: String,                   // <--- Nuevo parámetro
    onQueryChange: (String) -> Unit // <--- Nuevo parámetro
)
{
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth()
        .shadow(15.dp, RoundedCornerShape(15.dp))
        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
        .padding(horizontal = 20.dp, vertical = 10.dp))
    {
        Row(verticalAlignment = Alignment.CenterVertically)
        {
            Text(text = "Hola $nombre!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface)

            Spacer(Modifier.weight(1f))

            IconButton(onClick = { expanded = !expanded })
            {
                Icon(Icons.Outlined.MoreVert,
                    contentDescription = "Preferencias",
                    tint = MaterialTheme.colorScheme.onSurface)
                DropdownMenu(expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                )
                {
                    DropdownMenuItem(
                        text = { Text("Cuenta") },
                        leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                        onClick = { expanded = false; onCuenta() })
                    HorizontalDivider()

                    DropdownMenuItem(
                        text = { Text("Preferencias") },
                        leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                        onClick = { expanded = false; onPreferences() })
                    HorizontalDivider()

                    DropdownMenuItem(
                        text = { Text("Ayuda") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null) },
                        onClick = { onHelp() }
                    )
                    HorizontalDivider()

                    DropdownMenuItem(
                        text = { Text("Exportar tareas") },
                        leadingIcon = { Icon(Icons.Outlined.SaveAlt, contentDescription = null) },
                        onClick = { exportarTareas(context, listaTareas); expanded = false })
                    HorizontalDivider()

                    DropdownMenuItem(
                        text = { Text("Vaciar lista") },
                        leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
                        onClick = { onVaciarLista(); expanded = false })
                    HorizontalDivider()

                    DropdownMenuItem(
                        text = { Text("Salir") },
                        leadingIcon = { Icon(Icons.Outlined.Close, contentDescription = null) },
                        onClick = { onBack() })
                }
            }
        }
        // --- Barra de búsqueda insertada aquí ---
        if (listaTareas.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            CustomizableSearchBar(
                query = query,
                onQueryChange = onQueryChange
            )
            Spacer(modifier = Modifier.height(5.dp)) // Un pequeño margen inferior opcional
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizableSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier)
{
    DockedSearchBar(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = {},
        active = false,
        onActiveChange = {},
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text("Buscar tarea",
            color = Color.Gray)
        },
        leadingIcon = {
            Icon(Icons.Default.Search,
            contentDescription = "Search")
        },
        trailingIcon = { if (query.isNotEmpty())
        {
            IconButton(
                onClick = { onQueryChange("") })
            {
                Icon(Icons.Default.Close, contentDescription = "Clear search")
            }
        }
    }, colors = SearchBarDefaults.colors(
            containerColor = MaterialTheme.colorScheme.background,
            dividerColor = Color.Transparent),
        shadowElevation = 10.dp)
    {}
}

@Composable
fun TaskItem(
    tarea: Tarea,
    textColor: Color,
    onTaskClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit)
{
    Card(modifier = Modifier
        .fillMaxWidth()
        .clickable { onTaskClick() },
        elevation = CardDefaults.cardElevation(5.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface))
    {
        Row(modifier = Modifier.padding(vertical = 10.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically)
        {
            if (tarea.fecha.isNotBlank())
            {
                Column{
                    Text(text = tarea.texto, color = textColor)
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(text = tarea.fecha, color = MaterialTheme.colorScheme.inversePrimary, fontSize = 12.sp)
                }
            }
            else
            {
                Text(text = tarea.texto, color = textColor)
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Eliminar") }
        }
    }
}

@Composable
fun EmptyTasksMessage() {
    Column(modifier = Modifier
        .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center)
    {
        Text("Tu lista de tareas está vacía", fontSize = 20.sp, fontStyle = FontStyle.Italic, color = Color.Gray)
    }
}

@Composable
fun EmptySearchMessage() {
    Column(modifier = Modifier
        .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center)
    {
        Text("No se ha encontrado la tarea",
            fontSize = 20.sp,
            fontStyle = FontStyle.Italic,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 50.dp))
    }
}

@Composable
fun ConfirmClearDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss,
        title = {
            Text("Vaciar lista de tareas")
        },
        text = { Text("¿Estás seguro de que quieres eliminar todas las tareas? " +
                "\nEsta acción NO se puede deshacer.")
               },
        confirmButton =
        {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary)
            )
            {
                Text("Aceptar")
            }
        },
        dismissButton =
        {
            TextButton(onClick = onDismiss)
            { Text("Cancelar",
                color = MaterialTheme.colorScheme.secondary)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp)
    )
}

@Composable
fun DetailTaskDialog(tarea: Tarea, onDismiss: () -> Unit)
{
    val priority = determinePriority(tarea.fecha)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Card(elevation = CardDefaults.cardElevation(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface))
            {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(10.dp))
                {
                    Text("ID: ${tarea.id}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    if (tarea.fecha.isNotBlank())
                    {
                        Spacer(modifier = Modifier.weight(1f))
                        PriorityChip(priority)
                    }
                }
            }
        },
        text =
            {
                Column{
                    Text(text = tarea.texto,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(10.dp))
                    if (tarea.fecha.isNotBlank())
                    {
                        Text(text = tarea.fecha,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.inversePrimary,
                            modifier = Modifier.padding(10.dp))
                    }
                }
            },
        confirmButton =
            {
                TextButton(onClick = onDismiss)
                {
                    Text("Listo",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 20.sp)
                }
            },
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(10.dp))
}

@Composable
fun ConfirmDeleteDialog(onDismiss: () -> Unit, onConfirm: () -> Unit)
{
    AlertDialog(
        onDismissRequest = onDismiss,
        title =
        {
            Text("Eliminar tarea",
                color = MaterialTheme.colorScheme.onSurface)
        },
        text =
        {
            Text("¿Estás seguro de que quieres eliminar la tarea? " +
                    "\nEsta acción se puede deshacer al agitar el dispositivo.",
                color = MaterialTheme.colorScheme.onSurface)
        },
        confirmButton =
        {
            Button(onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary)
            )
            {
                Text("Aceptar")
            }
        },
        dismissButton =
        {
            TextButton(onClick = onDismiss)
            {
                Text("Cancelar",
                    color = MaterialTheme.colorScheme.secondary)
            }
       },
        containerColor = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(10.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskDialog(
    tarea: Tarea,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit // texto y fecha
) {
    var textoEditado by remember(tarea) { mutableStateOf(tarea.texto) }
    var fechaEditada by remember(tarea) { mutableStateOf(tarea.fecha) }

    var showDatePicker by remember { mutableStateOf(false) }

    // Configuramos el DatePicker con la fecha actual de la tarea o el sistema si no hay
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = if (fechaEditada.isNotBlank()) {
            try {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fechaEditada)?.time ?: System.currentTimeMillis()
            } catch (e: Exception) { System.currentTimeMillis() }
        } else System.currentTimeMillis(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= System.currentTimeMillis() - 86400000
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    showDatePicker = false
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        fechaEditada = sdf.format(millis)
                    }
                }) { Text("Aceptar") }
            },
            dismissButton = { Button(onClick = { showDatePicker = false }) { Text("Cancelar") } }
        ) {
            DatePicker(
                state = datePickerState,
                title = { Text("Seleccionar fecha", modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp, bottom = 12.dp)) }
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(text = "Editar tarea", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        },
        text = {
            Column {
                OutlinedTextField(
                    value = textoEditado,
                    onValueChange = { textoEditado = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(30.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.secondary,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = Color.Gray
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Campo editable para la fecha
                OutlinedTextField(
                    value = fechaEditada,
                    onValueChange = { fechaEditada = it },
                    label = { Text("Fecha") },
                    placeholder = { Text("dd/MM/yyyy") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(imageVector = Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
                        }
                    },
                    shape = RoundedCornerShape(30.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.secondary,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = Color.Gray
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (textoEditado.isNotBlank()) {
                        onSave(textoEditado, fechaEditada) // Enviamos ambos valores
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = MaterialTheme.colorScheme.secondary) }
        },
        shape = RoundedCornerShape(10.dp)
    )
}

@Composable
fun HelpDialog(onDismiss: () -> Unit)
{
    Dialog(onDismissRequest = onDismiss)
    {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(20.dp))
                .padding(10.dp))
        {
            Text("Ayuda",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(top = 10.dp))

            Text("AYUDAAAAAAAAAAAAAAAAAAAA",
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(10.dp),
                fontSize = 16.sp)

            Image(painter = painterResource(id = R.drawable.ayuda),
                modifier = Modifier
                    .width(80.dp)
                    .padding(vertical = 10.dp),
                contentDescription = "logo texto")
            TextButton(onClick = onDismiss) { Text("Cerrar", color = MaterialTheme.colorScheme.secondary) }
        }
    }
}

fun exportarTareas(context: Context, listaTareas: List<Tarea>) {
    val stringBuilder = StringBuilder()
    listaTareas.forEach { tarea -> stringBuilder.append(
        if (tarea.fecha.isNotBlank())
            "ID: ${tarea.id} - Tarea: ${tarea.texto} - Fecha: ${tarea.fecha}\n"
        else
            "ID: ${tarea.id} - Tarea: ${tarea.texto}\n"
    )
    }
    val texto = stringBuilder.toString()
    val nombreArchivo = "tareas.txt"
    var mensaje = ""
    if (listaTareas.isEmpty()) mensaje = "No hay tareas para exportar"
    else {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, nombreArchivo)
                    put(MediaStore.Downloads.MIME_TYPE, "text/plain")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                uri?.let {
                    outputUri -> resolver.openOutputStream(outputUri)?.use {
                    output -> output.write(texto.toByteArray())
                }
                    mensaje = "Guardado exitosamente en Descargas"
                } ?: run { mensaje = "Error: No se pudo crear el archivo URI" }
            }
            else
            {
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!dir.exists()) dir.mkdirs()
                val archivo = File(dir, nombreArchivo)
                FileOutputStream(archivo).use { it.write(texto.toByteArray()) }
                mensaje = "Guardado en: ${archivo.absolutePath}"
            }
        } catch (e: Exception) { mensaje = "Error al guardar: ${e.message}"; e.printStackTrace() }
    }
    Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
}

fun determinePriority(dateString: String): TaskPriority {
    return try {
        // 1. Definir el formato esperado de la fecha (ej: 25/12/2023)
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        // 2. Parsear el string a LocalDate
        val dueDate = LocalDate.parse(dateString, formatter)
        val today = LocalDate.now()

        // 3. Calcular diferencia de días
        val daysUntil = ChronoUnit.DAYS.between(today, dueDate)

        // 4. Determinar prioridad
        when {
            daysUntil < 0 -> TaskPriority.EXPIRED  // Tarea vencida (Urgente)
            daysUntil <= 7 -> TaskPriority.HIGH // Faltan 7 días o menos
            daysUntil <= 14 -> TaskPriority.MEDIUM // Falta dos semanas o menos
            else -> TaskPriority.LOW            // Falta más de dos semanas
        }
    } catch (e: Exception) {
        // Si el string no tiene el formato correcto
        TaskPriority.UNKNOWN
    }
}

@Composable
fun PriorityChip(priority: TaskPriority) {
    Box(
        modifier = Modifier
            .background(
                color = priority.color.copy(alpha = 0.2f), // Fondo suave
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .width(if (priority == TaskPriority.EXPIRED) 50.dp else 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = priority.label,
            color = priority.color, // Texto del color fuerte
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
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