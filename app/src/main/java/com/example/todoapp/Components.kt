package com.example.todoapp

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.ArrowDropUp
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SaveAlt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

// ============ UI COMPONENTES ============ //

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
            Modifier
                .background(
                    MaterialTheme.colorScheme.background,
                    RoundedCornerShape(20.dp)
                )
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
                modifier = Modifier
                    .shadow(
                        10.dp,
                        RoundedCornerShape(10.dp)
                    )
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(10.dp)
                    )
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
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .width(120.dp))
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
                modifier = Modifier
                    .shadow(10.dp, RoundedCornerShape(10.dp))
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(10.dp)
                    )
                    .padding(10.dp)
                    .width(180.dp))
            {
                Text(text = "Modo Oscuro",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .padding(horizontal = 20.dp))

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
fun CuentaDialog(onDismiss: () -> Unit, usuario: User?) {

    // 1. LÓGICA DE DATOS: Extraemos los valores o mostramos "Cargando..." si es null
    // Asegúrate de que tu clase User tenga estos nombres de variables (nombre, username, email, fechaRegistro)
    val nombreMostrar = usuario?.nombre ?: "Cargando..."
    val usuarioMostrar = usuario?.username ?: "..."
    val emailMostrar = usuario?.email ?: "..." // Si usaste @PropertyName("email_contacto") en User.kt, esto funcionará
    val fechaMostrar = usuario?.fechaalta ?: "..."
    var enabled by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .background(
                    MaterialTheme.colorScheme.background,
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .width(300.dp),
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
                modifier = Modifier
                    .shadow(
                        10.dp,
                        RoundedCornerShape(20.dp)
                    )
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 20.dp))
            {
                // 1. Nombre
                TextField(
                    value = nombreMostrar,
                    onValueChange = {},
                    enabled = enabled,
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(30.dp),
                    colors = TextFieldDefaults.colors(
                        // 1. Quitamos la línea inferior (underline)
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,

                        // 2. Fondo del color de tu contenedor (PrimaryContainer)
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,


                        // 3. Colores de iconos y texto
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.secondary,
                        focusedLabelColor = MaterialTheme.colorScheme.secondary
                    )
                )

                Spacer(modifier = Modifier.height(5.dp))

                // 2. Usuario
                TextField(
                    value = usuarioMostrar,
                    onValueChange = {},
                    enabled = enabled,
                    label = { Text("Usuario") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(30.dp),
                    colors = TextFieldDefaults.colors(
                        // 1. Quitamos la línea inferior (underline)
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,

                        // 2. Fondo del color de tu contenedor (PrimaryContainer)
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,


                        // 3. Colores de iconos y texto
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.secondary,
                        focusedLabelColor = MaterialTheme.colorScheme.secondary
                    )
                )

                Spacer(modifier = Modifier.height(5.dp))

                // 3. Email
                TextField(
                    value = emailMostrar,
                    onValueChange = {},
                    enabled = enabled,
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(30.dp),
                    colors = TextFieldDefaults.colors(
                        // 1. Quitamos la línea inferior (underline)
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,

                        // 2. Fondo del color de tu contenedor (PrimaryContainer)
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,


                        // 3. Colores de iconos y texto
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.secondary,
                        focusedLabelColor = MaterialTheme.colorScheme.secondary
                    )
                )

                Spacer(modifier = Modifier.height(5.dp))

                // 4. Fecha de Registro
                TextField(
                    value = fechaMostrar,
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Fecha de Registro") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(30.dp),
                    colors = TextFieldDefaults.colors(
                        // 1. Quitamos la línea inferior (underline)
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,

                        // 2. Fondo del color de tu contenedor (PrimaryContainer)
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,


                        // 3. Colores de iconos y texto
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.secondary,
                        focusedLabelColor = MaterialTheme.colorScheme.secondary
                    )
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth())
            {
                Column (horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .shadow(10.dp, RoundedCornerShape(20.dp))
                        .background(
                            MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(20.dp)
                        )
                        .padding(10.dp)
                        .width(100.dp))
                {
                    Text(text = "Editar",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .padding(horizontal = 20.dp))

                    IconButton(onClick = {enabled = true})
                    {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Eliminar Cuenta",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Column (horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .shadow(10.dp, RoundedCornerShape(20.dp))
                        .background(
                            MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(20.dp)
                        )
                        .padding(10.dp)
                        .width(100.dp))
                {
                    Text(text = "Eliminar",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .padding(horizontal = 20.dp))

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
                                    millis -> val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
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
                .background(
                    MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(20.dp)
                )
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally)
        {
            Image(
                painter = painterResource(id = R.drawable.fontlogo),
                modifier = Modifier
                    .width(80.dp)
                    .padding(top = 10.dp, bottom = 20.dp),
                contentDescription = "logo texto")

            Text("Añadir Tarea",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 15.dp),
                color = MaterialTheme.colorScheme.onSurface)

            TextField(
                value = tarea,
                onValueChange = onTareaChange,
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,

                    // 2. Fondo del color de tu contenedor (PrimaryContainer)
                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,

                    // 3. Colores de iconos y texto
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = MaterialTheme.colorScheme.primary)
            )

            Spacer(Modifier.height(10.dp))

            TextField(
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
                shape = RoundedCornerShape(30.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,

                    // 2. Fondo del color de tu contenedor (PrimaryContainer)
                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,

                    // 3. Colores de iconos y texto
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = MaterialTheme.colorScheme.primary)
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
    viewModel: TareasViewModel,
    listaTareas: List<Tarea>,
    listaCompletadas: List<Tarea>,
    onVaciarLista: () -> Unit,
    onBack: () -> Unit,
    onPreferences: () -> Unit,
    onCuenta: () -> Unit,
    onHelp: () -> Unit,
    query: String,
    onQueryChange: (String) -> Unit
)
{
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val nombre by viewModel.nombreUsuario.collectAsStateWithLifecycle()
    val auth = FirebaseAuth.getInstance()

    Column(modifier = Modifier
        .fillMaxWidth()
        .shadow(15.dp, RoundedCornerShape(15.dp))
        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(15.dp))
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
                    shape = RoundedCornerShape(30.dp),
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
                        onClick = { exportarTareas(context, listaTareas, listaCompletadas); expanded = false })
                    HorizontalDivider()

                    DropdownMenuItem(
                        text = { Text("Vaciar lista") },
                        leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
                        onClick = { onVaciarLista(); expanded = false })
                    HorizontalDivider()

                    DropdownMenuItem(
                        text = { Text("Salir") },
                        leadingIcon = { Icon(Icons.Outlined.Close, contentDescription = null) },
                        onClick = { auth.signOut(); onBack()  })
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

// ============ LISTA TAREAS COMPLETADAS ============ //

@Composable
fun CompletedTasksList(
    completedTasks: List<Tarea>,
    viewModel: TareasViewModel,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
)
{
    var expanded by remember { mutableStateOf(false) }
    var tareaDetallada by remember { mutableStateOf<Tarea?>(null) }
    var tareaAEliminar by remember { mutableStateOf<Tarea?>(null) }


    Column(modifier = Modifier
        .fillMaxWidth()
        .shadow(15.dp, RoundedCornerShape(15.dp))
        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(15.dp))
        .animateContentSize())
    {
        Column{
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 10.dp, horizontal = 20.dp))
            {
                Text("Completadas (${completedTasks.size})", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontStyle = FontStyle.Italic)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { expanded = !expanded })
                {
                    if (!expanded) {
                        Icon(Icons.Outlined.ArrowDropDown,
                            contentDescription = "Expandir", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    else {
                        Icon(Icons.Outlined.ArrowDropUp,
                            contentDescription = "Ocultar", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            if (expanded)
            {
                LazyColumn(modifier = Modifier.fillMaxWidth()
                    .heightIn(max = 350.dp))
                {
                    items(completedTasks, key = { it.id }) { tareaItem ->
                        HorizontalDivider()
                        CompletedTaskItem(tarea = tareaItem, viewModel = viewModel, onTaskClick = {tareaDetallada = tareaItem}, onDelete = {tareaAEliminar = tareaItem})
                    }
                }
            }
        }
    }
    if (tareaDetallada != null) {
        DetailTaskDialog(tarea = tareaDetallada!!, onDismiss = { tareaDetallada = null })
    }
    if (tareaAEliminar != null) {
        ConfirmDeleteDialog(
            onDismiss = { tareaAEliminar = null },
            onConfirm = {
                val taskToDelete = tareaAEliminar!!
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
}

// ============ BUSCADOR DE TAREAS ============ //

@Composable
fun CustomizableSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
        // Si quieres sombra, descomenta la siguiente línea:
        // .shadow(4.dp, shape = RoundedCornerShape(30.dp))
        ,
        placeholder = { Text("Buscar Tarea", color = Color.Gray) },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Buscar")
        },
        trailingIcon = {
            // Solo mostramos la X si hay texto escrito
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Borrar búsqueda", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(30.dp), // Forma de píldora
        colors = TextFieldDefaults.colors(
            // 1. Quitamos la línea inferior (underline)
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,

            // 2. Fondo del color de tu contenedor (PrimaryContainer)
            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,

            // 3. Colores de iconos y texto
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}

// ============ TAREAS ============ //

@Composable
fun TaskItem(
    tarea: Tarea,
    textColor: Color,
    onTaskClick: () -> Unit,
    onEdit: () -> Unit,
    onCheck: () -> Unit,
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
                    Text(text = formatearFechaParaMostrar(tarea.fecha), color = MaterialTheme.colorScheme.inversePrimary, fontSize = 12.sp)
                }
            }
            else
            {
                Text(text = tarea.texto, color = textColor)
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onCheck) { Icon(Icons.Default.Check, contentDescription = "Completar", tint = MaterialTheme.colorScheme.secondary)}
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.inversePrimary) }
        }
    }
}

// ============ TAREAS COMPLETADAS ============ //

@Composable
fun CompletedTaskItem(
    tarea: Tarea,
    viewModel: TareasViewModel,
    onTaskClick: () -> Unit,
    onDelete: () -> Unit
)
{
    var showDeleteDialog by remember { mutableStateOf(false) }

    Row(modifier = Modifier.padding(vertical = 10.dp, horizontal = 20.dp).clickable { onTaskClick() },
        verticalAlignment = Alignment.CenterVertically)
    {
        IconButton(onClick = {viewModel.descompletarTarea(tarea)})
        {
        Icon(Icons.Default.Check, contentDescription = "Editar", tint = MaterialTheme.colorScheme.secondary)}
        if (tarea.fecha.isNotBlank())
        {
            Column{
                Text(text = tarea.texto, fontStyle = FontStyle.Italic, textDecoration = TextDecoration.LineThrough)
                Spacer(modifier = Modifier.height(5.dp))
                Text(text = formatearFechaParaMostrar(tarea.fecha), color = MaterialTheme.colorScheme.inversePrimary, fontSize = 12.sp, fontStyle = FontStyle.Italic, textDecoration = TextDecoration.LineThrough)
            }
        }
        else
        {
            Text(text = tarea.texto, fontStyle = FontStyle.Italic, textDecoration = TextDecoration.LineThrough)
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { showDeleteDialog = true }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.inversePrimary) }
    }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = { onDelete() })
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
    val priority = determinePriority(tarea)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Card(elevation = CardDefaults.cardElevation(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp))
            {
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(10.dp).padding(start = 10.dp))
                {
                    Text("Estado: ", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    PriorityChip(priority)
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
                        Text(text = formatearFechaParaMostrar(tarea.fecha),
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
        shape = RoundedCornerShape(20.dp))
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
            } catch (e: Exception) { System.currentTimeMillis()}
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
                        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
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
                TextField(
                    value = textoEditado,
                    onValueChange = { textoEditado = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(30.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,

                        // 2. Fondo del color de tu contenedor (PrimaryContainer)
                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,

                        // 3. Colores de iconos y texto
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Campo editable para la fecha
                TextField(
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
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,

                        // 2. Fondo del color de tu contenedor (PrimaryContainer)
                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,

                        // 3. Colores de iconos y texto
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary)
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
                .background(
                    MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(20.dp)
                )
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

@Composable
fun PriorityChip(priority: TaskPriority) {
    Box(
        modifier = Modifier
            .background(
                color = priority.color.copy(alpha = 0.2f), // Fondo suave
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .width(
                when(priority)
                {
                    TaskPriority.EXPIRED -> 50.dp
                    TaskPriority.COMPLETED -> 80.dp
                    TaskPriority.UNKNOWN -> 60.dp
                    else -> 40.dp
                }
            ),
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

@Composable
fun RowButtons(onGoogleClick: () -> Unit,
               onGithubClick: () -> Unit,
               onMicrosoftClick: () -> Unit,
               onFacebookClick: () -> Unit)
{
    // 1. Define el tamaño aquí para afectar a todos a la vez
    val buttonSize = 60.dp

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth() // Asegura que la fila ocupe el ancho
    ) {
        SocialMediaButton(
            iconRes = R.drawable.google,
            size = buttonSize,
            onClick = onGoogleClick
        )
        SocialMediaButton(
            iconRes = R.drawable.github4,
            size = buttonSize,
            onClick = onGithubClick
        )
        SocialMediaButton(
            iconRes = R.drawable.microsoft,
            size = buttonSize,
            onClick = onMicrosoftClick
        )
        SocialMediaButton(
            iconRes = R.drawable.facebook,
            size = buttonSize,
            onClick = onFacebookClick
        )
    }
}

// Componente reutilizable para evitar repetir código
@Composable
fun SocialMediaButton(
    iconRes: Int,
    size: Dp,
    onClick: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(5.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .padding(15.dp) // Espacio entre tarjetas
            .clickable { onClick() },
    ) {
        // 2. Eliminamos IconButton. La imagen controla el tamaño.
        Image(
            painter = painterResource(iconRes),
            contentDescription = "Logo",
            modifier = Modifier
                .size(size) // Aquí se aplica el tamaño real
                .padding(15.dp) // Padding interno de la imagen dentro de la tarjeta
        )
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String)
{
    var showPassword by remember { mutableStateOf(false) }
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        //modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,

            // 2. Fondo del color de tu contenedor (PrimaryContainer)
            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,

            // 3. Colores de iconos y texto
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            cursorColor = MaterialTheme.colorScheme.primary),
        leadingIcon = when (label)
        {
            "Descripción" ->
            { { Icon(Icons.Default.Description, contentDescription = "Descripción") } }

            "Fecha" ->
            { { Icon(Icons.Default.DateRange, contentDescription = "Fecha") } }

            "Email" ->
            { { Icon(Icons.Default.Email, contentDescription = "Email") } }

            "Contraseña", "Confirmar contraseña" ->
            { { Icon(Icons.Default.Lock, contentDescription = "Contraseña") } }

            "Usuario" ->
            { { Icon(Icons.Default.Person, contentDescription = "Usuario") } }

            "Nombre" ->
            { { Icon(Icons.Default.Person, contentDescription = "Nombre") } }

            else -> { { } }
        },
        trailingIcon = when (label)
        {
            "Contraseña" , "Confirmar contraseña" ->
            { { if (value.isNotBlank())
            {
                IconButton(onClick = { showPassword = !showPassword }
                ) {
                    if (!showPassword)
                        Icon(Icons.Default.Visibility,
                            contentDescription = "Limpiar",
                            tint = MaterialTheme.colorScheme.inversePrimary)
                    else
                        Icon(Icons.Default.VisibilityOff,
                            contentDescription = "Limpiar",
                            tint = MaterialTheme.colorScheme.inversePrimary)
                }
            } } }
            else -> { { } }
        },
        visualTransformation = when (label)
        {
            "Contraseña" ->
            { if (!showPassword) PasswordVisualTransformation() else VisualTransformation.None }
            else -> { VisualTransformation.None }
        },
        keyboardOptions = when (label)
        {
            "Contraseña" ->
            { KeyboardOptions(keyboardType = KeyboardType.Password) }
            else -> { KeyboardOptions.Default }
        }
    )
}

// Función auxiliar
fun formatearFechaParaMostrar(fechaIso: String): String {
    if (fechaIso.isBlank()) return ""
    return try {
        // Divide "2025/01/01" y reordena
        val partes = fechaIso.split("/")
        "${partes[2]}/${partes[1]}/${partes[0]}" // Retorna "01/01/2025"
    } catch (e: Exception) {
        fechaIso // Si falla, devuelve la original
    }
}