package com.example.todoapp

import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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