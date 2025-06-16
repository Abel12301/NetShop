package com.cibertec.pe.netshop.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cibertec.pe.netshop.data.database.AppDatabase
import com.cibertec.pe.netshop.data.entity.Proveedor
import com.cibertec.pe.netshop.repository.ProveedorRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProveedorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ProveedorRepository

    val proveedores = AppDatabase.obtenerInstancia(application)
        .proveedorDao()
        .obtenerTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    init {
        val dao = AppDatabase.obtenerInstancia(application).proveedorDao()
        repository = ProveedorRepository(dao)
    }

    fun insertar(proveedor: Proveedor) {
        viewModelScope.launch {
            repository.insertar(proveedor)
        }
    }

    fun actualizar(proveedor: Proveedor) {
        viewModelScope.launch {
            repository.actualizar(proveedor)
        }
    }

    fun eliminar(proveedor: Proveedor) {
        viewModelScope.launch {
            repository.eliminar(proveedor)
        }
    }
}
