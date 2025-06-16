package com.cibertec.pe.netshop.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "proveedores")
data class Proveedor(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val empresa: String,
    val telefono: String,
    val correo: String,
    val direccion: String,
    val infoAdicional: String
)