package com.cibertec.pe.netshop.data.dao

import androidx.room.*
import com.cibertec.pe.netshop.data.entity.Producto
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {

    @Insert
    suspend fun insertar(producto: Producto)

    @Update
    suspend fun actualizar(producto: Producto)

    @Delete
    suspend fun eliminar(producto: Producto)

    @Query("SELECT * FROM productos")
    fun obtenerTodos(): Flow<List<Producto>>

    @Query("SELECT * FROM productos WHERE id = :id")
    suspend fun obtenerPorId(id: Int): Producto?

    @Query("SELECT * FROM productos")
    fun obtenerTodosSinFlow(): List<Producto>

    @Query("SELECT * FROM productos")
    suspend fun obtenerTodosSuspend(): List<Producto>
}
