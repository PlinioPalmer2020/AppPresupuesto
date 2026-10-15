package com.pliniopalmer.presupuesto.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.pliniopalmer.presupuesto.data.local.entity.CategoriaPrincipalEntity
import com.pliniopalmer.presupuesto.data.local.entity.GastoEntity
import com.pliniopalmer.presupuesto.data.local.entity.SubcategoriaEntity

data class SubcategoriaConGastos(
    @Embedded val subcategoria: SubcategoriaEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "subcategoria_id"
    )
    val gastos: List<GastoEntity>
)

data class CategoriaConSubcategorias(
    @Embedded val categoria: CategoriaPrincipalEntity,
    @Relation(
        entity = SubcategoriaEntity::class,
        parentColumn = "id",
        entityColumn = "categoria_id"
    )
    val subcategoriasConGastos: List<SubcategoriaConGastos>
)
