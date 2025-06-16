package com.cibertec.pe.netshop.data.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.cibertec.pe.netshop.data.entity.DetalleVenta
import com.cibertec.pe.netshop.data.entity.Producto

data class DetalleVentaConProducto(
    @Embedded val detalle: DetalleVenta,

    @Relation(
        parentColumn = "productoId",
        entityColumn = "id"
    )
    val producto: Producto
)
