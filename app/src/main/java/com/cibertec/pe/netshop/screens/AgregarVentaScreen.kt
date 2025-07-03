package com.cibertec.pe.netshop.screens

import android.app.Application
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.cibertec.pe.netshop.data.entity.Producto
import com.cibertec.pe.netshop.qrScanned
import com.cibertec.pe.netshop.viewmodel.ProductoViewModel
import com.google.zxing.integration.android.IntentIntegrator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarVentaScreen(
    navController: NavHostController,
    onProductoSeleccionado: (Producto) -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val productoViewModel = remember { ProductoViewModel(application) }
    val productos by productoViewModel.productos.collectAsState()

    var metodoSeleccionado by rememberSaveable { mutableStateOf(1) }
    var busqueda by rememberSaveable { mutableStateOf("") }
    val qrResult by qrScanned
    var selectedIndex by rememberSaveable { mutableStateOf(0) }

    var showBottomSheet by remember { mutableStateOf(false) }
    var busquedaSheet by remember { mutableStateOf("") }
    var isSearchFocused by remember { mutableStateOf(false) }

    val bottomSheetHeight by animateDpAsState(
        targetValue = if (isSearchFocused) 600.dp else 360.dp,
        label = "BottomSheetHeight"
    )

    val productosFiltradosSheet = if (busquedaSheet.isBlank()) productos
    else productos.filter {
        it.nombre.contains(busquedaSheet, ignoreCase = true) || it.clave.contains(busquedaSheet, ignoreCase = true)
    }

    LaunchedEffect(qrResult) {
        qrResult?.let { codigoQR ->
            val productoEncontrado = productos.find { it.clave.equals(codigoQR, ignoreCase = true) }
            qrScanned.value = null
            productoEncontrado?.let { onProductoSeleccionado(it) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agregar Venta", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF1D2951)) {
                NavigationBarItem(
                    selected = selectedIndex == 0,
                    onClick = {
                        selectedIndex = 0
                        showBottomSheet = true
                    },
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = "Producto") },
                    label = { Text("Producto") }
                )
                NavigationBarItem(
                    selected = selectedIndex == 1,
                    onClick = {
                        selectedIndex = 1
                        navController.navigate("manufactura")
                    },
                    icon = { Icon(Icons.Default.Factory, contentDescription = "Manufactura") },
                    label = { Text("Manufactura") }
                )
                NavigationBarItem(
                    selected = selectedIndex == 2,
                    onClick = {
                        selectedIndex = 2
                        navController.navigate("servicio")
                    },
                    icon = { Icon(Icons.Default.Work, contentDescription = "Servicio") },
                    label = { Text("Servicio") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Selecciona el método de ingreso:", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = metodoSeleccionado == 1, onClick = { metodoSeleccionado = 1 })
                    Text("Escanear QR")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = metodoSeleccionado == 2, onClick = { metodoSeleccionado = 2 })
                    Text("Buscar manualmente")
                }
            }

            if (metodoSeleccionado == 1) {
                val activity = context as? ComponentActivity
                Button(
                    onClick = {
                        IntentIntegrator(activity)
                            .setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
                            .setPrompt("Escanea el código QR del producto")
                            .setBeepEnabled(true)
                            .setOrientationLocked(false)
                            .setCaptureActivity(com.cibertec.pe.netshop.ScannerActivity::class.java)
                            .initiateScan()
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Escanear QR")
                }
            }

            if (metodoSeleccionado == 2) {
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    label = { Text("Buscar producto") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                val productosFiltrados = productos.filter {
                    it.nombre.contains(busqueda, ignoreCase = true) || it.clave.contains(busqueda, ignoreCase = true)
                }

                LazyColumn(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(productosFiltrados) { producto ->
                        ProductoCardConImagen(producto = producto) {
                            onProductoSeleccionado(producto)
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = showBottomSheet) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable { showBottomSheet = false }
            ) {
                Surface(
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(bottomSheetHeight)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = busquedaSheet,
                                onValueChange = { busquedaSheet = it },
                                placeholder = { Text("Buscar producto o clave") },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                                    .onFocusChanged { isSearchFocused = it.isFocused },
                                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { isSearchFocused = false })
                            )
                            IconButton(onClick = { showBottomSheet = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Cerrar")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CategoriaChip("Productos", selected = true)
                            CategoriaChip("Manufactura", selected = false)
                            CategoriaChip("Servicio", selected = false)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(productosFiltradosSheet) { producto ->
                                ProductoCardConImagen(producto = producto) {
                                    showBottomSheet = false
                                    onProductoSeleccionado(producto)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductoCardConImagen(producto: Producto, onClick: () -> Unit = {}) {
    val context = LocalContext.current
    val imagenBitmap = remember(producto.imagenUri) {
        producto.imagenUri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(Uri.parse(it))
                BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (imagenBitmap != null) {
                Image(
                    bitmap = imagenBitmap,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .padding(end = 12.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(producto.nombre, fontWeight = FontWeight.Bold)
                Text("Clave: ${producto.clave}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Precio: S/. ${"%.2f".format(producto.precio)}", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun CategoriaChip(texto: String, selected: Boolean) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (selected) MaterialTheme.colorScheme.primary else Color.LightGray,
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier
                .height(32.dp)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = texto,
                color = if (selected) Color.White else Color.Black,
                fontSize = 14.sp
            )
        }
    }
}
