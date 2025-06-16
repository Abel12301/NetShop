package com.cibertec.pe.netshop.data.dao

import androidx.room.*
import com.cibertec.pe.netshop.data.entity.DetalleVenta
import com.cibertec.pe.netshop.data.relations.DetalleVentaConProducto
import kotlinx.coroutines.flow.Flow

@Dao
interface DetalleVentaDao {

    @Insert
    suspend fun insertar(detalle: DetalleVenta)

    @Update
    suspend fun actualizar(detalle: DetalleVenta)

    @Delete
    suspend fun eliminar(detalle: DetalleVenta)

    @Query("SELECT * FROM detalle_ventas WHERE ventaId = :ventaId")
    fun obtenerPorVentaId(ventaId: Int): Flow<List<DetalleVenta>>

    @Query("SELECT * FROM detalle_ventas WHERE ventaId = :ventaId")
    suspend fun obtenerDetallePorVenta(ventaId: Int): List<DetalleVenta>


    @Query("DELETE FROM detalle_ventas WHERE ventaId = :ventaId")
    suspend fun eliminarPorVentaId(ventaId: Int)

    // ✅ NUEVO método con relación:
    @Transaction
    @Query("SELECT * FROM detalle_ventas WHERE ventaId = :ventaId")
    suspend fun obtenerDetalleConProducto(ventaId: Int): List<DetalleVentaConProducto>
}
