package com.cibertec.pe.netshop.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "detalle_ventas")
data class DetalleVenta(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ventaId: Int,
    val productoId: Int,
    val cantidad: Int,
    val precioUnitario: Double,
    val subtotal: Double
)
