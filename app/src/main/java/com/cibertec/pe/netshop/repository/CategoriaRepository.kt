package com.cibertec.pe.netshop.repository

import com.cibertec.pe.netshop.data.dao.CategoriaDao
import com.cibertec.pe.netshop.data.entity.Categoria

class CategoriaRepository(private val dao: CategoriaDao) {
    fun obtenerTodas() = dao.obtenerTodas()
    suspend fun insertar(categoria: Categoria) = dao.insertar(categoria)
    suspend fun actualizar(categoria: Categoria) = dao.actualizar(categoria)
    suspend fun eliminar(categoria: Categoria) = dao.eliminar(categoria)
    suspend fun obtenerPorId(id: Int) = dao.obtenerPorId(id)
}


