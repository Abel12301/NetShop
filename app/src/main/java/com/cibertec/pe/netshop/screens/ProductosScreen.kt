package com.cibertec.pe.netshop

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.cibertec.pe.netshop.data.entity.Producto
import com.cibertec.pe.netshop.viewmodel.ProductoViewModel

@Composable
fun ProductosScreen(
    navController: NavHostController,
    viewModel: ProductoViewModel = viewModel()
) {
    var buscadorActivo by remember { mutableStateOf(false) }
    var textoBusqueda by remember { mutableStateOf("") }

    val productos by viewModel.productos.collectAsState()
    val productosFiltrados = if (buscadorActivo && textoBusqueda.isNotBlank()) {
        productos.filter {
            it.nombre.contains(textoBusqueda, ignoreCase = true) ||
                    it.clave.contains(textoBusqueda, ignoreCase = true)
        }
    } else {
        productos
    }

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
                },
                onTextChange = { textoBusqueda = it }
            )
            FilterRow()

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
fun FilterRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FilterChip("Merma")
        FilterChip("Cantidad Mínima")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChip(text: String) {
    AssistChip(
        onClick = { /* Acción de filtro */ },
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
                    Text(text = "#${producto.cantidadInicial}", color = Color.Gray)
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
