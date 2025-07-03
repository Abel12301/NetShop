package com.cibertec.pe.netshop.data.dao

import androidx.room.*
import com.cibertec.pe.netshop.data.entity.Unidad
import kotlinx.coroutines.flow.Flow

@Dao
interface UnidadDao {

    @Query("SELECT * FROM unidades ORDER BY nombre ASC")
    fun obtenerTodas(): Flow<List<Unidad>>


    @Query("SELECT * FROM unidades ORDER BY nombre ASC")
    suspend fun obtenerTodos(): List<Unidad>

    @Insert
    suspend fun insertar(unidad: Unidad)

    @Update
    suspend fun actualizar(unidad: Unidad)

    @Delete
    suspend fun eliminar(unidad: Unidad)

    @Query("SELECT * FROM unidades WHERE id = :id")
    suspend fun obtenerPorId(id: Int): Unidad?
}

