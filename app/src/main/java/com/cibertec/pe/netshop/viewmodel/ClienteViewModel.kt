package com.cibertec.pe.netshop.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cibertec.pe.netshop.data.database.AppDatabase
import com.cibertec.pe.netshop.data.entity.Cliente
import com.cibertec.pe.netshop.repository.ClienteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ClienteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ClienteRepository

    val clientes = AppDatabase.obtenerInstancia(application)
        .clienteDao()
        .obtenerTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    init {
        val dao = AppDatabase.obtenerInstancia(application).clienteDao()
        repository = ClienteRepository(dao)
    }

    fun insertar(cliente: Cliente) {
        viewModelScope.launch {
            repository.insertar(cliente)
        }
    }

    fun actualizar(cliente: Cliente) {
        viewModelScope.launch {
            repository.actualizar(cliente)
        }
    }

    fun eliminar(cliente: Cliente) {
        viewModelScope.launch {
            repository.eliminar(cliente)
        }
    }
}