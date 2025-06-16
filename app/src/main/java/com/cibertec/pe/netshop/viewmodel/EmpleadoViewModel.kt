package com.cibertec.pe.netshop.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cibertec.pe.netshop.data.database.AppDatabase
import com.cibertec.pe.netshop.data.entity.Empleado
import com.cibertec.pe.netshop.repository.EmpleadoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EmpleadoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EmpleadoRepository

    val empleados = AppDatabase.obtenerInstancia(application)
        .empleadoDao()
        .obtenerTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    init {
        val dao = AppDatabase.obtenerInstancia(application).empleadoDao()
        repository = EmpleadoRepository(dao)
    }

    fun insertar(empleado: Empleado) {
        viewModelScope.launch {
            repository.insertar(empleado)
        }
    }

    fun actualizar(empleado: Empleado) {
        viewModelScope.launch {
            repository.actualizar(empleado)
        }
    }

    fun eliminar(empleado: Empleado) {
        viewModelScope.launch {
            repository.eliminar(empleado)
        }
    }
}
