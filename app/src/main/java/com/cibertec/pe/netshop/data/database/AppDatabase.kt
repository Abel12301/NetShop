package com.cibertec.pe.netshop.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cibertec.pe.netshop.data.dao.*
import com.cibertec.pe.netshop.data.entity.*

@Database(
    entities = [
        Producto::class,
        Empleado::class,
        Cliente::class,
        Proveedor::class,
        Venta::class,
        DetalleVenta::class,
        Categoria::class,   // ✅ Nueva entidad
        Unidad::class       // ✅ Nueva entidad
    ],
    version = 7,  // ✅ Aumentado
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productoDao(): ProductoDao
    abstract fun empleadoDao(): EmpleadoDao
    abstract fun clienteDao(): ClienteDao
    abstract fun proveedorDao(): ProveedorDao
    abstract fun ventaDao(): VentaDao
    abstract fun detalleVentaDao(): DetalleVentaDao
    abstract fun categoriaDao(): CategoriaDao       // ✅ Nuevo DAO
    abstract fun unidadDao(): UnidadDao             // ✅ Nuevo DAO

    companion object {
        @Volatile private var instancia: AppDatabase? = null

        fun obtenerInstancia(context: Context): AppDatabase {
            return instancia ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "productos_db"
                ).fallbackToDestructiveMigration()
                    .build().also { instancia = it }
            }
        }
    }
}
