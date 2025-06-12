package com.cibertec.pe.netshop.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cibertec.pe.netshop.ProductosScreen
import com.cibertec.pe.netshop.screens.AgregarProductoScreen
import com.cibertec.pe.netshop.screens.ClienteScreen
import com.cibertec.pe.netshop.screens.EmpleadoScreen
import com.cibertec.pe.netshop.screens.ProveedorScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController, startDestination = NavDrawerItem.Inventario.route) {
        composable(NavDrawerItem.Inventario.route) {
            ProductosScreen(navController) // ✅ se pasa navController
        }
        composable(NavDrawerItem.Ventas.route) { PlaceholderScreen("Ventas") }
        composable(NavDrawerItem.Compras.route) { PlaceholderScreen("Compras") }
        composable(NavDrawerItem.Reportes.route) { PlaceholderScreen("Reportes") }
        composable(NavDrawerItem.Empleado.route) { PlaceholderScreen("Empleado") }
        composable(NavDrawerItem.Clientes.route) { PlaceholderScreen("Clientes") }
        composable(NavDrawerItem.Proveedores.route) { PlaceholderScreen("Proveedores") }

        composable("agregar_producto") {
            AgregarProductoScreen(navController) // ✅ se pasa navController
        }
        composable(NavDrawerItem.Empleado.route) {
            EmpleadoScreen(navController) // ✅ CORRECTO
        }
        composable(NavDrawerItem.Clientes.route) {
            ClienteScreen(navController) // muestra la pantalla de clientes
        }
        composable(NavDrawerItem.Proveedores.route) {
            ProveedorScreen(navController)
        }

    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Text("Pantalla de $title", modifier = Modifier.padding(16.dp))
}
