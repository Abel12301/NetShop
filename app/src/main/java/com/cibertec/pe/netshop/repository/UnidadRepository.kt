package com.cibertec.pe.netshop.repository

import com.cibertec.pe.netshop.data.dao.UnidadDao
import com.cibertec.pe.netshop.data.entity.Unidad

class UnidadRepository(private val dao: UnidadDao) {
    fun obtenerTodas() = dao.obtenerTodas()
    suspend fun insertar(unidad: Unidad) = dao.insertar(unidad)
    suspend fun actualizar(unidad: Unidad) = dao.actualizar(unidad)
    suspend fun eliminar(unidad: Unidad) = dao.eliminar(unidad)
    suspend fun obtenerPorId(id: Int) = dao.obtenerPorId(id)
}
