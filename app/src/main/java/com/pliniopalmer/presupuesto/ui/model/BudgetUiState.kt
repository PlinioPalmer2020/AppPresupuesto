package com.pliniopalmer.presupuesto.ui.model

import com.pliniopalmer.presupuesto.data.local.entity.CategoriaPrincipalEntity
import com.pliniopalmer.presupuesto.data.local.entity.GastoEntity
import com.pliniopalmer.presupuesto.data.local.entity.SubcategoriaEntity
import com.pliniopalmer.presupuesto.data.local.entity.UsuarioEntity

enum class BudgetStatus {
    DISPONIBLE,
    AGOTANDOSE,
    AGOTADO_O_EXCEDIDO
}

data class FinancialOverview(
    val dineroRealDisponible: Double = 0.0,
    val totalIngresos: Double = 0.0,
    val totalPresupuestado: Double = 0.0,
    val totalGastado: Double = 0.0
) {
    val totalPresupuestoDisponible: Double
        get() = totalPresupuestado - totalGastado
}

data class SubcategoriaSummary(
    val subcategoria: SubcategoriaEntity,
    val gastos: List<GastoEntity>,
    val gastadoTotal: Double,
    val restante: Double,
    val porcentajeRestante: Float,
    val status: BudgetStatus
)

data class CategoriaSummary(
    val categoria: CategoriaPrincipalEntity,
    val subcategorias: List<SubcategoriaSummary>,
    val presupuestoTotal: Double,
    val gastadoTotal: Double,
    val restante: Double,
    val porcentajeRestante: Float,
    val status: BudgetStatus
)

data class MainUiState(
    val isLoading: Boolean = true,
    val usuarios: List<UsuarioEntity> = emptyList(),
    val activeUsuario: UsuarioEntity? = null,
    val overview: FinancialOverview = FinancialOverview(),
    val categorias: List<CategoriaSummary> = emptyList()
)
