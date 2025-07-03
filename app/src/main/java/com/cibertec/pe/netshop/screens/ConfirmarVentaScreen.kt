package com.cibertec.pe.netshop.screens

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.cibertec.pe.netshop.data.entity.DetalleVenta
import com.cibertec.pe.netshop.data.entity.Producto
import com.cibertec.pe.netshop.data.entity.Venta
import com.cibertec.pe.netshop.viewmodel.VentaViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmarVentaScreen(
    navController: NavHostController,
    productosSeleccionadosInicial: List<Producto>,
    cantidadesInicial: Map<Int, Int>,
    clienteId: Int? = null,
    empleadoId: Int = 1,
    onAgregarMasProductos: (() -> Unit)? = null
) {
    val context = LocalContext.current.applicationContext as Application
    val ventaViewModel = remember { VentaViewModel(context) }
    val scope = rememberCoroutineScope()

    val productosSeleccionados = productosSeleccionadosInicial as MutableList<Producto>
    val cantidades = cantidadesInicial as MutableMap<Int, Int>
    val precios = remember { mutableStateMapOf<Int, Double>() }

    var metodoPago by remember { mutableStateOf("Efectivo") }
    var mostrarDialogoConfirmacion by remember { mutableStateOf(false) }
    var montoRecibido by remember { mutableStateOf("") }
    var selectedClienteId by remember { mutableStateOf(clienteId ?: 0) }
    var selectedEmpleadoId by remember { mutableStateOf(empleadoId) }
    var productoDetalle by remember { mutableStateOf<Producto?>(null) }
    val stockActual = remember { mutableStateMapOf<Int, Int>() }
    val mostrarMensajeStock = remember { mutableStateMapOf<Int, Boolean>() }

    val fecha = obtenerSoloFecha()
    val hora = obtenerSoloHora()

    LaunchedEffect(Unit) {
        productosSeleccionados.forEach {
            if (it.id !in precios) precios[it.id] = it.precio

            // Cargar el stock actual desde la base de datos
            val productoDb = ventaViewModel.obtenerProductoPorIdSuspend(it.id)
            productoDb?.let { p -> stockActual[it.id] = p.cantidadInicial }
        }
    }

    val total = productosSeleccionados.sumOf {
        (cantidades[it.id] ?: 0) * (precios[it.id] ?: it.precio)
    }
    val hayStockInsuficiente = productosSeleccionados.any { producto ->
        val cantidad = cantidades[producto.id] ?: 0
        val stock = stockActual[producto.id] ?: producto.cantidadInicial
        cantidad > stock
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirmar Venta", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },

        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                FloatingActionButton(
                    onClick = { onAgregarMasProductos?.invoke() },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar más productos")
                }
                FloatingActionButton(
                    onClick = {
                        if (!hayStockInsuficiente) {
                            mostrarDialogoConfirmacion = true
                        }
                    },
                    containerColor = if (hayStockInsuficiente) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Confirmar",
                        tint = if (hayStockInsuficiente) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
                    )
                }
                if (hayStockInsuficiente) {
                    Text(
                        text = "⚠ Stock insuficiente en uno o más productos.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, end = 12.dp)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("🛒 Productos en la venta", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)

            LazyColumn(modifier = Modifier.fillMaxHeight(0.45f)) {
                items(productosSeleccionados) { producto ->
                    val cantidad = cantidades[producto.id] ?: 1
                    val precio = precios[producto.id] ?: producto.precio
                    val subtotal = cantidad * precio
                    val stock = stockActual[producto.id] ?: producto.cantidadInicial

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
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (imagenBitmap != null) {
                                    Image(
                                        bitmap = imagenBitmap,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("IMG", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(producto.clave, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text(producto.nombre, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Row {
                                    IconButton(onClick = { productoDetalle = producto }) {
                                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = {
                                        productosSeleccionados.remove(producto)
                                        cantidades.remove(producto.id)
                                        precios.remove(producto.id)
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedButton(onClick = {
                                    if (cantidad > 1) {
                                        cantidades[producto.id] = cantidad - 1
                                        val nuevoValor = cantidad - 1
                                        if (nuevoValor <= stock) mostrarMensajeStock[producto.id] = false
                                    }
                                }) {
                                    Text("-")
                                }

                                Text(cantidad.toString(), modifier = Modifier.padding(horizontal = 16.dp))

                                OutlinedButton(onClick = {
                                    if (cantidad < stock) {
                                        cantidades[producto.id] = cantidad + 1
                                        mostrarMensajeStock[producto.id] = false
                                    } else {
                                        mostrarMensajeStock[producto.id] = true
                                    }
                                }) {
                                    Text("+")
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                OutlinedTextField(
                                    value = precio.toString(),
                                    onValueChange = {
                                        precios[producto.id] = it.toDoubleOrNull() ?: precio
                                    },
                                    label = { Text("Precio") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Total: S/. %.2f".format(subtotal),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                if (mostrarMensajeStock[producto.id] == true) {
                                    Text(
                                        "⚠ sin existencia (disponible: $stock)",
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            } // ← CIERRA LazyColumn correctamente

            Text("\uD83D\uDCB5 Total: S/. ${"%.2f".format(total)}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
            Divider(color = MaterialTheme.colorScheme.outline)

            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("\uD83D\uDCC5 Fecha: $fecha", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("⏰ Hora: $hora", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            OutlinedTextField(
                value = selectedClienteId.toString(),
                onValueChange = { selectedClienteId = it.toIntOrNull() ?: 0 },
                label = { Text("ID del Cliente") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = selectedEmpleadoId.toString(),
                onValueChange = { selectedEmpleadoId = it.toIntOrNull() ?: 1 },
                label = { Text("ID del Empleado") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("\uD83D\uDCB3 Método de Pago", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("Efectivo", "Yape", "Plin").forEach {
                    FilterChip(
                        selected = metodoPago == it,
                        onClick = { metodoPago = it },
                        label = { Text(it) }
                    )
                }
            }
        }
    }

    // Confirmación de venta
    if (mostrarDialogoConfirmacion) {
        if (metodoPago == "Yape") {
            val qrText = "982296407"  // Solo el número, para que funcione en Yape real
            val qrBitmap = generarQR(qrText)

            AlertDialog(
                onDismissRequest = { mostrarDialogoConfirmacion = false },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            val venta = Venta(
                                fecha = fecha,
                                hora = hora,
                                metodoPago = metodoPago,
                                total = total,
                                empleadoId = selectedEmpleadoId,
                                clienteId = selectedClienteId
                            )
                            val detalles = productosSeleccionados.map {
                                val cantidad = cantidades[it.id] ?: 0
                                val precio = precios[it.id] ?: it.precio
                                DetalleVenta(
                                    productoId = it.id,
                                    cantidad = cantidad,
                                    precioUnitario = precio,
                                    subtotal = cantidad * precio,
                                    ventaId = 0
                                )
                            }

                            val ventaId = ventaViewModel.registrarVentaConDetalles(venta, detalles)
                            ventaViewModel.actualizarStockProductos(detalles)
                            mostrarDialogoConfirmacion = false
                            montoRecibido = ""
                            navController.navigate("venta_confirmada/$ventaId")
                        }
                    }) { Text("Confirmar") }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoConfirmacion = false }) {
                        Text("Cancelar")
                    }
                },
                title = { Text("Paga con Yape", fontWeight = FontWeight.Bold) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        qrBitmap?.let {
                            Image(bitmap = it, contentDescription = null, modifier = Modifier.size(250.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("N°: 982296407", fontWeight = FontWeight.SemiBold)
                            Text("Monto: S/. ${"%.2f".format(total)}")
                            Text("Escanea el QR con tu app Yape y presiona Confirmar", fontSize = 12.sp)
                        } ?: Text("No se pudo generar el código QR", color = MaterialTheme.colorScheme.error)
                    }
                }
            


        )
        } else {
            AlertDialog(
                onDismissRequest = { mostrarDialogoConfirmacion = false },
                confirmButton = {
                    TextButton(onClick = {
                        val efectivo = montoRecibido.toDoubleOrNull() ?: 0.0
                        if (metodoPago != "Efectivo" || efectivo >= total) {
                            scope.launch {
                                val venta = Venta(
                                    fecha = fecha,
                                    hora = hora,
                                    metodoPago = metodoPago,
                                    total = total,
                                    empleadoId = selectedEmpleadoId,
                                    clienteId = selectedClienteId
                                )
                                val detalles = productosSeleccionados.map {
                                    val cantidad = cantidades[it.id] ?: 0
                                    val precio = precios[it.id] ?: it.precio
                                    DetalleVenta(
                                        productoId = it.id,
                                        cantidad = cantidad,
                                        precioUnitario = precio,
                                        subtotal = cantidad * precio,
                                        ventaId = 0
                                    )
                                }

                                val ventaId = ventaViewModel.registrarVentaConDetalles(venta, detalles)
                                ventaViewModel.actualizarStockProductos(detalles)
                                mostrarDialogoConfirmacion = false
                                montoRecibido = ""
                                navController.navigate("venta_confirmada/$ventaId")
                            }
                        }
                    }) { Text("Confirmar") }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogoConfirmacion = false }) { Text("Cancelar") }
                },
                title = { Text("Confirmar Venta") },
                text = {
                    Column {
                        Text("Total a pagar: S/. %.2f".format(total), fontWeight = FontWeight.Bold)
                        if (metodoPago == "Efectivo") {
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = montoRecibido,
                                onValueChange = { montoRecibido = it },
                                label = { Text("Monto recibido") },
                                singleLine = true
                            )
                            val vuelto = (montoRecibido.toDoubleOrNull() ?: 0.0) - total
                            if (vuelto >= 0) {
                                Text("Vuelto: S/. %.2f".format(vuelto), color = MaterialTheme.colorScheme.primary)
                            } else {
                                Text("Monto insuficiente", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            )
        }
    }

    // Detalles del producto
    productoDetalle?.let { prod ->
        val imagen = prod.imagenUri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(Uri.parse(it))
                BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }

        AlertDialog(
            onDismissRequest = { productoDetalle = null },
            confirmButton = {
                TextButton(onClick = { productoDetalle = null }) {
                    Text("Cerrar")
                }
            },
            title = { Text("Detalles del Producto", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (imagen != null) {
                        Image(
                            bitmap = imagen,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Sin imagen", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Text("Código: ${prod.clave}")
                    Text("Nombre: ${prod.nombre}")
                    Text("Categoría: ${prod.categoria}")
                    Text("Unidad: ${prod.unidad}")
                    Text("Stock Inicial: ${prod.cantidadInicial}")
                    Text("Stock Mínimo: ${prod.cantidadMinima}")
                    Text("Costo: S/. ${prod.costo}")
                    Text("Precio: S/. ${prod.precio}")
                }
            }
        )
    }
}

fun obtenerSoloFecha(): String {
    val formato = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    return formato.format(Date())
}

fun obtenerSoloHora(): String {
    val formato = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return formato.format(Date())
}
fun generarQR(text: String, size: Int = 512): ImageBitmap? {
    return try {
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bitmap.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}