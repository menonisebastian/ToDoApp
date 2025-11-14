package com.example.todoapp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.todoapp.ui.theme.ToDoAppTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ============ DATA CLASS ============
data class Tarea(
    val id: Int,
    val texto: String,
    var completada: Boolean = false,
    val fechaCreacion: Date = Date(),
    // simple date format
    val sdf: SimpleDateFormat = SimpleDateFormat("'dd-MM-yyyy'", Locale.getDefault()),

    // current date and time and calling a simple date format
    val currentDateAndTime: String = sdf.format(Date())
)

// ============ ACTIVITY ============
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent{
            val settingsPreferences = remember { SettingsPreferences(applicationContext) }

            // 1. Lee el valor del modo oscuro desde DataStore.
            val isDarkMode by settingsPreferences.isDarkMode.collectAsStateWithLifecycle(initialValue = false)

            // 1. Lee el nombre del color desde DataStore.
            val textColorName by settingsPreferences.taskTextColor.collectAsStateWithLifecycle(initialValue = "Default")

            // 2. Convierte el nombre del color a un valor Color de Compose.
            val taskTextColor = when (textColorName)
            {
                "Negro" -> Color.Black
                "Naranja" -> MaterialTheme.colorScheme.primary
                "Azul" -> MaterialTheme.colorScheme.secondary
                "Blanco" -> Color(0xFFF1F1F1)
                else -> MaterialTheme.colorScheme.onSurface // Color por defecto del tema
            }

            ToDoAppTheme(darkTheme = isDarkMode)
            {
                AppNav(taskTextColor = taskTextColor)
            }
        }
    }
}

@Composable
fun AppNav(taskTextColor: Color) {
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
                onPreferences = { navController.navigate("preferences") }
            )
        }
        composable ("preferences" )
        {
            Preferences (onBack = { navController.popBackStack() })
        }
    }
}

// ============ LOGIN SCREEN ============
@Composable
fun Login(onEnviar: (String, String) -> Unit) {
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
        Spacer(Modifier.height(20.dp))
        Image(
            painter = painterResource(R.drawable.cutlogoapp),
            modifier = Modifier
                .size(90.dp)
                .padding(10.dp),
            contentDescription = "Logo"
        )
        Image(
            painter = painterResource(R.drawable.fontlogo),
            modifier = Modifier
                .width(200.dp)
                .padding(top = 10.dp),
            contentDescription = "logo texto"
        )

        Spacer(Modifier.height(20.dp))
        Column(
            modifier = Modifier
                .shadow(15.dp, RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                .padding(20.dp)
                .width(300.dp),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = nombres,
                onValueChange = { nombres = it },
                singleLine = true,
                label = { Text("Nombre") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1B3B68),
                    //unfocusedBorderColor = Color(0xFFFD6310),
                    focusedLabelColor = Color(0xFFFD6310),
                    unfocusedLabelColor = Color(0xFF868686)
                )
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = alias,
                onValueChange = { alias = it },
                singleLine = true,
                label = { Text("Alias") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1B3B68),
                    //unfocusedBorderColor = Color(0xFFFD6310),
                    focusedLabelColor = Color(0xFFFD6310),
                    unfocusedLabelColor = Color(0xFF868686)
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
                modifier = Modifier.width(275.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = "Continuar", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ============ MAIN APP SCREEN ============
@Composable
fun App(nombre: String, alias: String, taskTextColor: Color, onBack: () -> Unit, onPreferences: () -> Unit)
{
    var tarea by remember { mutableStateOf("") }
    val tareas = remember { mutableStateListOf<Tarea>() }
    var nextId by remember { mutableIntStateOf(0) }
    var tareaEditando by remember { mutableStateOf<Tarea?>(null) }
    var tareaAEliminar by remember { mutableStateOf<Tarea?>(null) }
    var ultimaTareaEliminada by remember { mutableStateOf<Pair<Int, Tarea>?>(null) }        //POR IMPLEMENTAR SHAKEDETECTOR
    var showClearDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var tareaDetallada by remember { mutableStateOf<Tarea?>(null) }
    val filteredTareas =
        if (searchQuery.isBlank())
        {
            tareas
        } else
        {
            tareas.filter { it.texto.contains(searchQuery, ignoreCase = true) }
        }

    val context = LocalContext.current
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager   //POR IMPLEMENTAR SHAKEDETECTOR
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)           //POR IMPLEMENTAR SHAKEDETECTOR



    // ESTRUCTURA: Column con LazyColumn solo para la lista
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // HEADER FIJO
        Spacer(Modifier.height(10.dp))

        Image(
            painter = painterResource(id = R.drawable.fontlogo),
            modifier = Modifier
                .width(100.dp)
                .padding(10.dp),
            contentDescription = "logo texto"
        )

        // CARD SUPERIOR FIJA
        TopCard(
            nombre = nombre,
            alias = alias,
            tarea = tarea,
            onTareaChange = { tarea = it },
            onAddTarea = {
                if (tarea.isNotBlank()) {
                    tareas.add(Tarea(id = nextId++, texto = tarea))
                    tarea = ""
                    Toast.makeText(context, "Tarea agregada correctamente", Toast.LENGTH_SHORT).show()
                }
            },
            onVaciarLista = {
                if (!tareas.isEmpty())
                    showClearDialog = true
                else
                    Toast.makeText(context, "No hay tareas para vaciar", Toast.LENGTH_SHORT).show()
            },
            onBack = onBack,
            onPreferences = onPreferences
        )

        if (tareas.isEmpty()) {
            EmptyTasksMessage()
        } else {

            Spacer(Modifier.height(20.dp))


            CustomizableSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = { /* La búsqueda ya es en tiempo real, puedes dejar esto vacío o añadir lógica extra */ },
                searchResults = filteredTareas,
                onResultClick = { tarea ->
                    // Acción al hacer clic en un resultado, por ejemplo, abrir el diálogo de edición.
                    tareaDetallada = tarea
                    searchQuery = "" // Limpiar la búsqueda
                },
                //modifier = Modifier.fillMaxWidth()
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(tareas) { tareaItem ->
                    TaskItem(
                        tarea = tareaItem,
                        onTaskClick = { tareaDetallada = tareaItem },
                        onEdit = { tareaEditando = tareaItem },
                        textColor = taskTextColor,
                        onDelete =
                        {
                            tareaAEliminar = tareaItem
                        }
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }
        }

        if (tareaEditando != null) {
            val tarea = tareaEditando!!
            EditTaskDialog(
                tarea = tarea,
                onDismiss = { tareaEditando = null },
                onSave = { nuevoTexto ->
                    val index = tareas.indexOf(tarea)
                    if (index != -1) {
                        tareas[index] = tarea.copy(
                            texto = nuevoTexto
                        )
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
                    tareas.remove(tareaAEliminar)
                    Toast.makeText(context, "Tarea eliminada correctamente", Toast.LENGTH_SHORT).show()
                    tareaAEliminar = null
                }
            )
        }

        if (showClearDialog) {
            ConfirmClearDialog(
                onDismiss = { showClearDialog = false },
                onConfirm = {
                    tareas.clear()
                    nextId = 0
                    Toast.makeText(context, "La lista de tareas ha sido vaciada", Toast.LENGTH_SHORT).show()
                    showClearDialog = false
                }
            )
        }
        if (tareaDetallada != null) {
            DetailTaskDialog(
                tarea = tareaDetallada!!,
                onDismiss = { tareaDetallada = null }
            )
        }
    }
}

// ============ VENTANA PREFERENCIAS ============
@Composable
fun Preferences(onBack: () -> Unit)
{
    val colorTexto = remember { mutableListOf("Negro", "Naranja", "Azul", "Blanco") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settingsPreferences = remember { SettingsPreferences(context) }

    // 1. Lee el valor del modo oscuro desde DataStore y lo convierte en un estado de Compose.
    // El valor 'initial' es importante para la primera composición.
    val isDarkMode by settingsPreferences.isDarkMode.collectAsStateWithLifecycle(initialValue = false)

    // OBTENER EL COLOR ACTUAL ---
    val colorSeleccionado by settingsPreferences.taskTextColor.collectAsStateWithLifecycle(initialValue = "Default")


    Column(Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally)
    {
        Spacer(modifier = Modifier.height(40.dp))

        Image(
            painter = painterResource(id = R.drawable.fontlogo),
            modifier = Modifier
                .width(100.dp)
                .padding(10.dp),
            contentDescription = "logo texto"
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Preferencias",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .shadow(10.dp, RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                .padding(10.dp)
                .width(160.dp))
        {
            Text(text = "Colores del Texto",
                modifier = Modifier.padding(10.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface)

            colorTexto.forEach { color ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .width(110.dp)
                ) {
                    RadioButton(
                        selected = colorSeleccionado == color,
                        onClick = { scope.launch {
                            settingsPreferences.setTaskTextColor(color)
                        } },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = when (color)
                            {
                                "Negro" -> Color.Black
                                "Naranja" -> Color(0xFFFD6310)
                                "Azul" -> Color(0xFF017FFC)
                                "Blanco" -> Color(0xFFF1F1F1)
                                else -> Color.LightGray
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
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                .padding(10.dp)
                .width(160.dp))
        {
            Text(text = "Modo Oscuro",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier
                    .padding(top = 10.dp)
                    .padding(horizontal = 20.dp)
            )
            Switch(checked = isDarkMode, onCheckedChange = { nuevoValor ->
                scope.launch {
                    settingsPreferences.setDarkMode(nuevoValor)
                }
            },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onSurface,
                    checkedTrackColor = MaterialTheme.colorScheme.background,
                    checkedBorderColor = MaterialTheme.colorScheme.onSurface,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                    uncheckedTrackColor = MaterialTheme.colorScheme.background,
                    uncheckedBorderColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { onBack() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        )
        {
            Text("Guardar")
        }

        TextButton(onClick = { onBack() })
        { Text("Volver a la pantalla principal", color = MaterialTheme.colorScheme.secondary) }
    }
}

// ============ TARJETA SUPERIOR DE APP ============ //

@Composable
fun TopCard(
    nombre: String,
    alias: String,
    tarea: String,
    onTareaChange: (String) -> Unit,
    onAddTarea: () -> Unit,
    onVaciarLista: () -> Unit,
    onBack: () -> Unit,
    onPreferences: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .shadow(15.dp, RoundedCornerShape(15.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        // FILA NOMBRE Y MENÚ
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Hola, $nombre ($alias)",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { expanded = !expanded }) {
                Icon(Icons.Outlined.MoreVert, contentDescription = "Preferencias", tint = MaterialTheme.colorScheme.onSurface)
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    DropdownMenuItem(
                        text = { Text("Preferencias") },
                        leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                        onClick =
                        {
                            expanded = false
                            onPreferences()
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Ayuda") },
                        leadingIcon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                        onClick = { /* por implementar */ }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Vaciar lista") },
                        leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
                        onClick = {
                            onVaciarLista()
                            expanded = false
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Salir") },
                        leadingIcon = { Icon(Icons.Outlined.Close, contentDescription = null) },
                        onClick = { onBack() }
                    )
                }
            }
        }

        // FILA AGREGAR NUEVA TAREA
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = tarea,
                onValueChange = onTareaChange,
                label = { Text("Nueva tarea") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF017FFC),
                    //unfocusedBorderColor = Color(0xFFFD6310),
                    focusedLabelColor = Color(0xFFFD6310),
                    unfocusedLabelColor = Color(0xFF017FFC)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(5.dp))

            IconButton(
                onClick = onAddTarea,
                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Añadir", tint = Color.White)
            }
        }
    }
}


// ============ BUSCADOR ============ //
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizableSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    searchResults: List<Tarea>,
    onResultClick: (Tarea) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = modifier
            .semantics { isTraversalGroup = true }
    ) {
        DockedSearchBar(
            colors = SearchBarDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
            shadowElevation = 10.dp,
            modifier = Modifier
                .semantics { traversalIndex = 0f },
            inputField = {
                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = onQueryChange,
                    onSearch = {
                        onSearch(it)
                        expanded = false
                    },
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    placeholder = { Text("Buscar tarea") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (expanded) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Close",
                                modifier = Modifier.clickable
                                {
                                    onQueryChange("")
                                    expanded = false
                                }
                            )
                        }
                        else
                        {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Close",
                                modifier = Modifier.clickable
                                {
                                    expanded = true
                                }
                            )
                        }
                    }
                )
            },
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            LazyColumn()

            {
                if (searchResults.isEmpty())
                {
                    item {
                        Column(modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center)
                        {
                            Spacer(modifier = Modifier.height(100.dp))

                            Text("No se encontraron resultados", color = Color.Gray)
                        }
                    }
                }
                else
                {
                    items(searchResults) { tarea ->
                        ListItem(
                            headlineContent = { Text(tarea.texto) },
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .clickable {
                                    onResultClick(tarea)
                                    expanded = false
                                }
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============ TaskItem ============ //

@Composable
fun TaskItem(
    tarea: Tarea,
    textColor: Color,
    onTaskClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    if (tarea.id==0)
    {
        Spacer(modifier = Modifier.height(20.dp))
    }
    Card(modifier = Modifier
        .fillMaxWidth()
        .clickable { onTaskClick() },
        elevation = CardDefaults.cardElevation(5.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    )
    {
        Row(
            modifier = Modifier
                .padding(vertical = 10.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        )
        {
            Text(text = tarea.texto, color = textColor)
            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onEdit)
            {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }

            IconButton(onClick = onDelete)
            {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
            }
        }
    }

}

// ============ MENSAJE DE LISTA VACIA ============ //

@Composable
fun EmptyTasksMessage()
{
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    )
    {
        Text(
            "Tu lista de tareas está vacía",
            fontSize = 20.sp,
            fontStyle = FontStyle.Italic,
            color = Color.Gray
        )
    }
}

// ============ DIALOGO DE VACIAR LISTA ============ //
@Composable
fun ConfirmClearDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Vaciar lista de tareas") },
        text = { Text("¿Estás seguro de que quieres eliminar todas las tareas? Esta acción no se puede deshacer.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.secondary)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp)
    )
}

// ============ DIALOGO DE VISTA DE TAREA ============ //
@Composable
fun DetailTaskDialog(
    tarea: Tarea,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tarea  #${tarea.id+1} - ${tarea.currentDateAndTime}") },
        text = { Text(tarea.texto) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Listo", color = MaterialTheme.colorScheme.primary, fontSize = 20.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss /* por implementar */)
            {
                Text("Editar", color = MaterialTheme.colorScheme.secondary)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp)
    )
}

// ============ DIALOGO DE ELIMINAR TAREA ============ //
@Composable
fun ConfirmDeleteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar tarea", color = MaterialTheme.colorScheme.onSurface) },
        text = { Text("¿Estás seguro de que quieres eliminar la tarea? Esta acción no se puede deshacer.", color = MaterialTheme.colorScheme.onSurface) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.secondary)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(10.dp)
    )
}

// ============ DIALOGO DE EDITAR TAREA ============ //
@Composable
fun EditTaskDialog(
    tarea: Tarea,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
)
{
    var textoEditado by remember(tarea) { mutableStateOf(tarea.texto) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title =
            {
                Text(
                    text = "Editar tarea",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
        text = {
            OutlinedTextField(
                value = textoEditado,
                onValueChange = { textoEditado = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF017FFC),
                    unfocusedBorderColor = Color(0xFFFD6310),
                    focusedLabelColor = Color(0xFFFD6310),
                    unfocusedLabelColor = Color(0xFF017FFC)
                ),
                singleLine = true
            )
        },
        confirmButton =
            {
                Button(
                    onClick =
                        {
                            if (textoEditado.isNotBlank())
                            {
                                onSave(textoEditado)
                            }
                        },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Guardar")
                }
            },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.secondary)
            }
        },
        shape = RoundedCornerShape(10.dp)
    )
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    //AppNav()
    App(nombre = "Sebastian", alias = "Menoni", taskTextColor = Color(12312312313),onBack = {}, onPreferences = {})
    //TaskItem(tarea = Tarea(0, "Tarea de prueba"), onEdit = { }, onDelete = { }
    //EditTaskDialog(tarea = Tarea(0, "Tarea de prueba"), onDismiss = { }, onSave = { })
    //ConfirmClearDialog(onDismiss = {}, onConfirm = {})
    //Preferences(onBack = {})
}
