package com.cibertec.pe.netshop.screens

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import com.cibertec.pe.netshop.data.entity.Producto

@Composable
fun AgregarVentaFlowScreen(navController: NavHostController) {
    val productosSeleccionados = remember { mutableStateListOf<Producto>() }
    val cantidades = remember { mutableStateMapOf<Int, Int>() }
    var pantallaActual by remember { mutableStateOf("buscar") }

    when (pantallaActual) {
        "buscar" -> {
            AgregarVentaScreen(
                navController = navController,
                onProductoSeleccionado = { producto ->
                    val yaExiste = productosSeleccionados.any { it.id == producto.id }

                    if (yaExiste) {
                        val cantidadActual = cantidades[producto.id] ?: 0
                        cantidades[producto.id] = cantidadActual + 1
                    } else {
                        productosSeleccionados.add(producto)
                        cantidades[producto.id] = 1
                    }

                    pantallaActual = "confirmar"
                }
            )
        }

        "confirmar" -> {
            ConfirmarVentaScreen(
                navController = navController,
                productosSeleccionadosInicial = productosSeleccionados,
                cantidadesInicial = cantidades,
                onAgregarMasProductos = {
                    pantallaActual = "buscar"
                }
            )
        }
    }
}