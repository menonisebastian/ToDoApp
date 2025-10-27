package com.example.todoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todoapp.ui.theme.ToDoAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ToDoAppTheme {
                Login()
            }
        }
    }
}

@Composable
fun Login(onEnviar: (String, String)-> Unit)
{
    var nombres by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var checked by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Image(
            painter = painterResource(R.drawable.lista),
            modifier = Modifier.size(100.dp),
            contentDescription = "Imagen"
        )
        Text(
            text = "Inscripcion Carrera Popular 2025",
            fontSize = 16.sp,
            color = Color.Blue
        )
        Spacer(Modifier.height(20.dp))
        Column(
            modifier = Modifier
                .shadow(15.dp, RoundedCornerShape(10.dp))
                .background(Color.White, RoundedCornerShape(10.dp))
                .padding(20.dp)
                .height(350.dp)
                .width(300.dp),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            TextField(
                value = nombres,
                onValueChange = { nombres = it },
                label = { Text("Nombre y Apellidos") }
            )
            Spacer(Modifier.height(10.dp))
            TextField(
                value = edad,
                onValueChange = { edad = it },
                label = { Text("Edad") }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically

            ) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = {checked = it}
                )
                Text(
                    text = "Acepto las normas del evento"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Deseo recibir notificaciones"
                )
                Spacer(Modifier.width(10.dp))
                Switch(
                    checked = checked,
                    onCheckedChange =
                        {
                            checked = it
                        }
                )
            }
            HorizontalDivider(thickness = 3.dp)
            Spacer(modifier = Modifier.height(5.dp))
            Button(onClick = {onEnviar(nombres, edad)},
                modifier = Modifier.width(275.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue))
            {
                Text(
                    text = "Enviar"
                )
            }
            Text(
                text = "Gracias por inscribirte",
                fontSize = 16.sp,
                color = Color.Blue
            )
        }
    }
}

@Composable
fun FormScreen2(nombre:String, apellidos:String, onBack: ()-> Unit)
{

    Column(Modifier
        .fillMaxSize()
        .padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally)
    {
        Spacer(Modifier.height(15.dp))
        Text("Nombre: $nombre")
        Spacer(Modifier.height(15.dp))
        Text("Apellido: $apellidos")
        Spacer(Modifier.height(15.dp))
        Button(
            onClick = { onBack() },
            colors = ButtonDefaults.buttonColors(Color(0xFF35536B)))
        {
            Text(text = "VOLVER A LA PANTALLA PRINCIPAL")
        }
    }
}

@Composable
fun AppNav()
{
    val navController=rememberNavController()

    NavHost(navController, startDestination = "form")
    {
        composable("form")
        {
            Login(onEnviar =
                {nombre, apellidos ->
                    navController.currentBackStackEntry?.savedStateHandle?.apply {
                    set("nombre", nombre)
                    set("apellidos", apellidos)
                }
                navController.navigate("second")
            })
        }
        composable("second")
        {
            var prev = navController.previousBackStackEntry?.savedStateHandle
            var nombre = prev?.get<String>("nombre").orEmpty()
            var apellidos = prev?.get<String>("apellidos").orEmpty()

            FormScreen2(nombre = nombre, apellidos= apellidos, onBack = { navController.popBackStack()})
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ToDoAppTheme {
        Login()
    }
}