package com.pliniopalmer.presupuesto.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "gastos",
    foreignKeys = [
        ForeignKey(
            entity = SubcategoriaEntity::class,
            parentColumns = ["id"],
            childColumns = ["subcategoria_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subcategoria_id")]
)
data class GastoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "subcategoria_id")
    val subcategoriaId: Long,
    val monto: Double,
    val descripcion: String,
    val fecha: Long = System.currentTimeMillis()
)
