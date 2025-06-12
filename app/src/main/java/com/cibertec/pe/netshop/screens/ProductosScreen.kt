package com.cibertec.pe.netshop

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun ProductosScreen(navController: NavHostController)
 {
    Scaffold(
        bottomBar = { BottomNavBar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("agregar_producto")
                },
                containerColor = Color(0xFFD9B96A)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        },
        containerColor = Color(0xFFE5E5E5)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            FilterRow()
            ProductCard()
        }
    }
}

@Composable
fun FilterRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        FilterChip("Merma")
        FilterChip("Cantidad Mínima")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChip(text: String) {
    AssistChip(
        onClick = { },
        label = { Text(text) },
        leadingIcon = {
            if (text == "Merma")
                Icon(Icons.Default.Close, contentDescription = null)
            else
                Icon(Icons.Default.Warning, contentDescription = null)
        }
    )
}

@Composable
fun ProductCard() {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Sin Clave", fontSize = 12.sp, color = Color.Gray)
                Text("UNIDAD", fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("📦 GW", fontWeight = FontWeight.Bold)
                Text("#2.00", fontWeight = FontWeight.Bold)
                Text("$5.00", fontWeight = FontWeight.Bold, color = Color(0xFF1D2951))
            }

        }
    }
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
