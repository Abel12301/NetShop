package com.cibertec.pe.netshop.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cibertec.pe.netshop.data.database.AppDatabase
import com.cibertec.pe.netshop.data.entity.Categoria
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoriaViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.obtenerInstancia(application).categoriaDao()

    val categorias: StateFlow<List<Categoria>> = dao.obtenerTodas().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun insertar(categoria: Categoria) = viewModelScope.launch { dao.insertar(categoria) }
    fun actualizar(categoria: Categoria) = viewModelScope.launch { dao.actualizar(categoria) }
    fun eliminar(categoria: Categoria) = viewModelScope.launch { dao.eliminar(categoria) }
    suspend fun obtenerPorId(id: Int): Categoria? = dao.obtenerPorId(id)
}
