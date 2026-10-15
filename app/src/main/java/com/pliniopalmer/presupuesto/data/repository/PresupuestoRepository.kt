package com.pliniopalmer.presupuesto.data.repository

import com.pliniopalmer.presupuesto.data.local.AppDatabase
import com.pliniopalmer.presupuesto.data.local.UserPreferences
import com.pliniopalmer.presupuesto.data.local.entity.CategoriaPrincipalEntity
import com.pliniopalmer.presupuesto.data.local.entity.GastoEntity
import com.pliniopalmer.presupuesto.data.local.entity.IngresoEntity
import com.pliniopalmer.presupuesto.data.local.entity.SubcategoriaEntity
import com.pliniopalmer.presupuesto.data.local.entity.UsuarioEntity
import com.pliniopalmer.presupuesto.data.local.model.CategoriaConSubcategorias
import kotlinx.coroutines.flow.Flow

class PresupuestoRepository(
    private val database: AppDatabase,
    private val userPreferences: UserPreferences
) {
    private val usuarioDao = database.usuarioDao()
    private val ingresoDao = database.ingresoDao()
    private val categoriaDao = database.categoriaDao()
    private val gastoDao = database.gastoDao()

    // Preferences & Users
    val activeUserIdFlow: Flow<Long?> = userPreferences.activeUserIdFlow
    val usuariosFlow: Flow<List<UsuarioEntity>> = usuarioDao.getUsuariosFlow()

    suspend fun setActiveUserId(userId: Long) {
        userPreferences.setActiveUserId(userId)
    }

    suspend fun createUsuario(nombre: String): Long {
        val usuario = UsuarioEntity(nombre = nombre)
        val id = usuarioDao.insertUsuario(usuario)
        userPreferences.setActiveUserId(id)
        return id
    }

    suspend fun updateUsuario(usuario: UsuarioEntity) {
        usuarioDao.updateUsuario(usuario)
    }

    suspend fun deleteUsuario(usuario: UsuarioEntity) {
        usuarioDao.deleteUsuario(usuario)
    }

    // Ingresos
    fun getIngresosFlow(usuarioId: Long): Flow<List<IngresoEntity>> {
        return ingresoDao.getIngresosByUsuarioFlow(usuarioId)
    }

    suspend fun addIngreso(usuarioId: Long, monto: Double, descripcion: String, fecha: Long = System.currentTimeMillis()): Long {
        val ingreso = IngresoEntity(
            usuarioId = usuarioId,
            monto = monto,
            descripcion = descripcion,
            fecha = fecha
        )
        return ingresoDao.insertIngreso(ingreso)
    }

    suspend fun updateIngreso(ingreso: IngresoEntity) {
        ingresoDao.updateIngreso(ingreso)
    }

    suspend fun deleteIngreso(ingreso: IngresoEntity) {
        ingresoDao.deleteIngreso(ingreso)
    }

    // Categorias & Subcategorias
    fun getCategoriasConSubcategoriasFlow(usuarioId: Long): Flow<List<CategoriaConSubcategorias>> {
        return categoriaDao.getCategoriasConSubcategoriasFlow(usuarioId)
    }

    fun getCategoriaConSubcategoriasFlow(categoriaId: Long): Flow<CategoriaConSubcategorias?> {
        return categoriaDao.getCategoriaConSubcategoriasFlow(categoriaId)
    }

    suspend fun addCategoria(usuarioId: Long, nombre: String, icono: String? = null): Long {
        val categoria = CategoriaPrincipalEntity(
            usuarioId = usuarioId,
            nombre = nombre,
            icono = icono
        )
        return categoriaDao.insertCategoria(categoria)
    }

    suspend fun updateCategoria(categoria: CategoriaPrincipalEntity) {
        categoriaDao.updateCategoria(categoria)
    }

    suspend fun deleteCategoria(categoriaId: Long) {
        categoriaDao.deleteCategoriaById(categoriaId)
    }

    suspend fun addSubcategoria(categoriaId: Long, nombre: String, presupuesto: Double): Long {
        val subcategoria = SubcategoriaEntity(
            categoriaId = categoriaId,
            nombre = nombre,
            presupuesto = presupuesto
        )
        return categoriaDao.insertSubcategoria(subcategoria)
    }

    suspend fun updateSubcategoria(subcategoria: SubcategoriaEntity) {
        categoriaDao.updateSubcategoria(subcategoria)
    }

    suspend fun deleteSubcategoria(subcategoriaId: Long) {
        categoriaDao.deleteSubcategoriaById(subcategoriaId)
    }

    // Gastos
    fun getGastosBySubcategoriaFlow(subcategoriaId: Long): Flow<List<GastoEntity>> {
        return gastoDao.getGastosBySubcategoriaFlow(subcategoriaId)
    }

    suspend fun addGasto(subcategoriaId: Long, monto: Double, descripcion: String, fecha: Long = System.currentTimeMillis()): Long {
        val gasto = GastoEntity(
            subcategoriaId = subcategoriaId,
            monto = monto,
            descripcion = descripcion,
            fecha = fecha
        )
        return gastoDao.insertGasto(gasto)
    }

    suspend fun updateGasto(gasto: GastoEntity) {
        gastoDao.updateGasto(gasto)
    }

    suspend fun deleteGasto(gastoId: Long) {
        gastoDao.deleteGastoById(gastoId)
    }
}
