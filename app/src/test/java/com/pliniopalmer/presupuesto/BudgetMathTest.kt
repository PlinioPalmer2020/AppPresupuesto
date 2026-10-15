package com.pliniopalmer.presupuesto

import com.pliniopalmer.presupuesto.data.local.entity.CategoriaPrincipalEntity
import com.pliniopalmer.presupuesto.data.local.entity.GastoEntity
import com.pliniopalmer.presupuesto.data.local.entity.SubcategoriaEntity
import com.pliniopalmer.presupuesto.data.local.model.CategoriaConSubcategorias
import com.pliniopalmer.presupuesto.data.local.model.SubcategoriaConGastos
import com.pliniopalmer.presupuesto.ui.model.BudgetStatus
import com.pliniopalmer.presupuesto.ui.viewmodel.MainViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

class BudgetMathTest {

    @Test
    fun testSubcategoriaSummaryCalculation() {
        val subcategoria = SubcategoriaEntity(
            id = 1,
            categoriaId = 10,
            nombre = "UNAPEC",
            presupuesto = 5000.0
        )
        val gastos = listOf(
            GastoEntity(id = 1, subcategoriaId = 1, monto = 2000.0, descripcion = "Inscripción")
        )

        val subWithGastos = SubcategoriaConGastos(subcategoria, gastos)
        val summary = MainViewModel.calculateSubcategoriaSummary(subWithGastos)

        assertEquals(2000.0, summary.gastadoTotal, 0.01)
        assertEquals(3000.0, summary.restante, 0.01)
        assertEquals(0.60f, summary.porcentajeRestante, 0.01f)
        assertEquals(BudgetStatus.DISPONIBLE, summary.status)
    }

    @Test
    fun testCategoriaSummaryAndDineroRealCalculation() {
        val categoria = CategoriaPrincipalEntity(id = 10, usuarioId = 1, nombre = "Universidad")

        val unapecSub = SubcategoriaEntity(id = 1, categoriaId = 10, nombre = "UNAPEC", presupuesto = 5000.0)
        val unapecGastos = listOf(GastoEntity(id = 1, subcategoriaId = 1, monto = 2000.0, descripcion = "Inscripción"))

        val itlaSub = SubcategoriaEntity(id = 2, categoriaId = 10, nombre = "ITLA", presupuesto = 3000.0)
        val itlaGastos = emptyList<GastoEntity>()

        val rel = CategoriaConSubcategorias(
            categoria = categoria,
            subcategoriasConGastos = listOf(
                SubcategoriaConGastos(unapecSub, unapecGastos),
                SubcategoriaConGastos(itlaSub, itlaGastos)
            )
        )

        val catSummary = MainViewModel.calculateCategoriaSummary(rel)

        assertEquals(8000.0, catSummary.presupuestoTotal, 0.01)
        assertEquals(2000.0, catSummary.gastadoTotal, 0.01)
        assertEquals(6000.0, catSummary.restante, 0.01)

        val totalIngresos = 20000.0
        val dineroRealDisponible = totalIngresos - catSummary.presupuestoTotal
        assertEquals(12000.0, dineroRealDisponible, 0.01)
    }
}
