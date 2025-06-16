package com.cibertec.pe.netshop.data.entity

data class DetalleParaPdf(
    val productoNombre: String,
    val cantidad: Int,
    val precioUnitario: Double,
    val subtotal: Double
)