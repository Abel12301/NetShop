// AgregarProductoScreen adaptado para modo oscuro/claro con MaterialTheme

package com.cibertec.pe.netshop.screens

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
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
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.cibertec.pe.netshop.R
import com.cibertec.pe.netshop.ScannerActivity
import com.cibertec.pe.netshop.data.database.AppDatabase
import com.cibertec.pe.netshop.data.entity.Producto
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarProductoScreen(navController: NavController, productoId: Int? = null) {
    val context = LocalContext.current
    val activity = context as? Activity
    val db = AppDatabase.obtenerInstancia(context)

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

    var imagenUri by remember { mutableStateOf<Uri?>(null) }
    var mostrarImagenCompleta by remember { mutableStateOf(false) }
    var productoExistente by remember { mutableStateOf<Producto?>(null) }

    LaunchedEffect(productoId) {
        if (productoId != null) {
            val producto = db.productoDao().obtenerPorId(productoId)
            producto?.let {
                productoExistente = it
                nombre = TextFieldValue(it.nombre)
                costo = TextFieldValue(it.costo.toString())
                precio = TextFieldValue(it.precio.toString())
                clave = TextFieldValue(it.clave)
                cantidadInicial = TextFieldValue(it.cantidadInicial.toString())
                cantidadMinima = TextFieldValue(it.cantidadMinima.toString())
                infoAdicional = TextFieldValue(it.infoAdicional ?: "")
                unidadSeleccionada = it.unidad
                categoriaSeleccionada = it.categoria
                imagenUri = it.imagenUri?.let { uri -> Uri.parse(uri) }
            }
        }
    }

    val launcherGaleria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val file = File(context.filesDir, "img_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { out -> inputStream?.copyTo(out) }
                imagenUri = Uri.fromFile(file)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    val launcherCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            val file = File(context.filesDir, "img_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out) }
            imagenUri = Uri.fromFile(file)
        }
    }

    val scanLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val contents = result.data?.getStringExtra("SCAN_RESULT")
            if (!contents.isNullOrEmpty()) clave = TextFieldValue(contents)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (productoId != null) "Editar Producto" else "Agregar Producto", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                actions = {
                    if (productoId != null) {
                        IconButton(onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                productoExistente?.let { db.productoDao().eliminar(it) }
                                launch(Dispatchers.Main) { navController.popBackStack() }
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.White)
                        }
                    }
                    IconButton(onClick = {
                        val nuevoProducto = Producto(
                            id = productoExistente?.id ?: 0,
                            nombre = nombre.text,
                            costo = costo.text.toDoubleOrNull() ?: 0.0,
                            precio = precio.text.toDoubleOrNull() ?: 0.0,
                            clave = clave.text,
                            cantidadInicial = cantidadInicial.text.toIntOrNull() ?: 0,
                            cantidadMinima = cantidadMinima.text.toIntOrNull() ?: 0,
                            unidad = unidadSeleccionada,
                            categoria = categoriaSeleccionada,
                            infoAdicional = infoAdicional.text,
                            imagenUri = imagenUri?.toString()
                        )
                        CoroutineScope(Dispatchers.IO).launch {
                            if (productoId != null) db.productoDao().actualizar(nuevoProducto)
                            else db.productoDao().insertar(nuevoProducto)
                            launch(Dispatchers.Main) { navController.popBackStack() }
                        }
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Guardar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1D2951))
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(costo, { costo = it }, label = { Text("Costo") }, modifier = Modifier.weight(1f))
                OutlinedTextField(precio, { precio = it }, label = { Text("Precio") }, modifier = Modifier.weight(1f))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (imagenUri != null) {
                    Image(painter = rememberAsyncImagePainter(imagenUri), contentDescription = null, modifier = Modifier.fillMaxSize())
                } else {
                    Image(painter = painterResource(id = R.drawable.placeholder_producto), contentDescription = null, modifier = Modifier.size(100.dp).align(Alignment.Center))
                }
                if (imagenUri != null) {
                    IconButton(onClick = { mostrarImagenCompleta = true }, modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)) {
                        Icon(Icons.Default.Visibility, contentDescription = "Ver imagen")
                    }
                }
                Row(modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)) {
                    IconButton({ imagenUri = null }) { Icon(Icons.Default.Close, contentDescription = "Eliminar") }
                    IconButton({ launcherGaleria.launch("image/*") }) { Icon(Icons.Default.Image, contentDescription = "Galería") }
                    IconButton({ launcherCamara.launch(null) }) { Icon(Icons.Default.CameraAlt, contentDescription = "Cámara") }
                }
            }

            OutlinedTextField(clave, { clave = it }, label = { Text("Clave") }, modifier = Modifier.fillMaxWidth(), trailingIcon = {
                IconButton(onClick = {
                    val integrator = IntentIntegrator(activity)
                    integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
                    integrator.setPrompt("Escanea el código")
                    integrator.setBeepEnabled(true)
                    integrator.setOrientationLocked(false)
                    integrator.setCaptureActivity(ScannerActivity::class.java)
                    scanLauncher.launch(integrator.createScanIntent())
                }) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Escanear QR")
                }
            })

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(cantidadInicial, { cantidadInicial = it }, label = { Text("Cantidad Inicial") }, modifier = Modifier.weight(1f))
                OutlinedTextField(cantidadMinima, { cantidadMinima = it }, label = { Text("Cantidad Mínima") }, modifier = Modifier.weight(1f))
            }

            Box(modifier = Modifier.fillMaxWidth().clickable { mostrarDialogoUnidad = true }.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp)).padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Unidad: $unidadSeleccionada", color = MaterialTheme.colorScheme.onSurface)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            Box(modifier = Modifier.fillMaxWidth().clickable { mostrarDialogoCategoria = true }.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp)).padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Categoría: $categoriaSeleccionada", color = MaterialTheme.colorScheme.onSurface)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            OutlinedTextField(infoAdicional, { infoAdicional = it }, label = { Text("Información Adicional") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
        }
    }

    if (mostrarImagenCompleta && imagenUri != null) {
        AlertDialog(onDismissRequest = { mostrarImagenCompleta = false }, confirmButton = {}, text = {
            Image(painter = rememberAsyncImagePainter(imagenUri), contentDescription = null, modifier = Modifier.fillMaxWidth().aspectRatio(1f))
        })
    }

    if (mostrarDialogoUnidad) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoUnidad = false },
            title = { Text("Seleccionar o Agregar Unidad") },
            text = {
                Column {
                    unidades.forEach {
                        Text(it, modifier = Modifier.fillMaxWidth().clickable {
                            unidadSeleccionada = it
                            mostrarDialogoUnidad = false
                        }.padding(8.dp))
                    }
                    OutlinedTextField(nuevaUnidad, { nuevaUnidad = it }, label = { Text("Nueva Unidad") })
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
            dismissButton = { TextButton(onClick = { mostrarDialogoUnidad = false }) { Text("Cancelar") } }
        )
    }

    if (mostrarDialogoCategoria) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCategoria = false },
            title = { Text("Seleccionar o Agregar Categoría") },
            text = {
                Column {
                    categorias.forEach {
                        Text(it, modifier = Modifier.fillMaxWidth().clickable {
                            categoriaSeleccionada = it
                            mostrarDialogoCategoria = false
                        }.padding(8.dp))
                    }
                    OutlinedTextField(nuevaCategoria, { nuevaCategoria = it }, label = { Text("Nueva Categoría") })
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
            dismissButton = { TextButton(onClick = { mostrarDialogoCategoria = false }) { Text("Cancelar") } }
        )
    }
}
