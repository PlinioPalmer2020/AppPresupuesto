package com.pliniopalmer.presupuesto.ui.model

data class MonthlyReport(
    val mes: String, // "YYYY-MM"
    val totalIngresos: Double,
    val totalGastado: Double,
    val balance: Double
)

data class AnnualReportState(
    val isLoading: Boolean = false,
    val reportesMensuales: List<MonthlyReport> = emptyList(),
    val totalIngresosAnual: Double = 0.0,
    val totalGastadoAnual: Double = 0.0,
    val balanceAnual: Double = 0.0
)
