package com.cibertec.pe.netshop.repository

import com.cibertec.pe.netshop.data.dao.EmpleadoDao
import com.cibertec.pe.netshop.data.entity.Empleado

class EmpleadoRepository(private val dao: EmpleadoDao) {

    suspend fun insertar(empleado: Empleado) = dao.insertar(empleado)

    suspend fun actualizar(empleado: Empleado) = dao.actualizar(empleado)

    suspend fun eliminar(empleado: Empleado) = dao.eliminar(empleado)

    fun obtenerTodos() = dao.obtenerTodos()
}
