package com.cibertec.pe.netshop.repository

import com.cibertec.pe.netshop.data.dao.ClienteDao
import com.cibertec.pe.netshop.data.entity.Cliente

class ClienteRepository(private val dao: ClienteDao) {
    fun obtenerTodos() = dao.obtenerTodos()
    suspend fun insertar(cliente: Cliente) = dao.insertar(cliente)
    suspend fun actualizar(cliente: Cliente) = dao.actualizar(cliente)
    suspend fun eliminar(cliente: Cliente) = dao.eliminar(cliente)
}
