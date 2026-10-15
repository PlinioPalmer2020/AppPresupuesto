package com.pliniopalmer.presupuesto.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pliniopalmer.presupuesto.data.local.entity.IngresoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IngresoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngreso(ingreso: IngresoEntity): Long

    @Update
    suspend fun updateIngreso(ingreso: IngresoEntity)

    @Delete
    suspend fun deleteIngreso(ingreso: IngresoEntity)

    @Query("SELECT * FROM ingresos WHERE usuario_id = :usuarioId ORDER BY fecha DESC")
    fun getIngresosByUsuarioFlow(usuarioId: Long): Flow<List<IngresoEntity>>
}
