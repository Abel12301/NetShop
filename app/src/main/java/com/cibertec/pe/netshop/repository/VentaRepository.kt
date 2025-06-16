package com.cibertec.pe.netshop.repository

import com.cibertec.pe.netshop.data.dao.VentaDao
import com.cibertec.pe.netshop.data.entity.Venta
import kotlinx.coroutines.flow.Flow

class VentaRepository(private val dao: VentaDao) {

    suspend fun insertar(venta: Venta): Long {
        return dao.insertar(venta)
    }

    fun obtenerTodas(): Flow<List<Venta>> {
        return dao.obtenerTodas()
    }
}