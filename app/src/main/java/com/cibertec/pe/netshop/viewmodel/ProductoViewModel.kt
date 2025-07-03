package com.cibertec.pe.netshop.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cibertec.pe.netshop.data.database.AppDatabase
import com.cibertec.pe.netshop.data.entity.Producto
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProductoViewModel(application: Application) : AndroidViewModel(application) {

    private val productoDao = AppDatabase.obtenerInstancia(application).productoDao()

    // Lista de productos desde Room
    val productos: StateFlow<List<Producto>> = productoDao.obtenerTodos().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtro de cantidad mínima (estado que se mantiene)
    private val _filtrarCantidadMinima = MutableStateFlow(false)
    val filtrarCantidadMinima: StateFlow<Boolean> = _filtrarCantidadMinima

    // --- Métodos para modificar el estado del filtro ---
    fun toggleCantidadMinima() {
        _filtrarCantidadMinima.value = !_filtrarCantidadMinima.value
    }

    fun setCantidadMinima(valor: Boolean) {
        _filtrarCantidadMinima.value = valor
    }

    // --- Operaciones CRUD ---
    fun insertar(producto: Producto) {
        viewModelScope.launch {
            productoDao.insertar(producto)
        }
    }

    fun actualizar(producto: Producto) {
        viewModelScope.launch {
            productoDao.actualizar(producto)
        }
    }

    fun eliminar(producto: Producto) {
        viewModelScope.launch {
            productoDao.eliminar(producto)
        }
    }

    suspend fun obtenerPorId(id: Int): Producto? {
        return productoDao.obtenerPorId(id)
    }
}
