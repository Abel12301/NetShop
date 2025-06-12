package com.cibertec.pe.netshop.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

data class Empleado(
    val nombre: String,
    val puesto: String,
    val salario: String,
    val telefono: String,
    val correo: String,
    val direccion: String,
    val infoAdicional: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmpleadoScreen(navController: NavHostController) {
    var mostrarDialogo by remember { mutableStateOf(false) }
    var empleados by remember { mutableStateOf(listOf<Empleado>()) }

    Scaffold(
        bottomBar = { BottomNavBarEmpleado() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogo = true },
                containerColor = Color(0xFFD9B96A)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        },
        containerColor = Color(0xFFE5E5E5)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            FilterRowEmpleado()
            empleados.forEach { empleado ->
                EmpleadoCard(empleado)
            }
        }

        if (mostrarDialogo) {
            DialogAgregarEmpleado(
                onDismiss = { mostrarDialogo = false },
                onConfirm = { nombre, puesto, salario, telefono, correo, direccion, infoAdicional ->
                    empleados = empleados + Empleado(nombre, puesto, salario, telefono, correo, direccion, infoAdicional)
                    mostrarDialogo = false
                }
            )
        }
    }
}

@Composable
fun FilterRowEmpleado() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        FilterChip("Activos")
        FilterChip("Inactivos")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChip(text: String) {
    AssistChip(
        onClick = { },
        label = { Text(text) },
        leadingIcon = {
            Icon(
                imageVector = if (text == "Activos") Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null
            )
        }
    )
}

@Composable
fun EmpleadoCard(empleado: Empleado) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("📞 ${empleado.telefono}", fontSize = 12.sp, color = Color.Gray)
                Text("📧 ${empleado.correo}", fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(Modifier.height(6.dp))
            Text("👤 ${empleado.nombre}", fontWeight = FontWeight.Bold)
            Text("Puesto: ${empleado.puesto}")
            Text("Salario: ${empleado.salario}")
            Text("📍 Dirección: ${empleado.direccion}")
            if (empleado.infoAdicional.isNotEmpty()) {
                Text("ℹ️ ${empleado.infoAdicional}", fontSize = 12.sp, color = Color.DarkGray)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogAgregarEmpleado(
    onDismiss: () -> Unit,
    onConfirm: (
        String, String, String, String, String, String, String
    ) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var puesto by remember { mutableStateOf("") }
    var salario by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var infoAdicional by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1D2951))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar Empleado", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
                OutlinedTextField(value = puesto, onValueChange = { puesto = it }, label = { Text("Puesto") })
                OutlinedTextField(value = salario, onValueChange = { salario = it }, label = { Text("Salario") })
                OutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = { Text("Teléfono") })
                OutlinedTextField(value = correo, onValueChange = { correo = it }, label = { Text("Correo") })
                OutlinedTextField(value = direccion, onValueChange = { direccion = it }, label = { Text("Dirección") })
                OutlinedTextField(
                    value = infoAdicional,
                    onValueChange = { infoAdicional = it },
                    label = { Text("Información adicional") },
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(nombre, puesto, salario, telefono, correo, direccion, infoAdicional)
            }) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        containerColor = Color.White
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavBarEmpleado() {
    NavigationBar(containerColor = Color(0xFF1D2951)) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Default.Work, contentDescription = null) },
            label = { Text("Empleados") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.Schedule, contentDescription = null) },
            label = { Text("Turnos") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.Badge, contentDescription = null) },
            label = { Text("Cargos") }
        )
    }
}
