// AgregarProductoScreen.kt
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
import com.cibertec.pe.netshop.data.entity.*
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.flow.first


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

    var unidades by remember { mutableStateOf(listOf<String>()) }
    var categorias by remember { mutableStateOf(listOf<String>()) }

    var mostrarDialogoUnidad by remember { mutableStateOf(false) }
    var nuevaUnidad by remember { mutableStateOf(TextFieldValue()) }
    var mostrarDialogoCategoria by remember { mutableStateOf(false) }
    var nuevaCategoria by remember { mutableStateOf(TextFieldValue()) }

    var imagenUri by remember { mutableStateOf<Uri?>(null) }
    var mostrarImagenCompleta by remember { mutableStateOf(false) }
    var productoExistente by remember { mutableStateOf<Producto?>(null) }

    val enModoEdicion = productoId != null

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

    LaunchedEffect(Unit) {
        unidades = db.unidadDao().obtenerTodos().map { it.nombre }
        categorias = db.categoriaDao().obtenerTodos().map { it.nombre }
    }

    val launcherGaleria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val file = File(context.filesDir, "img_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out -> inputStream?.copyTo(out) }
            imagenUri = Uri.fromFile(file)
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
                    Image(
                        painter = rememberAsyncImagePainter(imagenUri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.placeholder_producto),
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .align(Alignment.Center)
                    )
                }

                // Ícono del ojo a la izquierda abajo
                if (enModoEdicion && imagenUri != null) {
                    IconButton(
                        onClick = { mostrarImagenCompleta = true },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Ver Imagen",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Otros íconos a la derecha abajo
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = { imagenUri = null }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    IconButton(onClick = { launcherGaleria.launch("image/*") }) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Galería",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { launcherCamara.launch(null) }) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Cámara",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }}
            }

            if (mostrarImagenCompleta && imagenUri != null) {
                AlertDialog(
                    onDismissRequest = { mostrarImagenCompleta = false },
                    confirmButton = {},
                    text = {
                        Image(
                            painter = rememberAsyncImagePainter(imagenUri),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                        )
                    }
                )
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

    // Estados para mostrar inputs dinámicos
    var mostrandoInputUnidad by remember { mutableStateOf(false) }
    var mostrandoInputCategoria by remember { mutableStateOf(false) }

    val gold = Color(0xFFFFC107)
    val backgroundDialog = Color(0xFFFDFDFD)

    if (mostrarDialogoUnidad) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoUnidad = false
                mostrandoInputUnidad = false
                nuevaUnidad = TextFieldValue("")
            },
            title = { Text("Seleccionar o Agregar Unidad", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {

                    // Botón +
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Unidades disponibles", fontWeight = FontWeight.SemiBold)
                        IconButton(onClick = {
                            mostrandoInputUnidad = true
                            nuevaUnidad = TextFieldValue("")
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Agregar", tint = gold)
                        }
                    }

                    // Campo dinámico de entrada
                    if (mostrandoInputUnidad) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = nuevaUnidad,
                                onValueChange = { nuevaUnidad = it },
                                label = { Text("Nueva Unidad") },
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                val texto = nuevaUnidad.text.trim()
                                if (texto.isNotEmpty()) {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        db.unidadDao().insertar(Unidad(nombre = texto))
                                        unidades = db.unidadDao().obtenerTodos().map { it.nombre }
                                        unidadSeleccionada = texto
                                        nuevaUnidad = TextFieldValue("")
                                        mostrandoInputUnidad = false
                                        mostrarDialogoUnidad = false
                                    }
                                }
                            }) {
                                Icon(Icons.Default.Check, contentDescription = "Aceptar", tint = Color(0xFF4CAF50))
                            }
                            IconButton(onClick = {
                                mostrandoInputUnidad = false
                                nuevaUnidad = TextFieldValue("")
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = Color(0xFFF44336))
                            }
                        }
                    }

                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(unidades) { unidad ->
                            var expanded by remember { mutableStateOf(false) }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    unidad,
                                    modifier = Modifier
                                        .clickable {
                                            unidadSeleccionada = unidad
                                            mostrarDialogoUnidad = false
                                        }
                                        .weight(1f)
                                )
                                Box {
                                    IconButton(onClick = { expanded = true }) {
                                        Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                                    }
                                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                        DropdownMenuItem(
                                            text = { Text("Editar") },
                                            onClick = {
                                                nuevaUnidad = TextFieldValue(unidad)
                                                unidades = unidades - unidad
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    db.unidadDao().obtenerTodos().firstOrNull { it.nombre == unidad }
                                                        ?.let { db.unidadDao().eliminar(it) }
                                                }
                                                mostrandoInputUnidad = true
                                                expanded = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Eliminar") },
                                            onClick = {
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    db.unidadDao().obtenerTodos().firstOrNull { it.nombre == unidad }
                                                        ?.let { db.unidadDao().eliminar(it) }
                                                    unidades = db.unidadDao().obtenerTodos().map { it.nombre }
                                                }
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogoUnidad = false
                    mostrandoInputUnidad = false
                    nuevaUnidad = TextFieldValue("")
                }) {
                    Text("Cerrar", color = Color.Gray)
                }
            }
        )
    }

    if (mostrarDialogoCategoria) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogoCategoria = false
                mostrandoInputCategoria = false
                nuevaCategoria = TextFieldValue("")
            },
            title = { Text("Seleccionar o Agregar Categoría", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Categorías disponibles", fontWeight = FontWeight.SemiBold)
                        IconButton(onClick = {
                            mostrandoInputCategoria = true
                            nuevaCategoria = TextFieldValue("")
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Agregar", tint = gold)
                        }
                    }

                    if (mostrandoInputCategoria) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = nuevaCategoria,
                                onValueChange = { nuevaCategoria = it },
                                label = { Text("Nueva Categoría") },
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                val texto = nuevaCategoria.text.trim()
                                if (texto.isNotEmpty()) {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        db.categoriaDao().insertar(Categoria(nombre = texto))
                                        categorias = db.categoriaDao().obtenerTodos().map { it.nombre }
                                        categoriaSeleccionada = texto
                                        nuevaCategoria = TextFieldValue("")
                                        mostrandoInputCategoria = false
                                        mostrarDialogoCategoria = false
                                    }
                                }
                            }) {
                                Icon(Icons.Default.Check, contentDescription = "Aceptar", tint = Color(0xFF4CAF50))
                            }
                            IconButton(onClick = {
                                mostrandoInputCategoria = false
                                nuevaCategoria = TextFieldValue("")
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = Color(0xFFF44336))
                            }
                        }
                    }

                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(categorias) { categoria ->
                            var expanded by remember { mutableStateOf(false) }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    categoria,
                                    modifier = Modifier
                                        .clickable {
                                            categoriaSeleccionada = categoria
                                            mostrarDialogoCategoria = false
                                        }
                                        .weight(1f)
                                )
                                Box {
                                    IconButton(onClick = { expanded = true }) {
                                        Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                                    }
                                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                        DropdownMenuItem(
                                            text = { Text("Editar") },
                                            onClick = {
                                                nuevaCategoria = TextFieldValue(categoria)
                                                categorias = categorias - categoria
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    db.categoriaDao().obtenerTodos().firstOrNull { it.nombre == categoria }
                                                        ?.let { db.categoriaDao().eliminar(it) }
                                                }
                                                mostrandoInputCategoria = true
                                                expanded = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Eliminar") },
                                            onClick = {
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    db.categoriaDao().obtenerTodos().firstOrNull { it.nombre == categoria }
                                                        ?.let { db.categoriaDao().eliminar(it) }
                                                    categorias = db.categoriaDao().obtenerTodos().map { it.nombre }
                                                }
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogoCategoria = false
                    mostrandoInputCategoria = false
                    nuevaCategoria = TextFieldValue("")
                }) {
                    Text("Cerrar", color = Color.Gray)
                }
            }
        )
    }


    if (mostrarImagenCompleta && imagenUri != null) {
        AlertDialog(
            onDismissRequest = { mostrarImagenCompleta = false },
            confirmButton = {},
            text = {
                Image(painter = rememberAsyncImagePainter(imagenUri), contentDescription = null, modifier = Modifier.fillMaxWidth().aspectRatio(1f))
            }
        )
    }
}
