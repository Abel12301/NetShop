package com.cibertec.pe.netshop.screens

import android.app.Application

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.cibertec.pe.netshop.viewmodel.VentaViewModel
import com.cibertec.pe.netshop.data.entity.Venta
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VentasScreen(navController: NavHostController) {
    val context = LocalContext.current.applicationContext as Application
    val ventaViewModel = remember { VentaViewModel(context) }
    val ventas by remember { ventaViewModel.ventas }.collectAsState(initial = emptyList())

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("agregar_venta") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Venta")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)

        ) {
            Text(
                text = "Historial De Ventas",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (ventas.isEmpty()) {
                Text(
                    text = "No hay ventas registradas.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(ventas) { venta ->
                        VentaCard(venta = venta, navController = navController)
                    }
                }
            }
        }
    }
}

@Composable
fun VentaCard(venta: Venta, navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("editar_venta/${venta.id}") },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Venta N°: ${venta.id}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Fecha: ${venta.fecha} ${venta.hora}",
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Método de pago: ${venta.metodoPago}",
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Total: ${formatoMoneda(venta.total)}",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

fun formatoMoneda(valor: Double): String {
    return NumberFormat.getCurrencyInstance(Locale("es", "PE")).format(valor)
}
