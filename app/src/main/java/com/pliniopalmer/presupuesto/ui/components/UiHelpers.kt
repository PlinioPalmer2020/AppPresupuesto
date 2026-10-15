package com.pliniopalmer.presupuesto.ui.components

import androidx.compose.ui.graphics.Color
import com.pliniopalmer.presupuesto.ui.model.BudgetStatus
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    return formatter.format(amount)
}

fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

fun getStatusColor(status: BudgetStatus): Color {
    return when (status) {
        BudgetStatus.DISPONIBLE -> Color(0xFF2E7D32) // Green
        BudgetStatus.AGOTANDOSE -> Color(0xFFEF6C00) // Amber / Orange
        BudgetStatus.AGOTADO_O_EXCEDIDO -> Color(0xFFD32F2F) // Coral / Red
    }
}

fun getStatusLabel(status: BudgetStatus, restante: Double): String {
    return when (status) {
        BudgetStatus.DISPONIBLE -> "Disponible"
        BudgetStatus.AGOTANDOSE -> "Agotándose"
        BudgetStatus.AGOTADO_O_EXCEDIDO -> if (restante < 0) "Excedido" else "Agotado"
    }
}
