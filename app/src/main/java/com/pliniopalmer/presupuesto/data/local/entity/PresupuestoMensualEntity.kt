package com.pliniopalmer.presupuesto.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "presupuestos_mensuales",
    foreignKeys = [
        ForeignKey(
            entity = SubcategoriaEntity::class,
            parentColumns = ["id"],
            childColumns = ["subcategoria_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subcategoria_id"), Index("mes")]
)
data class PresupuestoMensualEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "subcategoria_id")
    val subcategoriaId: Long,
    val mes: String, // Formato "YYYY-MM"
    val monto: Double
)
