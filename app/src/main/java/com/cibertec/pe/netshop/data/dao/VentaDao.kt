package com.cibertec.pe.netshop.data.dao

import androidx.room.*
import com.cibertec.pe.netshop.data.entity.Venta
import kotlinx.coroutines.flow.Flow

@Dao
interface VentaDao {

    @Insert
    suspend fun insertar(venta: Venta): Long  // retorna el ID de la venta insertada

    @Update
    suspend fun actualizar(venta: Venta)

    @Delete
    suspend fun eliminar(venta: Venta)

    @Query("SELECT * FROM ventas ORDER BY id DESC")
    fun obtenerTodas(): Flow<List<Venta>>

    @Query("SELECT * FROM ventas WHERE id = :id")
    suspend fun obtenerPorId(id: Int): Venta?


}
