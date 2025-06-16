package com.cibertec.pe.netshop.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "productos")
data class Producto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val costo: Double,
    val precio: Double,
    val clave: String,
    val cantidadInicial: Int,
    val cantidadMinima: Int,
    val unidad: String,
    val categoria: String,
    val infoAdicional: String,
    val imagenUri: String? = null // ✅ IMPORTANTE
)
