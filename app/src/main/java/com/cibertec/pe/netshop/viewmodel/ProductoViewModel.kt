package com.cibertec.pe.netshop.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cibertec.pe.netshop.data.database.AppDatabase
import com.cibertec.pe.netshop.data.entity.Producto
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductoViewModel(application: Application) : AndroidViewModel(application) {

    private val productoDao = AppDatabase.obtenerInstancia(application).productoDao()

    val productos: StateFlow<List<Producto>> = productoDao.obtenerTodos().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

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
