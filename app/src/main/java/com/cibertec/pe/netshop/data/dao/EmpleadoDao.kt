package com.cibertec.pe.netshop.data.dao

import androidx.room.*
import com.cibertec.pe.netshop.data.entity.Empleado
import kotlinx.coroutines.flow.Flow

@Dao
interface EmpleadoDao {

    @Insert
    suspend fun insertar(empleado: Empleado)

    @Update
    suspend fun actualizar(empleado: Empleado)

    @Delete
    suspend fun eliminar(empleado: Empleado)

    @Query("SELECT * FROM empleados")
    fun obtenerTodos(): Flow<List<Empleado>>
}
