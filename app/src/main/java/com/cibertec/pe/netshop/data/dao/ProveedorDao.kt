package com.cibertec.pe.netshop.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.cibertec.pe.netshop.data.entity.Proveedor
import kotlinx.coroutines.flow.Flow

@Dao
interface ProveedorDao {

    @Insert
    suspend fun insertar(proveedor: Proveedor)

    @Update
    suspend fun actualizar(proveedor: Proveedor)

    @Delete
    suspend fun eliminar(proveedor: Proveedor)

    @Query("SELECT * FROM proveedores")
    fun obtenerTodos(): Flow<List<Proveedor>>
}