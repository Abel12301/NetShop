package com.cibertec.pe.netshop.repository

import com.cibertec.pe.netshop.data.dao.ProveedorDao
import com.cibertec.pe.netshop.data.entity.Proveedor

class ProveedorRepository(private val dao: ProveedorDao) {

    fun obtenerTodos() = dao.obtenerTodos()

    suspend fun insertar(proveedor: Proveedor) = dao.insertar(proveedor)

    suspend fun actualizar(proveedor: Proveedor) = dao.actualizar(proveedor)

    suspend fun eliminar(proveedor: Proveedor) = dao.eliminar(proveedor)
}
