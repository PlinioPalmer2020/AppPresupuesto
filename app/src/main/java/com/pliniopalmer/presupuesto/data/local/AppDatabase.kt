package com.pliniopalmer.presupuesto.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pliniopalmer.presupuesto.data.local.dao.CategoriaDao
import com.pliniopalmer.presupuesto.data.local.dao.GastoDao
import com.pliniopalmer.presupuesto.data.local.dao.IngresoDao
import com.pliniopalmer.presupuesto.data.local.dao.UsuarioDao
import com.pliniopalmer.presupuesto.data.local.entity.CategoriaPrincipalEntity
import com.pliniopalmer.presupuesto.data.local.entity.GastoEntity
import com.pliniopalmer.presupuesto.data.local.entity.IngresoEntity
import com.pliniopalmer.presupuesto.data.local.entity.SubcategoriaEntity
import com.pliniopalmer.presupuesto.data.local.entity.UsuarioEntity

@Database(
    entities = [
        UsuarioEntity::class,
        IngresoEntity::class,
        CategoriaPrincipalEntity::class,
        SubcategoriaEntity::class,
        GastoEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun ingresoDao(): IngresoDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun gastoDao(): GastoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "presupuesto_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
