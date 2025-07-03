package com.cibertec.pe.netshop

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.cibertec.pe.netshop.data.entity.Producto
import com.cibertec.pe.netshop.utils.generarQRyGuardar
import com.cibertec.pe.netshop.viewmodel.ProductoViewModel
import com.google.zxing.integration.android.IntentIntegrator

@Composable
fun ProductosScreen(
    navController: NavHostController,
    viewModel: ProductoViewModel = viewModel()
) {
    var buscadorActivo by remember { mutableStateOf(false) }
    var textoBusqueda by remember { mutableStateOf("") }

    val productos by viewModel.productos.collectAsState()
    val filtrarCantidadMinima by viewModel.filtrarCantidadMinima.collectAsState()

    var filtrarExportarQR by remember { mutableStateOf(false) }

    // Código QR escaneado
    var filtroQR by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val activity = context as? Activity

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val qrCode = result.data?.getStringExtra("qr_result")
            if (!qrCode.isNullOrEmpty()) {
                filtroQR = qrCode.trim() // 👈 Usa directamente el texto escaneado

                textoBusqueda = ""
                buscadorActivo = false
                Toast.makeText(context, "Escaneado: $filtroQR", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "No se escaneó ningún código", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Escaneo cancelado", Toast.LENGTH_SHORT).show()
        }
    }


    val productosFiltrados = if (filtroQR != null) {
        val qrLimpio = filtroQR!!.trim().lowercase()
        val resultado = productos.filter { producto ->
            val clave = producto.clave.trim().lowercase()
            println("🔍 Comparando: clave='$clave' vs QR='$qrLimpio'")
            clave == qrLimpio
        }

        if (resultado.isEmpty()) {
            Toast.makeText(context, "⚠️ Producto no encontrado para '$qrLimpio'", Toast.LENGTH_SHORT).show()
        }

        resultado


} else {
        productos.filter { producto ->
            val coincideBusqueda = if (buscadorActivo && textoBusqueda.isNotBlank()) {
                producto.nombre.contains(textoBusqueda, ignoreCase = true) ||
                        producto.clave.contains(textoBusqueda, ignoreCase = true)
            } else true

            val coincideFiltroCantidad = if (filtrarCantidadMinima) {
                producto.cantidadInicial == producto.cantidadMinima
            } else true

            coincideBusqueda && coincideFiltroCantidad
        }
    }

    Scaffold(
        bottomBar = { BottomNavBar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("agregar_producto") },
                containerColor = Color(0xFFD9B96A)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            TopBarProductos(
                buscadorActivo = buscadorActivo,
                textoBusqueda = textoBusqueda,
                onBuscarClick = {
                    buscadorActivo = !buscadorActivo
                    if (!buscadorActivo) textoBusqueda = ""
                    filtroQR = null // 👈 limpia QR al cambiar búsqueda manual
                },
                onTextChange = { textoBusqueda = it }
            )

            FilterRow(
                filtrarCantidadMinima = filtrarCantidadMinima,
                onCantidadMinimaToggle = { viewModel.toggleCantidadMinima() },
                filtrarExportarQR = filtrarExportarQR,
                onExportarQRToggle = {
                    filtrarExportarQR = !filtrarExportarQR
                    if (filtrarExportarQR) navController.navigate("exportar_qr")
                },
                navController = navController,
                onEscanearQRClick = {
                    filtroQR = null // limpia QR antes de escanear
                    activity?.let {
                        val intent = Intent(context, QRScanActivity::class.java) // ✅ CLASE CORRECTA
                        scanLauncher.launch(intent)
                    }
                }
            )

            // ✅ INDICADOR DE FILTRO QR ACTIVO
            if (filtroQR != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2196F3))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Mostrando producto escaneado: \"$filtroQR\"",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(onClick = { filtroQR = null }) {
                        Text("Limpiar", color = Color.White)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(productosFiltrados) { producto ->
                    ProductoItem(producto) {
                        navController.navigate("agregar_producto/${producto.id}")
                    }
                }
            }
        }
    }
}


@Composable
fun TopBarProductos(
    buscadorActivo: Boolean,
    textoBusqueda: String,
    onBuscarClick: () -> Unit,
    onTextChange: (String) -> Unit
) {
    val fondoCabecera = Color(0xFF1D2951) // Color fijo

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(fondoCabecera)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (buscadorActivo) {
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = onTextChange,
                placeholder = { Text("Buscar producto...") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .background(Color.White, shape = MaterialTheme.shapes.small),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )
        } else {
            Text(
                "Productos",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
        }

        IconButton(onClick = onBuscarClick) {
            Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color.White)
        }
    }
}

@Composable
fun FilterRow(
    filtrarCantidadMinima: Boolean,
    onCantidadMinimaToggle: () -> Unit,
    filtrarExportarQR: Boolean,
    onExportarQRToggle: () -> Unit,
    navController: NavHostController,
    onEscanearQRClick: () -> Unit   // NUEVO parámetro
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // NUEVO filtro para escanear QR
        FilterChip(
            selected = false,
            onClick = onEscanearQRClick,
            label = { Text("Escanear QR") },
            leadingIcon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFF2196F3),
                selectedLabelColor = Color.White
            )
        )
        FilterChip(
            selected = filtrarCantidadMinima,
            onClick = onCantidadMinimaToggle,
            label = { Text("Cantidad Mínima") },
            leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFFFFC107),
                selectedLabelColor = Color.Black
            )
        )

        FilterChip(
            selected = filtrarExportarQR,
            onClick = onExportarQRToggle,
            label = { Text("Exportar QR") },
            leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFF4CAF50),
                selectedLabelColor = Color.White,
                selectedLeadingIconColor = Color.White
            )
        )


    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChip(text: String, onClick: () -> Unit, selected: Boolean) {
    AssistChip(
        onClick = onClick,
        label = { Text(text) },
        leadingIcon = {
            if (text == "Merma")
                Icon(Icons.Default.Close, contentDescription = null)
            else
                Icon(Icons.Default.Warning, contentDescription = null)
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected) Color(0xFFFFC107) else MaterialTheme.colorScheme.surface
        )
    )
}


@Composable
fun ProductoItem(producto: Producto, onClick: () -> Unit) {
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
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            if (imagenBitmap != null) {
                Image(
                    bitmap = imagenBitmap,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.placeholder_producto),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = producto.clave, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = producto.unidad, style = MaterialTheme.typography.bodySmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = producto.nombre, style = MaterialTheme.typography.titleMedium)
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val colorCantidad = when {
                        producto.cantidadInicial == 0 -> Color.Red
                        producto.cantidadInicial == producto.cantidadMinima -> Color(0xFFFFC107) // Dorado
                        else -> Color.Gray
                    }

                    Text(
                        text = "#${producto.cantidadInicial}",
                        color = colorCantidad,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "S/. ${producto.precio}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

fun exportarQRProductosSinClave(
    context: Context,
    productos: List<Producto>
) {
    val productosSinClave = productos.filter { it.clave.isBlank() }

    if (productosSinClave.isEmpty()) {
        Toast.makeText(context, "Todos los productos ya tienen clave", Toast.LENGTH_SHORT).show()
        return
    }

    productosSinClave.forEach { producto ->
        val contenidoQR = "Producto: ${producto.nombre} (ID: ${producto.id})"
        generarQRyGuardar(context, contenidoQR, producto.nombre)
    }

    Toast.makeText(context, "QRs exportados para productos sin clave", Toast.LENGTH_LONG).show()
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavBar() {
    NavigationBar(containerColor = Color(0xFF1D2951)) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Default.Inventory2, contentDescription = null) },
            label = { Text("Producto") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.Factory, contentDescription = null) },
            label = { Text("Manufactura") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.Work, contentDescription = null) },
            label = { Text("Servicio") }
        )
    }
}
