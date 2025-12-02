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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDatePickerState
import androidx.lifecycle.viewmodel.compose.viewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat

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
            Login(onEnviar = { nombre, alias ->
                navController.currentBackStackEntry?.savedStateHandle?.apply {
                    set("nombre", nombre)
                    set("alias", alias)
                }
                navController.navigate("app")
            })
        }
        composable("app") {
            val prev = navController.previousBackStackEntry?.savedStateHandle
            val nombre = prev?.get<String>("nombre").orEmpty()
            val alias = prev?.get<String>("alias").orEmpty()

            App(
                nombre = nombre,
                alias = alias,
                onBack = { navController.popBackStack() },
                taskTextColor = taskTextColor,
                viewModel = viewModel
            )
        }
    }
}

// ============ LOGIN SCREEN ============
@Composable
fun Login(onEnviar: (String, String) -> Unit)
{
    var nombres by remember { mutableStateOf("") }
    var alias by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(150.dp))
        Image(
            painter = painterResource(R.drawable.cutlogoapp),
            modifier = Modifier.size(90.dp).padding(10.dp),
            contentDescription = "Logo"
        )
        Image(
            painter = painterResource(R.drawable.fontlogo),
            modifier = Modifier.width(200.dp).padding(top = 10.dp),
            contentDescription = "logo texto"
        )

        Spacer(Modifier.height(20.dp))
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
                shape = RoundedCornerShape(20.dp),
                label = { Text("Nombre") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.inversePrimary
                )
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = alias,
                onValueChange = { alias = it },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                label = { Text("Alias") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.inversePrimary
                )
            )

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = {
                    if (nombres.isNotBlank() && alias.isNotBlank()) {
                        onEnviar(nombres, alias)
                    } else {
                        Toast.makeText(context, "Introduce nombre y alias para continuar", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = "Continuar", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ============ MAIN APP SCREEN ============
@Composable
fun App(
    nombre: String,
    alias: String,
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
    val showPreferencesDialog = remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var tareaDetallada by remember { mutableStateOf<Tarea?>(null) }

    val filteredTareas = if (searchQuery.isBlank()) {
        tareas
    } else {
        tareas.filter { it.texto.contains(searchQuery, ignoreCase = true) }
    }

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
                containerColor = MaterialTheme.colorScheme.primary
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
                alias = alias,
                tarea = tarea,
                listaTareas = tareas,
                onTareaChange = { tarea = it },
                onAddTarea = {
                    if (tarea.isNotBlank()) {
                        viewModel.agregarTarea(tarea, "")
                        tarea = ""
                        Toast.makeText(context, "Tarea agregada correctamente", Toast.LENGTH_SHORT).show()
                    }
                },
                onVaciarLista = {
                    if (tareas.isNotEmpty()) showClearDialog = true
                    else Toast.makeText(context, "No hay tareas para vaciar", Toast.LENGTH_SHORT).show()
                },
                onBack = onBack,
                onPreferences = { showPreferencesDialog.value = true },
                onHelp = { showHelpDialog = true }
            )

            if (tareas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                CustomizableSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                )
                Spacer(modifier = Modifier.height(20.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    if (filteredTareas.isEmpty()) {
                        item { EmptySearchMessage() }
                    } else {
                        items(filteredTareas, key = { it.id }) { tareaItem ->
                            TaskItem(
                                tarea = tareaItem,
                                onTaskClick = { tareaDetallada = tareaItem },
                                onEdit = { tareaEditando = tareaItem },
                                textColor = taskTextColor,
                                onDelete = { tareaAEliminar = tareaItem }
                            )
                            Spacer(Modifier.height(10.dp))
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

    if (tareaDetallada != null) {
        DetailTaskDialog(tarea = tareaDetallada!!, onDismiss = { tareaDetallada = null })
    }
    if (showHelpDialog) { HelpDialog(onDismiss = { showHelpDialog = false }) }
}

// ============ UI COMPONENTS ============
// (Puedes mover estos también a un archivo Components.kt si quisieras limpiar aún más)

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
                { Text("Aceptar") }
            },
            dismissButton = { Button(onClick = { showDatePicker = false }) { Text("Cancelar") } }
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
                shape = RoundedCornerShape(20.dp))

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = fecha,
                onValueChange = onFechaChange,
                label = { Text("Fecha (Opcional)") },
                placeholder = { Text("dd/MM/yyyy") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = {
                        showDatePicker = true
                    }
                    )
                    {
                        Icon(imageVector = Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
                    }
               },
                shape = RoundedCornerShape(20.dp)
            )
            Spacer(Modifier.height(20.dp))

            IconButton(onClick = {
                if (tarea.isNotBlank())
                {
                    onAddTarea(tarea, fecha)
                }
                else
                {
                    Toast.makeText(context, "La descripción de la tarea no puede estar vacía", Toast.LENGTH_SHORT).show()
                }
            },
                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary))
            {
                Icon(Icons.Outlined.Add, contentDescription = "Añadir", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
fun TopCard(
    nombre: String,
    alias: String,
    tarea: String,
    listaTareas: List<Tarea>,
    onTareaChange: (String) -> Unit,
    onAddTarea: () -> Unit,
    onVaciarLista: () -> Unit,
    onBack: () -> Unit,
    onPreferences: () -> Unit,
    onHelp: () -> Unit
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
            Text(text = "Hola, $nombre ($alias)",
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
                        text = { Text("Preferencias") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Settings, contentDescription = null)
                        },
                        onClick = { expanded = false; onPreferences() })
                    HorizontalDivider()

                    DropdownMenuItem(
                        text = { Text("Ayuda") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Info, contentDescription = null)
                            },
                        onClick = { onHelp() })
                    HorizontalDivider()

                    DropdownMenuItem(
                        text = { Text("Exportar tareas") },
                        leadingIcon = { Icon(Icons.Outlined.CheckCircle, contentDescription = null) },
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
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically)
        {
            OutlinedTextField(
                value = tarea,
                onValueChange = onTareaChange,
                label = { Text("Nueva tarea") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary),
                singleLine = true)

            Spacer(modifier = Modifier.width(5.dp))

            IconButton(
                onClick = onAddTarea,
                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary))
            {
                Icon(Icons.Outlined.Add,
                contentDescription = "Añadir",
                tint = MaterialTheme.colorScheme.onPrimary)
            }
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
            containerColor = MaterialTheme.colorScheme.surface,
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
                    Text(text = tarea.fecha, color = MaterialTheme.colorScheme.inversePrimary)
                }
            } else
                Text(text = tarea.texto, color = textColor)
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
            modifier = Modifier.padding(vertical = 20.dp))
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tarea  #${tarea.id}") },
        text =
            {
                Column{
                    Text(text = tarea.texto,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface)
                    if (tarea.fecha.isNotBlank())
                    {
                        Spacer(modifier = Modifier
                            .height(10.dp))
                        Text(text = tarea.fecha, color = MaterialTheme.colorScheme.inversePrimary)
                    }
                }
            },
        confirmButton =
            {
                TextButton(onClick = onDismiss)
                {
                    Text("Listo",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 20.sp)
                }
            },
        containerColor = MaterialTheme.colorScheme.surface,
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
    onSave: (String, String) -> Unit // Cambiado para aceptar texto y fecha
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
                    shape = RoundedCornerShape(20.dp),
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
                    shape = RoundedCornerShape(20.dp),
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
    listaTareas.forEach { tarea -> stringBuilder.append("ID: ${tarea.id} - Tarea: ${tarea.texto} - Fecha: ${tarea.fecha}\n") }
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview()
{

}