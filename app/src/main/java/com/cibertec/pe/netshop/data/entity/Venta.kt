package com.cibertec.pe.netshop.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "ventas")
data class Venta(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fecha: String,
    val hora: String,
    val metodoPago: String,
    val total: Double,
    val empleadoId: Int?, // puede ser null si no se selecciona
    val clienteId: Int?   // puede ser null si no se selecciona
)