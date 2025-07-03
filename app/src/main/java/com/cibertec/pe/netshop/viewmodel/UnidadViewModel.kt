package com.cibertec.pe.netshop.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cibertec.pe.netshop.data.database.AppDatabase
import com.cibertec.pe.netshop.data.entity.Unidad
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UnidadViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.obtenerInstancia(application).unidadDao()

    val unidades: StateFlow<List<Unidad>> = dao.obtenerTodas().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun insertar(unidad: Unidad) = viewModelScope.launch { dao.insertar(unidad) }
    fun actualizar(unidad: Unidad) = viewModelScope.launch { dao.actualizar(unidad) }
    fun eliminar(unidad: Unidad) = viewModelScope.launch { dao.eliminar(unidad) }
    suspend fun obtenerPorId(id: Int): Unidad? = dao.obtenerPorId(id)
}
