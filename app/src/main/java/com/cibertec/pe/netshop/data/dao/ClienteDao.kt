package com.cibertec.pe.netshop.data.dao

import androidx.room.*
import com.cibertec.pe.netshop.data.entity.Cliente
import kotlinx.coroutines.flow.Flow

@Dao
interface ClienteDao {
    @Insert
    suspend fun insertar(cliente: Cliente)

    @Update
    suspend fun actualizar(cliente: Cliente)

    @Delete
    suspend fun eliminar(cliente: Cliente)

    @Query("SELECT * FROM clientes")
    fun obtenerTodos(): Flow<List<Cliente>>
}
