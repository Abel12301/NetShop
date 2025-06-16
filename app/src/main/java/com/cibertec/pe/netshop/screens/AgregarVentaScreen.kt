package com.cibertec.pe.netshop.screens

import android.app.Application
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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

    LaunchedEffect(qrResult) {
        qrResult?.let { codigoQR ->
            val productoEncontrado = productos.find { it.clave.equals(codigoQR, ignoreCase = true) }
            qrScanned.value = null
            productoEncontrado?.let { onProductoSeleccionado(it) }
        }
    }

    val productosFiltrados = if (busqueda.isNotBlank()) {
        productos.filter {
            it.nombre.contains(busqueda, ignoreCase = true) || it.clave.contains(busqueda, ignoreCase = true)
        }
    } else productos

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Agregar Venta", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Selecciona el método de ingreso:",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = metodoSeleccionado == 1, onClick = { metodoSeleccionado = 1 })
                    Text("Escanear QR", color = MaterialTheme.colorScheme.onBackground)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = metodoSeleccionado == 2, onClick = { metodoSeleccionado = 2 })
                    Text("Buscar manualmente", color = MaterialTheme.colorScheme.onBackground)
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
                            .initiateScan()
                    },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Escanear QR", color = MaterialTheme.colorScheme.onPrimary)
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

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    productosFiltrados.forEach { producto ->
                        ProductoCardConImagen(producto = producto, onClick = {
                            onProductoSeleccionado(producto)
                        })
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
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
                Text(
                    text = producto.nombre,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Clave: ${producto.clave}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Precio: S/. ${"%.2f".format(producto.precio)}",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
