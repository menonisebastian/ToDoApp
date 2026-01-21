package com.example.todoapp.ui

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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.OpenInNew
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
import androidx.compose.material3.DatePickerDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import com.example.todoapp.ui.theme.AppColors
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
import coil.compose.AsyncImage
import com.example.todoapp.R
import com.example.todoapp.resources.determinePriority
import com.example.todoapp.resources.exportarTareas
import com.example.todoapp.data.model.Tarea
import com.example.todoapp.data.firebase.TareasViewModel
import com.example.todoapp.data.model.User
import com.example.todoapp.resources.SettingsPreferences
import com.example.todoapp.resources.TaskPriority
import com.example.todoapp.resources.compararFechaActual
import com.example.todoapp.resources.formatearFechaParaMostrar
import com.example.todoapp.resources.formatearStatsPokemon
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.text.isNotBlank
import kotlin.text.trim

// ============ COMPONENTES UI ============ //

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
                    RoundedCornerShape(30.dp)
                )
                .padding(horizontal = 60.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LogoSmall(80.dp)

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
                        RoundedCornerShape(30.dp)
                    )
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(30.dp)
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
                    .shadow(10.dp, RoundedCornerShape(30.dp))
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(30.dp)
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

    // De no encontrar el valor del dato, muestra 3 puntos
    val nombreMostrar = usuario?.nombre ?: "..."
    val usuarioMostrar = usuario?.username ?: "..."
    val emailMostrar = usuario?.email ?: "..."
    val fechaMostrar = usuario?.fechaalta ?: "..."
    var enabled by remember { mutableStateOf(false) }
    enabled = false

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
            LogoSmall(80.dp)

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
                CustomTextField(nombreMostrar, {}, "Nombre", enabled)

                Spacer(modifier = Modifier.height(5.dp))

                // 2. Usuario
                CustomTextField(usuarioMostrar, {}, "Usuario", enabled)

                Spacer(modifier = Modifier.height(5.dp))

                // 3. Email
                CustomTextField(emailMostrar, {}, "Email", enabled)

                Spacer(modifier = Modifier.height(5.dp))

                // 4. Fecha de Registro
                CustomTextField(fechaMostrar, {},"Fecha de Registro", false)
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

    Dialog(onDismissRequest = onDismiss)
    {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(30.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally)
        {
            LogoSmall(80.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Text("Añadir Tarea", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(bottom = 15.dp), color = MaterialTheme.colorScheme.onSurface)

            CustomTextField(tarea, onTareaChange, "Descripción", true)

            Spacer(Modifier.height(10.dp))

            CustomDateField(
                value = fecha,
                onValueChange = onFechaChange,
                label = "Fecha (Opcional)"
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
                    if (tarea.isNotBlank()) {
                        onAddTarea(tarea.trim(), fecha)
                        onDismiss()
                    } else {
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
    val datosUsuario by viewModel.datosUsuario.collectAsStateWithLifecycle()
    val nombreCompleto = datosUsuario?.nombre ?: "..."
    val nombre = if (nombreCompleto.isNotBlank()) nombreCompleto.split(" ") else listOf("...")
    val auth = FirebaseAuth.getInstance()

    Column(modifier = Modifier
        .fillMaxWidth()
        .shadow(20.dp, RoundedCornerShape(20.dp))
        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
        .padding(horizontal = 20.dp, vertical = 10.dp))
    {
        Row(verticalAlignment = Alignment.CenterVertically)
        {
            Text(text =
                if (nombreCompleto != "..." && nombre.isNotEmpty())
                    "Bienvenido, " + nombre[0]
                else
                    "Bienvenido",
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
                        leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) },
                        onClick = { expanded = false; onCuenta() })
                    HorizontalDivider()

                    DropdownMenuItem(
                        text = { Text("Preferencias") },
                        leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) },
                        onClick = { expanded = false; onPreferences() })
                    HorizontalDivider()

                    DropdownMenuItem(
                        text = { Text("Ayuda") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) },
                        onClick = { onHelp() }
                    )
                    HorizontalDivider()

                    DropdownMenuItem(
                        text = { Text("Exportar tareas") },
                        leadingIcon = { Icon(Icons.Outlined.SaveAlt, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) },
                        onClick = { exportarTareas(context, listaTareas, listaCompletadas); expanded = false })
                    HorizontalDivider()

                    DropdownMenuItem(
                        text = { Text("Vaciar lista") },
                        leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) },
                        onClick = { onVaciarLista(); expanded = false })
                    HorizontalDivider()

                    DropdownMenuItem(
                        text = { Text("Cerrar sesión") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) },
                        onClick = { auth.signOut(); onBack()  })
                }
            }
        }

        if (listaTareas.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            CustomizableSearchBar(
                query = query,
                onQueryChange = onQueryChange
            )
            Spacer(modifier = Modifier.height(5.dp))
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
        .shadow(5.dp, RoundedCornerShape(20.dp))
        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp))
        .clickable { expanded = !expanded }
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
                LazyColumn(modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp))
                {
                    items(completedTasks, key = { it.id }) { tareaItem ->
                        HorizontalDivider()
                        CompletedTaskItem(tarea = tareaItem,
                            onCompletar = {
                                if (tareaItem.fecha.isNotBlank() && compararFechaActual(tareaItem.fecha))
                                {
                                    scope.launch {
                                        val result = snackbarHostState
                                            .showSnackbar(
                                                message = "La fecha de la tarea no puede ser anterior a la actual.",
                                                actionLabel = "OK",
                                                duration = SnackbarDuration.Short
                                            )
                                        when (result) {
                                            SnackbarResult.ActionPerformed -> { }
                                            SnackbarResult.Dismissed -> { }
                                        }
                                    }
                                }
                                else {
                                    viewModel.descompletarTarea(tareaItem)
                                }
                            },
                            onTaskClick = {tareaDetallada = tareaItem},
                            onDelete = {tareaAEliminar = tareaItem})
                    }
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(10.dp))

    if (tareaDetallada != null) {
        DetailTaskDialog(tarea = tareaDetallada!!,
            onDismiss = { tareaDetallada = null },
            onEditar = { },
            onCompletar = {
                viewModel.descompletarTarea(tareaDetallada!!)
                tareaDetallada = null
            })
    }
    if (tareaAEliminar != null) {
        ConfirmDeleteDialog(
            onDismiss = { tareaAEliminar = null },
            onConfirm = {
                tareaAEliminar?.let { taskToDelete ->
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
                            SnackbarResult.Dismissed -> { }
                        }
                    }
                }
                tareaAEliminar = null
            }
        )
    }
}

// ============ BARRA DE BUSQUEDA ============ //
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
            .fillMaxWidth(),
        placeholder = { Text("Buscar Tarea", color = Color.Gray) },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Buscar", tint = MaterialTheme.colorScheme.onSurface)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Borrar búsqueda", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(30.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,

            // 2. Fondo del color del contenedor
            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,

            // 3. Colores de iconos y texto
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}

// ============ TAREA ITEM ============ //
@Composable
fun TaskItem(
    tarea: Tarea,
    textColor: Color,
    onTaskClick: () -> Unit,
    onEdit: () -> Unit,
    onCheck: () -> Unit,
    onDelete: () -> Unit)
{
    Card(
        shape = RoundedCornerShape(20.dp),
        onClick = onTaskClick,
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    )
    {
        // PADDINGS VARIABLES
        val paddingStart = if (tarea.pokeName.isBlank()) 30.dp else 20.dp
        val paddingTop = if (tarea.pokeName.isBlank()) 20.dp else 10.dp
        val paddingBottom = if (tarea.pokeName.isBlank()) 20.dp else 10.dp
        val paddingEnd = 10.dp

        Row(
            modifier = Modifier
                .padding(top = paddingTop, bottom = paddingBottom, start = paddingStart, end = paddingEnd),
            verticalAlignment = Alignment.CenterVertically
        )
        {
            if(tarea.pokeImg.isNotBlank()) {
                ImgPokemon(tarea, 80.dp)
            }
            if (tarea.fecha.isNotBlank())
            {
                Column{
                    Text(text = tarea.texto, color = textColor)
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(text = formatearFechaParaMostrar(tarea.fecha), color = MaterialTheme.colorScheme.inversePrimary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(5.dp))
                    if (tarea.pokeImg.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically)
                        {
                            Text(text = "Pokemon: ",
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold)
                            Text(text = tarea.pokeName,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            else
            {
                Column{
                    Text(text = tarea.texto, color = textColor)
                    if (tarea.pokeImg.isNotBlank()) {
                        Spacer(modifier = Modifier.height(5.dp))
                        Row(verticalAlignment = Alignment.CenterVertically)
                        {
                            Text(text = "Pokemon: ",
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold)
                            Text(text = tarea.pokeName,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            RowItemButtons(onCheck, onEdit, onDelete)
        }
    }
}


// ============ TAREA COMPLETADA ITEM ============ //
@Composable
fun CompletedTaskItem(
    tarea: Tarea,
    onTaskClick: () -> Unit,
    onCompletar: () -> Unit,
    onDelete: () -> Unit
)
{
    Row(modifier = Modifier
        .clickable { onTaskClick() }
        .padding(vertical = 10.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically)
    {
        IconButton(onClick = { onCompletar() })
        {
            Icon(Icons.Default.Check, contentDescription = "Editar", tint = MaterialTheme.colorScheme.secondary)
        }

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

        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.inversePrimary)
        }
    }
}


// ===== MENSAJE DE LISTA VACIA ===== //
@Composable
fun EmptyTasksMessage() {
    Column(modifier = Modifier
        .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center)
    { Text("Tu lista de tareas está vacía", fontSize = 20.sp, fontStyle = FontStyle.Italic, color = Color.Gray) }
}


// ===== MENSAJE DE BUSQUEDA VACIA ===== //
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
            modifier = Modifier.padding(top = 80.dp))
    }
}


// ===== CONFIRMACIÓN DE VACIAR LISTA ===== //
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
                    onClick = {onConfirm() ; onDismiss()} ,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                )
                { Text("Aceptar") }
            },
        dismissButton =
            {
                TextButton(onClick = onDismiss)
                { Text("Cancelar", color = MaterialTheme.colorScheme.secondary) }
            },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(30.dp)
    )
}


// ====== DIALOGO DE TAREA DETALLADA ====== //
@Composable
fun DetailTaskDialog(tarea: Tarea, onDismiss: () -> Unit, onCompletar: () -> Unit, onEditar: () -> Unit)
{
    val priority = determinePriority(tarea)

    Dialog(
        onDismissRequest = onDismiss
    ) {
        // 1. Contenedor Principal (Fondo del Dialog)
        Card(
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- SECCIÓN TÍTULO---
                Card(
                    elevation = CardDefaults.cardElevation(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(10.dp)
                            .padding(start = 10.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = tarea.texto,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 24.sp
                            )
                            if (tarea.fecha.isNotBlank()) {
                                Text(
                                    text = formatearFechaParaMostrar(tarea.fecha),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.inversePrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        PriorityChip(priority)
                    }
                }

                if (tarea.pokeName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp)) // Espacio entre título y contenido

                    // --- SECCIÓN CONTENIDO (Info Pokemon) ---
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(start = 20.dp)
                    ) {
                        Column {

                            Text(text = "Pokemon: ",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold)
                            Text(text = tarea.pokeName,
                                color = MaterialTheme.colorScheme.inversePrimary,
                                fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(text = "Tipos: ",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 15.sp,
                                fontWeight = FontWeight.Bold)
                            Text(text = tarea.pokeType,
                                color = MaterialTheme.colorScheme.inversePrimary,
                                fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(text = "Stats: ",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 15.sp,
                                fontWeight = FontWeight.Bold)

                            Row()
                            {
                                Column()
                                {
                                    Text(text = "HP: ", color = AppColors.HP, fontSize = 15.sp)
                                    Text(text = "ATK: ", color = AppColors.ATK, fontSize = 15.sp)
                                    Text(text = "DEF: ", color = AppColors.DEF, fontSize = 15.sp)
                                    Text(text = "SpA: ", color = AppColors.SP_ATK, fontSize = 15.sp)
                                    Text(text = "SpD: ", color = AppColors.SP_DEF, fontSize = 15.sp)
                                    Text(text = "SPD: ", color = AppColors.SPEED, fontSize = 15.sp)
                                }

                                Column()
                                {
                                    Text(text = formatearStatsPokemon(tarea.pokeStats), color = MaterialTheme.colorScheme.inversePrimary, fontSize = 15.sp)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(30.dp))
                        ImgPokemon(tarea, 130.dp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp)) // Espacio antes del botón

                // --- SECCIÓN BOTÓN --- //
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End )
                {
                    IconButton(onClick = { onDismiss() },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.inversePrimary), modifier = Modifier.size(40.dp))
                    {
                        Icon(Icons.Filled.Close, contentDescription = "Cancelar", tint = MaterialTheme.colorScheme.onPrimary)
                    }

                    if (!tarea.completada)
                    {
                        Spacer(modifier = Modifier.width(20.dp))

                        IconButton(onClick = { onEditar() },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary), modifier = Modifier.size(40.dp))
                        {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    IconButton(onClick = {
                        onCompletar()
                        onDismiss()
                    },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.secondary), modifier = Modifier.size(40.dp))
                    {
                        Icon(Icons.Filled.Check, contentDescription = "Añadir", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}

// ============ DIALOGO DE CONFIRMAR ELIMINACION =========== //
@Composable
fun ConfirmDeleteDialog(onDismiss: () -> Unit, onConfirm: () -> Unit)
{
    AlertDialog(
        onDismissRequest = onDismiss,
        title =
            { Text("Eliminar tarea", color = MaterialTheme.colorScheme.onSurface) },
        text =
            {
                Text("¿Estás seguro de que quieres eliminar la tarea? " +
                        "\nEsta acción se puede deshacer al agitar el dispositivo.",
                    color = MaterialTheme.colorScheme.onSurface)
            },
        confirmButton =
            {
                Button(onClick = {onConfirm() ; onDismiss()},
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                )
                { Text("Aceptar") }
            },
        dismissButton =
            {
                TextButton(onClick = onDismiss)
                { Text("Cancelar", color = MaterialTheme.colorScheme.secondary) }
            },
        containerColor = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(10.dp))
}

// ============ DIALOGO DE EDITAR TAREA =========== //
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskDialog(
    tarea: Tarea,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var textoEditado by remember(tarea) { mutableStateOf(tarea.texto) }
    var fechaEditada by remember(tarea) { mutableStateOf(tarea.fecha) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(text = "Editar tarea", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        },
        text = {
            Column {
                CustomTextField(textoEditado, { textoEditado = it }, "Descripción", true)

                Spacer(modifier = Modifier.height(10.dp))

                CustomDateField(value = fechaEditada, onValueChange = { fechaEditada = it }, label = "Fecha")
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (textoEditado.isNotBlank()) { onSave(textoEditado, fechaEditada); onDismiss() }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = MaterialTheme.colorScheme.secondary) } },
        shape = RoundedCornerShape(30.dp)
    )
}


// ============== DIALOGO AYUDA ============ //
@Composable
fun HelpDialog(onDismiss: () -> Unit, onGithub: () -> Unit)
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
            LogoSmall(80.dp)
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

            TextButton(onClick = onGithub) {
                Row(verticalAlignment = Alignment.CenterVertically)
                {
                    Text("Visitar Github ", color = MaterialTheme.colorScheme.secondary)
                    Icon(Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Github",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            TextButton(onClick = onDismiss) { Text("Cerrar", color = MaterialTheme.colorScheme.secondary) }
        }
    }
}


// ========= BOX DE PRIORIDAD DE TAREA =========== //
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
                when (priority) {
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


// =========== ROW DE BOTONES DE TAREA ========== //
@Composable
fun RowItemButtons(onCheck: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit)
{
    Row(horizontalArrangement = Arrangement.End)
    {
        IconButton(onClick = { onCheck()}) { Icon(Icons.Default.Check, contentDescription = "Completar", tint = MaterialTheme.colorScheme.secondary)}
        IconButton(onClick = { onEdit() }) { Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary) }
        IconButton(onClick = { onDelete() }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.inversePrimary) }
    }
}

// ========= ROW DE BOTONES DE LOGIN ========== //
@Composable
fun RowButtons(onGoogleClick: () -> Unit,
               onGithubClick: () -> Unit,
               onMicrosoftClick: () -> Unit,
               onFacebookClick: () -> Unit)
{
    val buttonSize = 60.dp

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
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

// ============ BOTONES LOGOS ============ //
@Composable
fun SocialMediaButton(
    iconRes: Int,
    size: Dp,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        elevation = CardDefaults.cardElevation(5.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .padding(15.dp) // Espacio entre tarjetas
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = "Logo",
            modifier = Modifier
                .size(size)
                .padding(15.dp)
        )
    }
}


// ============ TEXTFIELD CUSTOMIZABLE ============= //
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isEnabled: Boolean
)
{
    var showPassword by remember { mutableStateOf(false) }
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        singleLine = true,
        enabled = isEnabled,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,

            // 2. Fondo del color del contenedor
            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,

            // 3. Colores de iconos y texto
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedLabelColor = MaterialTheme.colorScheme.inversePrimary,
            focusedLabelColor = MaterialTheme.colorScheme.secondary,
            cursorColor = MaterialTheme.colorScheme.primary),
        leadingIcon = when (label)
        {
            "Descripción", "Nombre" ->
            { { Icon(Icons.Default.Description, contentDescription = "Descripción", tint = MaterialTheme.colorScheme.inversePrimary) } }

            "Email" ->
            { { Icon(Icons.Default.Email, contentDescription = "Email", tint = MaterialTheme.colorScheme.inversePrimary) } }

            "Contraseña", "Confirmar contraseña" ->
            { { Icon(Icons.Default.Lock, contentDescription = "Contraseña", tint = MaterialTheme.colorScheme.inversePrimary) } }

            "Usuario" ->
            { { Icon(Icons.Default.Person, contentDescription = "Usuario", tint = MaterialTheme.colorScheme.inversePrimary) } }

            "Fecha de Registro" ->
            { { Icon(Icons.Default.DateRange, contentDescription = "Fecha de Registro", tint = MaterialTheme.colorScheme.inversePrimary) } }

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
            "Contraseña" , "Confirmar contraseña" ->
            { if (!showPassword) PasswordVisualTransformation() else VisualTransformation.None }
            else -> { VisualTransformation.None }
        },
        keyboardOptions = when (label)
        {
            "Contraseña" , "Confirmar contraseña" ->
            { KeyboardOptions(keyboardType = KeyboardType.Password) }
            else -> { KeyboardOptions.Default }
        }
    )
}


// ============ TEXTFIELD DE FECHA ============ //
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDateField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = if (value.isNotBlank()) {
            try {
                val millis = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).parse(value)?.time
                    ?: System.currentTimeMillis()

                if (millis < System.currentTimeMillis() - 86400000) {
                    System.currentTimeMillis()
                } else {
                    millis
                }
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
        } else {
            System.currentTimeMillis()
        },
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                // Solo permite seleccionar fechas desde hoy en adelante
                return utcTimeMillis >= System.currentTimeMillis() - 86400000
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                IconButton(
                    onClick = {
                        showDatePicker = false
                        datePickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                            onValueChange(sdf.format(millis))
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Seleccionar", tint = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                IconButton(onClick = { showDatePicker = false },colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.inversePrimary)) {
                    Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = MaterialTheme.colorScheme.onPrimary)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        "Seleccionar fecha",
                        modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp, bottom = 12.dp)
                    )
                },
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.secondary,
                    headlineContentColor = MaterialTheme.colorScheme.onSurface,
                    subheadContentColor = MaterialTheme.colorScheme.onSurface,
                    selectedDayContainerColor = MaterialTheme.colorScheme.secondary,
                    navigationContentColor = MaterialTheme.colorScheme.onSurface,
                    weekdayContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        }
    }

    TextField(
        value = formatearFechaParaMostrar(value),
        onValueChange = {},
        label = { Text(label) },
        placeholder = { Text("yyyy/MM/dd") },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDatePicker = true },
        readOnly = true,
        enabled = true,
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(imageVector = Icons.Default.DateRange, contentDescription = "Seleccionar fecha", tint = MaterialTheme.colorScheme.inversePrimary)
            }
        },
        shape = RoundedCornerShape(30.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            unfocusedLabelColor = MaterialTheme.colorScheme.inversePrimary,
            focusedLabelColor = MaterialTheme.colorScheme.secondary,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.inversePrimary,
            focusedPlaceholderColor = MaterialTheme.colorScheme.inversePrimary,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}


// ============ LOGO PRINCIPAL ============ //
@Composable
fun MainLogo()
{
    Image(
        painter = painterResource(R.drawable.cutlogoapp),
        modifier = Modifier
            .size(50.dp)
            .padding(5.dp),
        contentDescription = "Logo"
    )
    Image(
        painter = painterResource(R.drawable.fontlogo),
        modifier = Modifier
            .width(125.dp)
            .padding(top = 5.dp),
        contentDescription = "logo texto"
    )
    Image(
        painter = painterResource(R.drawable.pokemonlogo),
        modifier = Modifier
            .width(80.dp)
            .padding(top = 5.dp),
        contentDescription = "logo pokemon"
    )
}


// ============ LOGO MINI ============ //
@Composable
fun LogoSmall(width:Dp)
{
    Image(painter = painterResource(id = R.drawable.fontlogo),
        modifier = Modifier
            .width(width)
            .padding(top = 10.dp),
        contentDescription = "logo texto")
    Image(painter = painterResource(id = R.drawable.pokemonlogo),
        modifier = Modifier
            .width(width - 10.dp)
            .padding(top = 10.dp),
        contentDescription = "logo texto")
}


// ============ IMAGEN DE POKEMON ============ //
@Composable
fun ImgPokemon(tarea: Tarea, size: Dp) {
    if (tarea.pokeImg.isNotBlank()) {
        AsyncImage(
            model = tarea.pokeImg,
            contentDescription = tarea.pokeName,
            modifier = Modifier
                .size(size)
                .padding(end = 10.dp)
        )
    } else {
        // Si no hay imagen de URL, no se debe mostrar nada o mostrar un placeholder
    }
}
