package com.pliniopalmer.presupuesto.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pliniopalmer.presupuesto.data.local.entity.CategoriaPrincipalEntity
import com.pliniopalmer.presupuesto.data.local.entity.GastoEntity
import com.pliniopalmer.presupuesto.data.local.entity.IngresoEntity
import com.pliniopalmer.presupuesto.data.local.entity.SubcategoriaEntity
import com.pliniopalmer.presupuesto.data.local.entity.UsuarioEntity
import com.pliniopalmer.presupuesto.data.local.model.CategoriaConSubcategorias
import com.pliniopalmer.presupuesto.data.local.model.SubcategoriaConGastos
import com.pliniopalmer.presupuesto.data.repository.PresupuestoRepository
import com.pliniopalmer.presupuesto.ui.model.BudgetStatus
import com.pliniopalmer.presupuesto.ui.model.CategoriaSummary
import com.pliniopalmer.presupuesto.ui.model.FinancialOverview
import com.pliniopalmer.presupuesto.ui.model.MainUiState
import com.pliniopalmer.presupuesto.ui.model.SubcategoriaSummary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(
    private val repository: PresupuestoRepository
) : ViewModel() {

    val usuarios: StateFlow<List<UsuarioEntity>> = repository.usuariosFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeUserId: StateFlow<Long?> = repository.activeUserIdFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeUsuario: StateFlow<UsuarioEntity?> = combine(
        usuarios,
        activeUserId
    ) { users, activeId ->
        if (users.isEmpty()) null
        else users.find { it.id == activeId } ?: users.first()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val ingresos: StateFlow<List<IngresoEntity>> = activeUsuario
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else repository.getIngresosFlow(user.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rawCategorias: StateFlow<List<CategoriaConSubcategorias>> = activeUsuario
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else repository.getCategoriasConSubcategoriasFlow(user.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val currentMes: StateFlow<java.time.YearMonth> = kotlinx.coroutines.flow.MutableStateFlow(java.time.YearMonth.now())

    val uiState: StateFlow<MainUiState> = combine(
        usuarios,
        activeUsuario,
        ingresos,
        rawCategorias,
        currentMes
    ) { userList, currentUser, incomeList, categoryRelations, mes ->
        val currentMonthStart = mes.atDay(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        val nextMonthStart = mes.plusMonths(1).atDay(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val categorySummaries = categoryRelations.map { calculateCategoriaSummary(it, currentMonthStart, nextMonthStart, mes.toString()) }

        val totalIngresosAllTime = incomeList.sumOf { it.monto }
        val totalPresupuestadoActual = categorySummaries.sumOf { it.presupuestoTotal }
        val totalGastadoActual = categorySummaries.sumOf { it.gastadoTotal }
        
        // Sumar todos los gastos de otros meses (históricos o futuros) que ya salieron del dinero real
        val gastosOtrosMeses = categoryRelations.flatMap { cat ->
            cat.subcategoriasConGastos.flatMap { sub ->
                sub.gastos.filter { it.fecha !in currentMonthStart until nextMonthStart }
            }
        }.sumOf { it.monto }
        
        // Para el mes en curso, deducimos del "Dinero Real" lo que asignaste como presupuesto o lo que ya gastaste (el que sea mayor)
        val deduccionEsteMes = categorySummaries.sumOf { catSum ->
             catSum.subcategorias.sumOf { subSum ->
                 val presu = subSum.restante + subSum.gastadoTotal
                 maxOf(presu, subSum.gastadoTotal)
             }
        }
        
        val dineroRealDisponible = totalIngresosAllTime - gastosOtrosMeses - deduccionEsteMes

        val overview = FinancialOverview(
            dineroRealDisponible = dineroRealDisponible,
            totalIngresos = totalIngresosAllTime,
            totalPresupuestado = totalPresupuestadoActual,
            totalGastado = totalGastadoActual
        )

        MainUiState(
            isLoading = false,
            usuarios = userList,
            activeUsuario = currentUser,
            overview = overview,
            categorias = categorySummaries
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainUiState(isLoading = true))
    
    val annualReportState: StateFlow<com.pliniopalmer.presupuesto.ui.model.AnnualReportState> = combine(
        ingresos,
        rawCategorias
    ) { incomeList, categoryRelations ->
        val mesesConMovimientos = mutableSetOf<String>()
        val gastosPorMes = mutableMapOf<String, Double>()
        val ingresosPorMes = mutableMapOf<String, Double>()
        
        incomeList.forEach { ingreso ->
            val mes = java.time.Instant.ofEpochMilli(ingreso.fecha)
                .atZone(java.time.ZoneId.systemDefault())
                .let { java.time.YearMonth.from(it).toString() }
            mesesConMovimientos.add(mes)
            ingresosPorMes[mes] = (ingresosPorMes[mes] ?: 0.0) + ingreso.monto
        }
        
        categoryRelations.forEach { cat ->
            cat.subcategoriasConGastos.forEach { sub ->
                sub.gastos.forEach { gasto ->
                    val mes = java.time.Instant.ofEpochMilli(gasto.fecha)
                        .atZone(java.time.ZoneId.systemDefault())
                        .let { java.time.YearMonth.from(it).toString() }
                    mesesConMovimientos.add(mes)
                    gastosPorMes[mes] = (gastosPorMes[mes] ?: 0.0) + gasto.monto
                }
            }
        }
        
        val reportes = mesesConMovimientos.sortedDescending().map { mes ->
            val ingresos = ingresosPorMes[mes] ?: 0.0
            val gastos = gastosPorMes[mes] ?: 0.0
            com.pliniopalmer.presupuesto.ui.model.MonthlyReport(
                mes = mes,
                totalIngresos = ingresos,
                totalGastado = gastos,
                balance = ingresos - gastos
            )
        }
        
        val totalIngresos = reportes.sumOf { it.totalIngresos }
        val totalGastado = reportes.sumOf { it.totalGastado }
        
        com.pliniopalmer.presupuesto.ui.model.AnnualReportState(
            isLoading = false,
            reportesMensuales = reportes,
            totalIngresosAnual = totalIngresos,
            totalGastadoAnual = totalGastado,
            balanceAnual = totalIngresos - totalGastado
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.pliniopalmer.presupuesto.ui.model.AnnualReportState(isLoading = true))

    fun createUsuario(nombre: String) {
        viewModelScope.launch {
            repository.createUsuario(nombre.trim())
        }
    }

    fun selectUsuario(userId: Long) {
        viewModelScope.launch {
            repository.setActiveUserId(userId)
        }
    }

    fun addIngreso(monto: Double, descripcion: String, fecha: Long = System.currentTimeMillis()) {
        val user = activeUsuario.value ?: return
        viewModelScope.launch {
            repository.addIngreso(
                usuarioId = user.id,
                monto = monto,
                descripcion = descripcion.trim(),
                fecha = fecha
            )
        }
    }

    fun updateIngreso(ingreso: IngresoEntity) {
        viewModelScope.launch {
            repository.updateIngreso(ingreso)
        }
    }

    fun deleteIngreso(ingreso: IngresoEntity) {
        viewModelScope.launch {
            repository.deleteIngreso(ingreso)
        }
    }

    fun addCategoria(nombre: String, icono: String? = null) {
        val user = activeUsuario.value ?: return
        viewModelScope.launch {
            repository.addCategoria(
                usuarioId = user.id,
                nombre = nombre.trim(),
                icono = icono
            )
        }
    }

    fun updateCategoria(categoria: CategoriaPrincipalEntity) {
        viewModelScope.launch {
            repository.updateCategoria(categoria)
        }
    }

    fun deleteCategoria(categoriaId: Long) {
        viewModelScope.launch {
            repository.deleteCategoria(categoriaId)
        }
    }

    fun addSubcategoria(categoriaId: Long, nombre: String, presupuesto: Double) {
        viewModelScope.launch {
            repository.addSubcategoria(
                categoriaId = categoriaId,
                nombre = nombre.trim(),
                presupuesto = presupuesto
            )
        }
    }

    fun updateSubcategoria(subcategoria: SubcategoriaEntity) {
        viewModelScope.launch {
            repository.updateSubcategoria(subcategoria)
        }
    }

    fun updateSubcategoriaPresupuesto(subcategoriaId: Long, mes: String, monto: Double) {
        viewModelScope.launch {
            repository.updateSubcategoriaPresupuesto(subcategoriaId, mes, monto)
        }
    }

    fun deleteSubcategoria(subcategoriaId: Long) {
        viewModelScope.launch {
            repository.deleteSubcategoria(subcategoriaId)
        }
    }

    fun addGasto(subcategoriaId: Long, monto: Double, descripcion: String, fecha: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            repository.addGasto(
                subcategoriaId = subcategoriaId,
                monto = monto,
                descripcion = descripcion.trim(),
                fecha = fecha
            )
        }
    }

    fun updateGasto(gasto: GastoEntity) {
        viewModelScope.launch {
            repository.updateGasto(gasto)
        }
    }

    fun deleteGasto(gastoId: Long) {
        viewModelScope.launch {
            repository.deleteGasto(gastoId)
        }
    }

    companion object {
        fun calculateSubcategoriaSummary(
            subWithGastos: SubcategoriaConGastos,
            currentMonthStart: Long,
            nextMonthStart: Long,
            mesActual: String
        ): SubcategoriaSummary {
            val sub = subWithGastos.subcategoria
            // Filtrar gastos por fecha (mes actual)
            val gastos = subWithGastos.gastos.filter { it.fecha in currentMonthStart until nextMonthStart }
            
            // Buscar el presupuesto para el mes actual. Si no existe, buscamos el más reciente de los meses anteriores.
            val presupuesto = subWithGastos.presupuestos.find { it.mes == mesActual }?.monto 
                ?: subWithGastos.presupuestos.filter { it.mes < mesActual }.maxByOrNull { it.mes }?.monto 
                ?: 0.0
            
            val gastadoTotal = gastos.sumOf { it.monto }
            val restante = presupuesto - gastadoTotal

            val porcentajeRestante = if (presupuesto > 0) {
                (restante / presupuesto).toFloat().coerceIn(0f, 1f)
            } else if (restante <= 0 && gastadoTotal > 0) {
                0f
            } else {
                1f
            }

            val status = when {
                restante <= 0 -> BudgetStatus.AGOTADO_O_EXCEDIDO
                porcentajeRestante <= 0.25f -> BudgetStatus.AGOTANDOSE
                else -> BudgetStatus.DISPONIBLE
            }

            return SubcategoriaSummary(
                subcategoria = sub,
                gastos = gastos,
                gastadoTotal = gastadoTotal,
                restante = restante,
                porcentajeRestante = porcentajeRestante,
                status = status
            )
        }

        fun calculateCategoriaSummary(
            rel: CategoriaConSubcategorias,
            currentMonthStart: Long,
            nextMonthStart: Long,
            mesActual: String
        ): CategoriaSummary {
            val subSummaries = rel.subcategoriasConGastos.map { 
                calculateSubcategoriaSummary(it, currentMonthStart, nextMonthStart, mesActual) 
            }
            // En vez de sacar presupuesto de la entidad base, lo sumamos del summary de cada subcategoria
            val presupuestoTotal = subSummaries.sumOf { summary -> 
                summary.restante + summary.gastadoTotal
            }
            val gastadoTotal = subSummaries.sumOf { it.gastadoTotal }
            val restante = presupuestoTotal - gastadoTotal

            val porcentajeRestante = if (presupuestoTotal > 0) {
                (restante / presupuestoTotal).toFloat().coerceIn(0f, 1f)
            } else if (restante <= 0 && gastadoTotal > 0) {
                0f
            } else {
                1f
            }

            val status = when {
                restante <= 0 -> BudgetStatus.AGOTADO_O_EXCEDIDO
                porcentajeRestante <= 0.25f -> BudgetStatus.AGOTANDOSE
                else -> BudgetStatus.DISPONIBLE
            }

            return CategoriaSummary(
                categoria = rel.categoria,
                subcategorias = subSummaries,
                presupuestoTotal = presupuestoTotal,
                gastadoTotal = gastadoTotal,
                restante = restante,
                porcentajeRestante = porcentajeRestante,
                status = status
            )
        }
    }
}

class MainViewModelFactory(
    private val repository: PresupuestoRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
