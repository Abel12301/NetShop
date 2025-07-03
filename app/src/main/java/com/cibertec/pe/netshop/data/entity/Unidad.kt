package com.cibertec.pe.netshop.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "unidades")
data class Unidad(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String
)
