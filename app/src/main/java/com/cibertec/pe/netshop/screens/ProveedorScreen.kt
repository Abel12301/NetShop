package com.cibertec.pe.netshop.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProveedorScreen(navController: NavHostController) {
    var mostrarDialogo by remember { mutableStateOf(false) }

    Scaffold(
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
            Text(
                "Proveedores",
                fontSize = 20.sp,
                modifier = Modifier.padding(16.dp)
            )
            ProveedorCard("Proveedor Ejemplo", "Empresa SAC", "999999999", "correo@empresa.com", "Lima, Perú", "Provee bebidas")
        }

        if (mostrarDialogo) {
            DialogAgregarProveedor(
                onDismiss = { mostrarDialogo = false },
                onConfirm = { nombre, empresa, telefono, correo, direccion, infoAdicional ->
                    println("Proveedor: $nombre, $empresa, $telefono, $correo, $direccion, $infoAdicional")
                    mostrarDialogo = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogAgregarProveedor(
    onDismiss: () -> Unit,
    onConfirm: (
        String, String, String, String, String, String
    ) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var empresa by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var infoAdicional by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF1D2951))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Proveedores")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") })
                OutlinedTextField(value = empresa, onValueChange = { empresa = it }, label = { Text("Empresa") })
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
                onConfirm(nombre, empresa, telefono, correo, direccion, infoAdicional)
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

@Composable
fun ProveedorCard(
    nombre: String,
    empresa: String,
    telefono: String,
    correo: String,
    direccion: String,
    infoAdicional: String
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("👤 $nombre", fontSize = 16.sp, color = Color.Black)
            Text("🏢 $empresa", fontSize = 14.sp)
            Text("📞 $telefono", fontSize = 14.sp)
            Text("✉️ $correo", fontSize = 14.sp)
            Text("📍 $direccion", fontSize = 14.sp)
            Text("📝 $infoAdicional", fontSize = 14.sp)
        }
    }
}
