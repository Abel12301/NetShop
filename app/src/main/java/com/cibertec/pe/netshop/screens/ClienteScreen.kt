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

data class Cliente(
    val nombre: String,
    val alias: String,
    val telefono: String,
    val correo: String,
    val direccion: String,
    val infoAdicional: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteScreen(navController: NavHostController) {
    var mostrarDialogo by remember { mutableStateOf(false) }
    var clientes by remember { mutableStateOf(listOf<Cliente>()) }

    Scaffold(
        bottomBar = { BottomNavBarCliente() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogo = true },
                containerColor = Color(0xFFD9B96A)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Cliente")
            }
        },
        containerColor = Color(0xFFE5E5E5)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            clientes.forEach { cliente ->
                ClienteCard(cliente)
            }
        }

        if (mostrarDialogo) {
            DialogAgregarCliente(
                onDismiss = { mostrarDialogo = false },
                onConfirm = { nombre, alias, telefono, correo, direccion, infoAdicional ->
                    clientes = clientes + Cliente(nombre, alias, telefono, correo, direccion, infoAdicional)
                    mostrarDialogo = false
                }
            )
        }
    }
}

@Composable
fun ClienteCard(cliente: Cliente) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("👤 ${cliente.nombre}", fontWeight = FontWeight.Bold)
            Text("Alias: ${cliente.alias}")
            Text("📞 ${cliente.telefono}")
            Text("📧 ${cliente.correo}")
            Text("📍 Dirección: ${cliente.direccion}")
            if (cliente.infoAdicional.isNotEmpty()) {
                Text("ℹ️ ${cliente.infoAdicional}", fontSize = 12.sp, color = Color.DarkGray)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogAgregarCliente(
    onDismiss: () -> Unit,
    onConfirm: (
        String, String, String, String, String, String
    ) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var alias by remember { mutableStateOf("") }
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
                Text("Agregar Cliente", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
                OutlinedTextField(value = alias, onValueChange = { alias = it }, label = { Text("Alias") })
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
                onConfirm(nombre, alias, telefono, correo, direccion, infoAdicional)
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
fun BottomNavBarCliente() {
    NavigationBar(containerColor = Color(0xFF1D2951)) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Default.Group, contentDescription = null) },
            label = { Text("Clientes") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
            label = { Text("Pedidos") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.Star, contentDescription = null) },
            label = { Text("Favoritos") }
        )
    }
}
