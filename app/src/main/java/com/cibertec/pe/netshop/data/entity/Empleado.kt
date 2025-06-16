package com.cibertec.pe.netshop.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "empleados")
data class Empleado(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val puesto: String,
    val salario: String,
    val telefono: String,
    val correo: String,
    val direccion: String,
    val infoAdicional: String
)