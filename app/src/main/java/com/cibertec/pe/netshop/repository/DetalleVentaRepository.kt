package com.cibertec.pe.netshop.repository

import com.cibertec.pe.netshop.data.dao.DetalleVentaDao
import com.cibertec.pe.netshop.data.entity.DetalleVenta
import kotlinx.coroutines.flow.Flow

class DetalleVentaRepository(private val dao: DetalleVentaDao) {

    suspend fun insertar(detalle: DetalleVenta) {
        dao.insertar(detalle)
    }

    fun obtenerPorVentaId(ventaId: Int): Flow<List<DetalleVenta>> {
        return dao.obtenerPorVentaId(ventaId)
    }
}
