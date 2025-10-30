package com.example.todoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.todoapp.ui.theme.ToDoAppTheme
import com.example.todoapp.ui.theme.Typography

@Composable
fun AppNav()
{
    val navController=rememberNavController()

    NavHost(navController, startDestination = "login")
    {
        composable("login")
        {
            Login(onEnviar =
            {nombre, alias ->
                navController.currentBackStackEntry?.savedStateHandle?.apply {
                    set("nombre", nombre)
                    set("alias", alias)
                }
                navController.navigate("app")
            })
        }
        composable("app")
        {
            val prev = navController.previousBackStackEntry?.savedStateHandle
            val nombre = prev?.get<String>("nombre").orEmpty()
            val alias = prev?.get<String>("alias").orEmpty()

            App(nombre = nombre, alias=alias, onBack = { navController.popBackStack()})
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ToDoAppTheme {
                AppNav()
            }
        }
    }
}

@Composable
fun MinimalDialog(onDismissRequest: () -> Unit) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                text = "Debes introducir un nombre y un alias para continuar".uppercase(),
                fontSize = 10.sp,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun Login(onEnviar: (String, String)-> Unit)
{
    var nombres by remember { mutableStateOf("") }
    var alias by remember { mutableStateOf("") }

    // 1. Add a state to control the dialog's visibility
    var showDialog by remember { mutableStateOf(false) }

    // 2. Conditionally show the dialog in the composition
    if (showDialog)
    {
        MinimalDialog(onDismissRequest = { showDialog = false })
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Spacer(Modifier.height(20.dp))
        Image(
            painter = painterResource(R.drawable.applogo),
            modifier = Modifier.size(100.dp).padding(vertical = 10.dp),
            contentDescription = "Logo"
        )
        Image(
            painter = painterResource(R.drawable.fontlogo),
            modifier = Modifier.width(200.dp).padding(top = 5.dp),
            contentDescription = "logo texto"
        )
//        Text(
//            text = "toDo(App)",
//            fontSize = 30.sp,
//            color = Color(0xFF505050),
//            fontWeight = FontWeight.Bold
//            )
        Spacer(Modifier.height(20.dp))
        Column(
            modifier = Modifier
                .shadow(15.dp, RoundedCornerShape(10.dp))
                .background(Color.White, RoundedCornerShape(10.dp))
                .padding(20.dp)
                .width(300.dp),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            OutlinedTextField(
                value = nombres,
                onValueChange = { nombres = it },
                singleLine = true,
                label = { Text("Nombre") }
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = alias,
                onValueChange = { alias = it },
                singleLine = true,
                label = { Text("Alias") }
            )
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically
//
//            ) {
//                Checkbox(
//                    checked = checked,
//                    onCheckedChange = {checked = it}
//                )
//                Text(
//                    text = "Mantener la sesion abierta"
//                )
//            }
//
//            Row(
//                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = "Activar notificaciones"
//                )
//                Spacer(Modifier.weight(1f))
//                Switch(
//                    checked = checked,
//                    onCheckedChange =
//                    {
//                        checked = it
//                    }
//                )
//            }

            Spacer(Modifier.height(10.dp))

            ElevatedButton(onClick = {
                if (nombres.isNotBlank() && alias.isNotBlank()) {
                    onEnviar(nombres, alias)
                }
                else
                {
//                  showDialog = true
                    //no hace nada, se puede eliminar el else
                }
            },
                modifier = Modifier.width(275.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFD6310)))
            {
                Text(
                    text = "Continuar",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun App(nombre:String, alias:String, onBack: ()-> Unit)
{
    var expanded by remember { mutableStateOf(false) }
    var tarea by remember { mutableStateOf("") }
    val tareas = remember { mutableStateListOf<String>() }

    Column(Modifier
        .fillMaxSize()
        .padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally)
    {
        Spacer(Modifier.height(30.dp))

        Column(modifier = Modifier.shadow(15.dp, RoundedCornerShape(15.dp))
            .background(Color.White, RoundedCornerShape(10.dp))
            .padding(horizontal = 20.dp, vertical = 10.dp))
        {
            // FILA NOMBRE Y PREFERENCIAS

            Row (verticalAlignment = Alignment.CenterVertically)
            {
                Text("Hola, $nombre ($alias)", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = {expanded = !expanded})
                {
                    Icon(Icons.Default.MoreVert, contentDescription = "Preferencias")
                    DropdownMenu(expanded = expanded,
                        onDismissRequest = {expanded=false},
                        modifier = Modifier.background(Color.White) )
                    {
                        // First section
//                    DropdownMenuItem(
//                        text = { Text("Profile") },
//                        leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
//                        onClick = { /* Do something... */ }
//                    )
                        DropdownMenuItem(
                            text = { Text("Preferencias") },
                            leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                            onClick = { /* Do something... */ }
                        )

                        HorizontalDivider()

                        // Second section
//                    DropdownMenuItem(
//                        text = { Text("Send Feedback") },
//                        leadingIcon = { Icon(Icons.Outlined.Warning, contentDescription = null) },
//                        trailingIcon = { Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = null) },
//                        onClick = { /* Do something... */ }
//                    )

                        HorizontalDivider()

                        // Third section
                        DropdownMenuItem(
                            text = { Text("Ayuda") },
                            leadingIcon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                            onClick = { /* Do something... */ }
                        )
//                    DropdownMenuItem(
//                        text = { Text("Help") },
//                        // https://developer.android.com/develop/ui/compose/components/menu?hl=es-419
//                        leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Help, contentDescription = null) },
//                        trailingIcon = { Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null) },
//                        onClick = { /* Do something... */ }
//                    )
                    }
                }
            }

            // FILA AGREGAR TAREA

            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically)
            {
                OutlinedTextField(
                    value = tarea,
                    onValueChange = { tarea = it },
                    label = { Text("Nueva tarea")}, modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(10.dp))

                Button(onClick = {
                    if (tarea.isNotBlank()) {
                        tareas.add(tarea)   //agregar tarea a la lista de tareas
                        tarea = ""          //limpiar el campo después de agregar
                    }
                }, colors = ButtonDefaults.buttonColors(Color(0xFFFD6310)))
                {
                    Text("Agregar",
                        fontWeight = FontWeight.Bold)
                }
            }
        }

        // LISTADO DE TAREAS

        if (tareas.isEmpty())
        {
            Column(modifier = Modifier.fillMaxHeight(),verticalArrangement = Arrangement.Center)
            {
                Text("Tu lista de tareas está vacía", modifier = Modifier.padding(20.dp),
                    fontSize = 20.sp,
                    fontStyle = FontStyle.Italic, color = Color.Gray)
            }
        }
        else
        {
            Spacer(Modifier.height(10.dp))
            //HorizontalDivider()
            // Mostrar cada tarea en su propia fila
            tareas.forEachIndexed { index, tareaItem ->
                Row(
                    modifier = Modifier.shadow(15.dp, RoundedCornerShape(15.dp))
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    Text(text = tareaItem)
                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = {tareas.removeAt(index)})
                    {
                        Icon(Icons.Default.Delete, contentDescription = "Preferencias")
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }

        Spacer(Modifier.weight(1f))
        Button(
            onClick = { onBack() },
            colors = ButtonDefaults.buttonColors(Color(0xFFFD6310))
        )
        {
            Text(text = "Salir",
                fontWeight = FontWeight.Bold)
        }

//        FloatingActionButton(
//            onClick = { onBack() },
//            containerColor = Color(0xFF850071))
//        {
//            Text(text = "Salir", color = Color.White)
//        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AppNav()
    //App(nombre = "Sebastian", alias = "Menoni", onBack = {})
}
