package com.cibertec.pe.netshop.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.cibertec.pe.netshop.ProductosScreen
import com.cibertec.pe.netshop.data.entity.Producto
import com.cibertec.pe.netshop.screens.*
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson

import java.net.URLDecoder

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController, startDestination = NavDrawerItem.Inventario.route) {
        composable(NavDrawerItem.Inventario.route) {
            ProductosScreen(navController)
        }
        composable(NavDrawerItem.Ventas.route) {
            VentasScreen(navController)
        }
        composable(NavDrawerItem.Compras.route) {
            PlaceholderScreen("Compras")
        }
        composable(NavDrawerItem.Reportes.route) {
            PlaceholderScreen("Reportes")
        }
        composable(NavDrawerItem.Empleado.route) {
            EmpleadoScreen(navController)
        }
        composable(NavDrawerItem.Clientes.route) {
            ClienteScreen(navController)
        }
        composable(NavDrawerItem.Proveedores.route) {
            ProveedorScreen(navController)
        }
        composable("agregar_producto") {
            AgregarProductoScreen(navController = navController, productoId = null)
        }
        composable(
            "agregar_producto/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val productoId = backStackEntry.arguments?.getInt("id")
            AgregarProductoScreen(navController = navController, productoId = productoId)
        }
        composable("agregar_venta") {
            AgregarVentaFlowScreen(navController)
        }
        composable("editar_venta/{ventaId}") { backStackEntry ->
            val ventaId = backStackEntry.arguments?.getString("ventaId")?.toIntOrNull() ?: return@composable
            EditarVentaCompletaScreen(navController, ventaId)

        }
        composable(
            "venta_confirmada/{ventaId}",
            arguments = listOf(navArgument("ventaId") { type = NavType.IntType })
        ) { backStackEntry ->
            val ventaId = backStackEntry.arguments?.getInt("ventaId")
            PantallaConfirmacion(navController, ventaId)
        }

        composable(
            "ver_pdf/{ventaId}",
            arguments = listOf(navArgument("ventaId") { type = NavType.IntType })
        ) { backStackEntry ->
            val ventaId = backStackEntry.arguments?.getInt("ventaId")
            if (ventaId != null) {
                VerPdfScreen(navController = navController, ventaId = ventaId)
            }
        }
        composable("reportes") {
            ReportesScreen(navController)
        }
        composable("exportar_qr") {
            ExportarQRScreen(
                onBack = { navController.popBackStack() }
            )
        }


        composable("confirmar_venta/{productoJson}", arguments = listOf(
            navArgument("productoJson") { type = NavType.StringType }
        )) { backStackEntry ->
            val productoJson = backStackEntry.arguments?.getString("productoJson") ?: ""
            val producto = Gson().fromJson(
                URLDecoder.decode(productoJson, "UTF-8"),
                object : com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken<Producto>() {}.type
            ) as Producto

            ConfirmarVentaScreen(
                navController = navController,
                productosSeleccionadosInicial = listOf(producto),
                cantidadesInicial = mapOf(producto.id to 1)
            )
        }
    }

}

@Composable
fun PlaceholderScreen(title: String) {
    Text("Pantalla de $title", modifier = Modifier.padding(16.dp))
}
