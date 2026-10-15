package com.pliniopalmer.presupuesto.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pliniopalmer.presupuesto.data.local.entity.GastoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GastoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGasto(gasto: GastoEntity): Long

    @Update
    suspend fun updateGasto(gasto: GastoEntity)

    @Delete
    suspend fun deleteGasto(gasto: GastoEntity)

    @Query("DELETE FROM gastos WHERE id = :id")
    suspend fun deleteGastoById(id: Long)

    @Query("SELECT * FROM gastos WHERE subcategoria_id = :subcategoriaId ORDER BY fecha DESC")
    fun getGastosBySubcategoriaFlow(subcategoriaId: Long): Flow<List<GastoEntity>>
}
