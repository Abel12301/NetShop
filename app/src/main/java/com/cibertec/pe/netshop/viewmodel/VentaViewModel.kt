package com.cibertec.pe.netshop.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cibertec.pe.netshop.data.database.AppDatabase
import com.cibertec.pe.netshop.data.entity.DetalleParaPdf
import com.cibertec.pe.netshop.data.entity.DetalleVenta
import com.cibertec.pe.netshop.data.entity.Producto
import com.cibertec.pe.netshop.data.entity.ProductoReporte
import com.cibertec.pe.netshop.data.entity.Venta
import com.cibertec.pe.netshop.utils.PdfGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.File

class VentaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.obtenerInstancia(application)
    private val ventaDao = db.ventaDao()
    private val detalleDao = db.detalleVentaDao()
    private val productoDao = db.productoDao()

    private val _productos = mutableStateListOf<Producto>()
    val productosDisponibles: List<Producto> get() = _productos

    init {
        cargarProductos()
    }

    fun cargarProductos() {
        viewModelScope.launch {
            val lista = productoDao.obtenerTodosSuspend()
            _productos.clear()
            _productos.addAll(lista)
        }
    }

    val ventas: Flow<List<Venta>> = ventaDao.obtenerTodas()

    fun actualizarVenta(venta: Venta) = viewModelScope.launch {
        ventaDao.actualizar(venta)
    }

    suspend fun obtenerVentaPorIdSuspend(id: Int): Venta? {
        return ventaDao.obtenerPorId(id)
    }
    suspend fun obtenerTodosLosDetalles(): List<DetalleVenta> {
        return detalleDao.obtenerTodosLosDetalles() // esta función la agregaremos en el DAO
    }

    suspend fun obtenerResumenVentasPorProducto(): List<ProductoReporte> {
        val detalles = obtenerTodosLosDetalles()
        val productos = productosDisponibles

        return detalles.groupBy { it.productoId }.mapNotNull { (productoId, lista) ->
            val producto = productos.find { it.id == productoId }
            producto?.let {
                val cantidad = lista.sumOf { it.cantidad }
                val total = lista.sumOf { it.subtotal }
                ProductoReporte(it.nombre, cantidad, total)
            }
        }
    }
    fun actualizarVentaConDetalles(venta: Venta, nuevosDetalles: List<DetalleVenta>) {
        viewModelScope.launch {
            ventaDao.actualizar(venta)
            detalleDao.eliminarPorVentaId(venta.id)
            nuevosDetalles.forEach {
                val detalleConVentaId = it.copy(ventaId = venta.id)
                detalleDao.insertar(detalleConVentaId)
            }
        }
    }

    suspend fun obtenerDetallesDeVenta(ventaId: Int): List<DetalleVenta> {
        return detalleDao.obtenerDetallePorVenta(ventaId)
    }
    suspend fun obtenerDetallesParaPdf(ventaId: Int): List<DetalleParaPdf> {
        val detalles = detalleDao.obtenerDetalleConProducto(ventaId)
        return detalles.map {
            DetalleParaPdf(
                productoNombre = it.producto.nombre,
                cantidad = it.detalle.cantidad,
                precioUnitario = it.detalle.precioUnitario,
                subtotal = it.detalle.subtotal
            )
        }
    }
    fun actualizarStockProductos(detalles: List<DetalleVenta>) {
        viewModelScope.launch {
            detalles.forEach { detalle ->
                val producto = productoDao.obtenerPorId(detalle.productoId)
                if (producto != null) {
                    val nuevoStock = producto.cantidadInicial - detalle.cantidad
                    productoDao.actualizarStock(detalle.productoId, nuevoStock)
                }
            }
        }
    }
    suspend fun obtenerProductoPorIdSuspend(id: Int): Producto? {
        return productoDao.obtenerPorId(id)
    }

    suspend fun registrarVentaConDetalles(venta: Venta, detalles: List<DetalleVenta>): Int {
        val ventaId = ventaDao.insertar(venta).toInt()
        detalles.forEach {
            val detalleConVentaId = it.copy(ventaId = ventaId)
            detalleDao.insertar(detalleConVentaId)
        }
        return ventaId
    }

    fun generarPdfVenta(context: Application, ventaId: Int, onPdfGenerado: (File?) -> Unit) {
        viewModelScope.launch {
            val venta = ventaDao.obtenerPorId(ventaId)
            val detalles = obtenerDetallesParaPdf(ventaId)

            if (venta != null && detalles.isNotEmpty()) {
                val file = PdfGenerator.generateTicket(context, venta, detalles)
                onPdfGenerado(file)
            } else {
                onPdfGenerado(null)
            }
        }
    }}