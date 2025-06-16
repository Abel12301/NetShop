package com.cibertec.pe.netshop.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cibertec.pe.netshop.data.database.AppDatabase
import com.cibertec.pe.netshop.data.entity.DetalleVenta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DetalleVentaViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.obtenerInstancia(application).detalleVentaDao()

    private val _detalles = MutableStateFlow<List<DetalleVenta>>(emptyList())
    val detalles: StateFlow<List<DetalleVenta>> = _detalles

    fun insertarDetalle(detalle: DetalleVenta) {
        viewModelScope.launch {
            dao.insertar(detalle)
        }
    }

    fun obtenerPorVenta(ventaId: Int) {
        viewModelScope.launch {
            dao.obtenerPorVentaId(ventaId).collect {
                _detalles.value = it
            }
        }
    }
}