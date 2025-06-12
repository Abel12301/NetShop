package com.cibertec.pe.netshop.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavDrawerItem(val route: String, val title: String, val icon: ImageVector) {
    object Inventario : NavDrawerItem("inventario", "Inventario", Icons.Default.Inventory2)
    object Ventas : NavDrawerItem("ventas", "Ventas", Icons.Default.PointOfSale)
    object Compras : NavDrawerItem("compras", "Compras", Icons.Default.ShoppingCart)
    object Reportes : NavDrawerItem("reportes", "Reportes", Icons.Default.BarChart)
    object Empleado : NavDrawerItem("empleado", "Empleado", Icons.Default.Person)
    object Clientes : NavDrawerItem("clientes", "Clientes", Icons.Default.People)
    object Proveedores : NavDrawerItem("proveedores", "Proveedores", Icons.Default.LocationCity)

    companion object {
        val items = listOf(
            Inventario, Ventas, Compras, Reportes, Empleado, Clientes, Proveedores
        )
    }
}
