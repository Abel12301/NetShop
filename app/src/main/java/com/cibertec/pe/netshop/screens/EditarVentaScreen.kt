package com.cibertec.pe.netshop.screens

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.launch

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarVentaCompletaScreen(navController: NavHostController, ventaId: Int) {
    val context = LocalContext.current.applicationContext as Application
    val ventaViewModel = remember { VentaViewModel(context) }
    val scope = rememberCoroutineScope()

    var venta by remember { mutableStateOf<Venta?>(null) }
    var detalles by remember { mutableStateOf<List<DetalleVenta>>(emptyList()) }
    var productosState by remember { mutableStateOf(listOf<Pair<DetalleVenta, Double>>()) }
    val todosLosProductos = remember { ventaViewModel.productosDisponibles }
    var productoDetalle by remember { mutableStateOf<Producto?>(null) }

    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(Unit) {
        venta = ventaViewModel.obtenerVentaPorIdSuspend(ventaId)
        detalles = ventaViewModel.obtenerDetallesDeVenta(ventaId)
        productosState = detalles.map { it to it.precioUnitario }
    }

    if (venta == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        var metodoPago by remember { mutableStateOf(venta!!.metodoPago) }
        var clienteId by remember { mutableStateOf(venta!!.clienteId ?: 0) }
        var empleadoId by remember { mutableStateOf(venta!!.empleadoId ?: 0) }

        val total by derivedStateOf {
            productosState.sumOf { (detalle, precio) -> detalle.cantidad * precio }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Editar Venta N°${venta!!.id}", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colorScheme.primary,
                        titleContentColor = colorScheme.onPrimary
                    )
                )
            },
            containerColor = colorScheme.background
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("🛒 Productos en la venta", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = colorScheme.onBackground)

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    itemsIndexed(productosState) { index, (detalle, precio) ->
                        val producto = todosLosProductos.find { it.id == detalle.productoId }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                            elevation = CardDefaults.cardElevation(6.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val imagenBitmap = remember(producto?.imagenUri) {
                                            producto?.imagenUri?.let {
                                                try {
                                                    val inputStream = context.contentResolver.openInputStream(Uri.parse(it))
                                                    BitmapFactory.decodeStream(inputStream)?.asImageBitmap()
                                                } catch (e: Exception) {
                                                    null
                                                }
                                            }
                                        }

                                        if (imagenBitmap != null) {
                                            Image(
                                                bitmap = imagenBitmap,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .background(colorScheme.secondaryContainer, RoundedCornerShape(6.dp))
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .background(Color.Gray, RoundedCornerShape(6.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("IMG", color = colorScheme.onSurface)
                                            }
                                        }

                                        Spacer(Modifier.width(12.dp))

                                        Column {
                                            Text(producto?.nombre ?: "Nombre", fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                                            Text(producto?.categoria ?: "Descripción", color = colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    Row {
                                        IconButton(onClick = { productoDetalle = producto }) {
                                            Icon(Icons.Default.Info, contentDescription = null, tint = colorScheme.primary)
                                        }
                                        IconButton(onClick = {
                                            productosState = productosState.toMutableList().apply {
                                                removeAt(index)
                                            }
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = null, tint = colorScheme.error)
                                        }
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Button(onClick = {
                                            if (detalle.cantidad > 1) {
                                                productosState = productosState.toMutableList().apply {
                                                    this[index] = this[index].copy(first = detalle.copy(cantidad = detalle.cantidad - 1))
                                                }
                                            }
                                        }) { Text("-") }
                                        Text(
                                            text = detalle.cantidad.toString(),
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.onSurface
                                        )
                                        Button(onClick = {
                                            productosState = productosState.toMutableList().apply {
                                                this[index] = this[index].copy(first = detalle.copy(cantidad = detalle.cantidad + 1))
                                            }
                                        }) { Text("+") }
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    OutlinedTextField(
                                        value = "%.2f".format(precio),
                                        onValueChange = {
                                            val nuevo = it.replace(",", ".").toDoubleOrNull() ?: precio
                                            productosState = productosState.toMutableList().apply {
                                                this[index] = this[index].copy(second = nuevo)
                                            }
                                        },
                                        label = { Text("Precio") },
                                        modifier = Modifier.width(100.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = colorScheme.primary,
                                            unfocusedBorderColor = colorScheme.outline,
                                            focusedTextColor = colorScheme.onSurface,
                                            unfocusedTextColor = colorScheme.onSurface
                                        )
                                    )
                                }

                                Text(
                                    text = "Total: S/. ${"%.2f".format(detalle.cantidad * precio)}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.primary,
                                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 12.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                    }
                }

                Text("💳 Método de Pago", fontWeight = FontWeight.SemiBold, color = colorScheme.onBackground)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("Efectivo", "Yape", "Plin").forEach {
                        FilterChip(
                            selected = metodoPago == it,
                            onClick = { metodoPago = it },
                            label = { Text(it) }
                        )
                    }
                }

                OutlinedTextField(
                    value = clienteId.toString(),
                    onValueChange = { clienteId = it.toIntOrNull() ?: clienteId },
                    label = { Text("ID del Cliente") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = empleadoId.toString(),
                    onValueChange = { empleadoId = it.toIntOrNull() ?: empleadoId },
                    label = { Text("ID del Empleado") },
                    modifier = Modifier.fillMaxWidth()
                )

                Divider()
                Text("💵 Total: S/. ${"%.2f".format(total)}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colorScheme.primary)

                Button(
                    onClick = {
                        val nuevosDetalles = productosState.map { (detalle, precio) ->
                            detalle.copy(precioUnitario = precio, subtotal = detalle.cantidad * precio)
                        }
                        val ventaActualizada = venta!!.copy(
                            metodoPago = metodoPago,
                            clienteId = clienteId,
                            empleadoId = empleadoId,
                            total = total
                        )
                        scope.launch {
                            ventaViewModel.actualizarVentaConDetalles(ventaActualizada, nuevosDetalles)
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = colorScheme.onPrimary)
                    Spacer(Modifier.width(8.dp))
                    Text("Guardar Cambios", color = colorScheme.onPrimary)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            productoDetalle?.let { producto ->
                val imagen = producto.imagenUri?.let {
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
                                Image(bitmap = imagen, contentDescription = null, modifier = Modifier.height(150.dp).fillMaxWidth())
                            }
                            Text("Código: ${producto.clave}")
                            Text("Nombre: ${producto.nombre}")
                            Text("Categoría: ${producto.categoria}")
                            Text("Unidad: ${producto.unidad}")
                            Text("Stock Inicial: ${producto.cantidadInicial}")
                            Text("Stock Mínimo: ${producto.cantidadMinima}")
                            Text("Costo: S/. ${producto.costo}")
                            Text("Precio: S/. ${producto.precio}")
                        }
                    }
                )
            }
        }
    }
}
