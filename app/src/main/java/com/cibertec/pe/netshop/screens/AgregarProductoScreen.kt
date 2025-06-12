package com.cibertec.pe.netshop.screens

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cibertec.pe.netshop.R
import com.cibertec.pe.netshop.ScannerActivity
import com.google.zxing.integration.android.IntentIntegrator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarProductoScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as? Activity

    var nombre by remember { mutableStateOf(TextFieldValue()) }
    var costo by remember { mutableStateOf(TextFieldValue()) }
    var precio by remember { mutableStateOf(TextFieldValue()) }
    var clave by remember { mutableStateOf(TextFieldValue()) }
    var cantidadInicial by remember { mutableStateOf(TextFieldValue()) }
    var cantidadMinima by remember { mutableStateOf(TextFieldValue()) }
    var infoAdicional by remember { mutableStateOf(TextFieldValue()) }

    var unidadSeleccionada by remember { mutableStateOf("Unidad") }
    var categoriaSeleccionada by remember { mutableStateOf("Categoría") }

    var unidades by remember { mutableStateOf(mutableListOf("Unidad", "Caja", "Paquete")) }
    var categorias by remember { mutableStateOf(mutableListOf("Bebidas", "Comida", "Limpieza")) }

    var mostrarDialogoUnidad by remember { mutableStateOf(false) }
    var nuevaUnidad by remember { mutableStateOf(TextFieldValue()) }

    var mostrarDialogoCategoria by remember { mutableStateOf(false) }
    var nuevaCategoria by remember { mutableStateOf(TextFieldValue()) }

    // ✅ Lanza escáner QR/barcode y obtiene resultado
    val scanLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val contents = data?.getStringExtra("SCAN_RESULT")
            if (!contents.isNullOrEmpty()) {
                clave = TextFieldValue(contents)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar Producto", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Guardar */ }) {
                        Icon(Icons.Default.Check, contentDescription = "Guardar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1D2951))
            )
        },
        containerColor = Color(0xFFE5E5E5)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Campos Obligatorios", fontSize = 14.sp)

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                leadingIcon = { Icon(Icons.Default.Inventory2, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = costo,
                    onValueChange = { costo = it },
                    label = { Text("Costo") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = precio,
                    onValueChange = { precio = it },
                    label = { Text("Precio") },
                    modifier = Modifier.weight(1f)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.placeholder_producto),
                    contentDescription = null,
                    modifier = Modifier.size(100.dp)
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Close, contentDescription = "Eliminar")
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Image, contentDescription = "Galería")
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Cámara")
                    }
                }
            }

            Text("*Campos Opcionales", fontSize = 14.sp)

            OutlinedTextField(
                value = clave,
                onValueChange = { clave = it },
                label = { Text("Clave") },
                trailingIcon = {
                    IconButton(onClick = {
                        activity?.let {
                            val integrator = IntentIntegrator(it).apply {
                                setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
                                setPrompt("Escanea un código")
                                setBeepEnabled(true)
                                setOrientationLocked(true)
                                setCaptureActivity(ScannerActivity::class.java)
                            }
                            val intent = integrator.createScanIntent()
                            scanLauncher.launch(intent)
                        }
                    }) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Escanear QR")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = cantidadInicial,
                    onValueChange = { cantidadInicial = it },
                    label = { Text("Cantidad Inicial") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = cantidadMinima,
                    onValueChange = { cantidadMinima = it },
                    label = { Text("Cantidad Mínima") },
                    modifier = Modifier.weight(1f)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { mostrarDialogoUnidad = true }
                    .background(Color.White, RoundedCornerShape(4.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Unidad: $unidadSeleccionada", color = Color.Black)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { mostrarDialogoCategoria = true }
                    .background(Color.White, RoundedCornerShape(4.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Categoría: $categoriaSeleccionada", color = Color.Black)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }

            OutlinedTextField(
                value = infoAdicional,
                onValueChange = { infoAdicional = it },
                label = { Text("Información Adicional") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
        }
    }

    if (mostrarDialogoUnidad) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoUnidad = false },
            title = { Text("Seleccionar o Agregar Unidad") },
            text = {
                Column {
                    unidades.forEach {
                        Text(
                            text = it,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    unidadSeleccionada = it
                                    mostrarDialogoUnidad = false
                                }
                                .padding(8.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nuevaUnidad,
                        onValueChange = { nuevaUnidad = it },
                        label = { Text("Nueva Unidad") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val nueva = nuevaUnidad.text.trim()
                    if (nueva.isNotEmpty()) {
                        unidades.add(nueva)
                        unidadSeleccionada = nueva
                        nuevaUnidad = TextFieldValue("")
                        mostrarDialogoUnidad = false
                    }
                }) { Text("Agregar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoUnidad = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (mostrarDialogoCategoria) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCategoria = false },
            title = { Text("Seleccionar o Agregar Categoría") },
            text = {
                Column {
                    categorias.forEach {
                        Text(
                            text = it,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    categoriaSeleccionada = it
                                    mostrarDialogoCategoria = false
                                }
                                .padding(8.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nuevaCategoria,
                        onValueChange = { nuevaCategoria = it },
                        label = { Text("Nueva Categoría") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val nueva = nuevaCategoria.text.trim()
                    if (nueva.isNotEmpty()) {
                        categorias.add(nueva)
                        categoriaSeleccionada = nueva
                        nuevaCategoria = TextFieldValue("")
                        mostrarDialogoCategoria = false
                    }
                }) { Text("Agregar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCategoria = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
