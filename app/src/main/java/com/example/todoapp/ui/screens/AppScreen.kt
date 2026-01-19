package com.example.todoapp.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todoapp.resources.InactivityNotifier
import com.example.todoapp.resources.ShakeDetector
import com.example.todoapp.data.model.Tarea
import com.example.todoapp.data.firebase.TareasViewModel
import com.example.todoapp.resources.TaskPriority
import com.example.todoapp.resources.determinePriority
import com.example.todoapp.resources.scheduleTaskNotification
import com.example.todoapp.ui.AggTareaDialog
import com.example.todoapp.ui.CompletedTasksList
import com.example.todoapp.ui.ConfirmClearDialog
import com.example.todoapp.ui.ConfirmDeleteDialog
import com.example.todoapp.ui.CuentaDialog
import com.example.todoapp.ui.DetailTaskDialog
import com.example.todoapp.ui.EditTaskDialog
import com.example.todoapp.ui.EmptySearchMessage
import com.example.todoapp.ui.EmptyTasksMessage
import com.example.todoapp.ui.HelpDialog
import com.example.todoapp.ui.LogoSmall
import com.example.todoapp.ui.PreferencesDialog
import com.example.todoapp.ui.TaskItem
import com.example.todoapp.ui.TopCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.net.toUri

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
            // Aquí se personaliza el aspecto visual del Snackbar
            Snackbar(
                snackbarData = data,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onBackground,
                actionColor = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier.padding(10.dp)
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
            LogoSmall(100.dp)
            Spacer(Modifier.height(10.dp))

            TopCard(
                viewModel = viewModel,
                listaTareas = tareas,
                onVaciarLista = {
                    if (tareas.isNotEmpty()) showClearDialog = true
                    else Toast.makeText(context, "No hay tareas para vaciar", Toast.LENGTH_SHORT)
                        .show()
                },
                onBack = onBack,
                onPreferences = { showPreferencesDialog.value = true },
                onHelp = { showHelpDialog = true },
                query = searchQuery,
                onQueryChange = { searchQuery = it; },
                onCuenta = { showCuentaDialog = true },
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
                            if (priority == TaskPriority.EXPIRED && !tareaItem.completada)
                            {
                                LaunchedEffect(Unit) {
                                    viewModel.completarTarea(tareaItem)
                                }
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
                                CompletedTasksList(
                                    completedTasks = completadas,
                                    viewModel = viewModel,
                                    scope = scope,
                                    snackbarHostState = snackbarHostState
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
                    CompletedTasksList(
                        completedTasks = completadas,
                        viewModel = viewModel,
                        scope = scope,
                        snackbarHostState = snackbarHostState
                    )
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
                if (nuevaTarea.isNotBlank()) {
                    viewModel.agregarTarea(nuevaTarea, nuevaFecha)
                    if (nuevaFecha.isNotBlank()) {
                        scheduleTaskNotification(context, nuevaTarea, nuevaFecha)
                    }
                    tarea = ""
                    fecha = ""
                    Toast.makeText(context, "Tarea agregada correctamente", Toast.LENGTH_SHORT)
                        .show()
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
                viewModel.editarTarea(tareaToEdit, nuevoTexto, nuevaFecha)
                if (nuevaFecha.isNotBlank()) {
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
                tareaAEliminar?.let { taskToDelete ->
                    ultimaTareaEliminada = taskToDelete
                    viewModel.eliminarTarea(taskToDelete)
                    scope.launch {
                        val result = snackbarHostState
                            .showSnackbar(
                                message = "Tarea eliminada. \nClickea o agita para deshacer.",
                                actionLabel = "Deshacer",
                                duration = SnackbarDuration.Short
                            )
                        when (result) {
                            SnackbarResult.ActionPerformed -> {
                                viewModel.restaurarTarea(taskToDelete)
                            }

                            SnackbarResult.Dismissed -> {}
                        }
                    }
                }
                // Limpiamos el estado al final
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
                Toast.makeText(context, "La lista de tareas ha sido vaciada", Toast.LENGTH_SHORT)
                    .show()
            }
        )
    }

    if (showCuentaDialog)
    {
        CuentaDialog(usuario = datosUsuario, onDismiss = { showCuentaDialog = false })
    }

    if (tareaDetallada != null) {
        DetailTaskDialog(
            tarea = tareaDetallada!!,
            onDismiss = { tareaDetallada = null },
            onEditar = { tareaEditando = tareaDetallada!! },
            onCompletar = {
                viewModel.completarTarea(tareaDetallada!!)
                tareaDetallada = null
            })
    }
    if (showHelpDialog) {
        HelpDialog(onDismiss = {showHelpDialog = false},
            onGithub = {
                val intent = Intent(Intent.ACTION_VIEW, "https://github.com/menonisebastian".toUri())
                context.startActivity(intent)
            })
    }
}