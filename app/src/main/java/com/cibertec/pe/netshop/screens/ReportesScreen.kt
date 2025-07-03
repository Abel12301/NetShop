package com.cibertec.pe.netshop.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.cibertec.pe.netshop.data.entity.ProductoReporte
import com.cibertec.pe.netshop.viewmodel.VentaViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesScreen(navController: NavHostController) {
    val context = LocalContext.current.applicationContext as Application
    val ventaViewModel = remember { VentaViewModel(context) }
    val scope = rememberCoroutineScope()

    var resumenVentas by remember { mutableStateOf<List<ProductoReporte>>(emptyList()) }
    var totalVentas by remember { mutableStateOf(0) }
    var totalProductosVendidos by remember { mutableStateOf(0) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val resumen = ventaViewModel.obtenerResumenVentasPorProducto()
        resumenVentas = resumen
        totalVentas = resumen.size
        totalProductosVendidos = resumen.sumOf { it.cantidadVendida }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes de Ventas") },
                navigationIcon = {
                    IconButton(onClick = { /* Navegación o menú */ }) {
                        Icon(Icons.Default.BarChart, contentDescription = "Reportes")
                    }
                },
                actions = {
                    IconButton(onClick = { mostrarConfirmacion = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar todo", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "📊 Resumen General",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 2.dp,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🔢 Productos vendidos: $totalProductosVendidos", fontSize = 16.sp)
                    Text("🧾 Ventas realizadas: $totalVentas", fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "📦 Detalle por Producto",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (resumenVentas.isEmpty()) {
                Text(
                    text = "No hay productos vendidos.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(resumenVentas) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            elevation = CardDefaults.cardElevation(3.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("🧾 Producto: ${item.nombre}", fontWeight = FontWeight.Medium)
                                Text("🛒 Vendidos: ${item.cantidadVendida}")
                                Text("💰 Total: S/. ${"%.2f".format(item.totalMonto)}")
                            }
                        }
                    }
                }
            }
        }

        // Confirmación para eliminar
        if (mostrarConfirmacion) {
            AlertDialog(
                onDismissRequest = { mostrarConfirmacion = false },
                title = { Text("¿Eliminar todos los datos?") },
                text = {
                    Text("Esta acción eliminará todas las ventas registradas. ¿Deseas continuar?")
                },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {


                            resumenVentas = emptyList()
                            totalVentas = 0
                            totalProductosVendidos = 0
                            mostrarConfirmacion = false
                        }
                    }) {
                        Text("Sí, eliminar", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarConfirmacion = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
