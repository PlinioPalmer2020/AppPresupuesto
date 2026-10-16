package com.pliniopalmer.presupuesto.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.pliniopalmer.presupuesto.data.local.entity.CategoriaPrincipalEntity
import com.pliniopalmer.presupuesto.data.local.entity.SubcategoriaEntity
import com.pliniopalmer.presupuesto.data.local.model.CategoriaConSubcategorias
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoria(categoria: CategoriaPrincipalEntity): Long

    @Update
    suspend fun updateCategoria(categoria: CategoriaPrincipalEntity)

    @Delete
    suspend fun deleteCategoria(categoria: CategoriaPrincipalEntity)

    @Query("DELETE FROM categorias_principales WHERE id = :id")
    suspend fun deleteCategoriaById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubcategoria(subcategoria: SubcategoriaEntity): Long

    @Update
    suspend fun updateSubcategoria(subcategoria: SubcategoriaEntity)

    @Delete
    suspend fun deleteSubcategoria(subcategoria: SubcategoriaEntity)

    @Query("DELETE FROM subcategorias WHERE id = :id")
    suspend fun deleteSubcategoriaById(id: Long)

    @Transaction
    @Query("SELECT * FROM categorias_principales WHERE usuario_id = :usuarioId ORDER BY id DESC")
    fun getCategoriasConSubcategoriasFlow(usuarioId: Long): Flow<List<CategoriaConSubcategorias>>

    @Transaction
    @Query("SELECT * FROM categorias_principales WHERE id = :categoriaId LIMIT 1")
    fun getCategoriaConSubcategoriasFlow(categoriaId: Long): Flow<CategoriaConSubcategorias?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPresupuestoMensual(presupuesto: com.pliniopalmer.presupuesto.data.local.entity.PresupuestoMensualEntity): Long

    @Update
    suspend fun updatePresupuestoMensual(presupuesto: com.pliniopalmer.presupuesto.data.local.entity.PresupuestoMensualEntity)

    @Query("SELECT * FROM presupuestos_mensuales WHERE subcategoria_id = :subcategoriaId AND mes = :mes LIMIT 1")
    suspend fun getPresupuestoMensual(subcategoriaId: Long, mes: String): com.pliniopalmer.presupuesto.data.local.entity.PresupuestoMensualEntity?

    @Query("SELECT * FROM presupuestos_mensuales WHERE subcategoria_id = :subcategoriaId ORDER BY mes DESC LIMIT 1")
    suspend fun getUltimoPresupuestoMensual(subcategoriaId: Long): com.pliniopalmer.presupuesto.data.local.entity.PresupuestoMensualEntity?
}
