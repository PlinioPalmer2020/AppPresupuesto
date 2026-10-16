package com.pliniopalmer.presupuesto.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.pliniopalmer.presupuesto.ui.model.BudgetStatus
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CurrencyVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val parts = originalText.split(".")
        val intPart = parts[0]
        val decimalPart = if (parts.size > 1) "." + parts[1] else ""

        val formatter = DecimalFormat("#,###")
        val formattedIntPart = try {
            formatter.format(intPart.toLong())
        } catch (e: NumberFormatException) {
            intPart
        }

        val outText = formattedIntPart + decimalPart

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (originalText.isEmpty()) return 0
                val safeOffset = offset.coerceIn(0, originalText.length)

                var originalIndex = 0
                var transformedIndex = 0

                while (originalIndex < safeOffset && transformedIndex < outText.length) {
                    if (outText[transformedIndex] == ',') {
                        transformedIndex++
                    } else {
                        originalIndex++
                        transformedIndex++
                    }
                }
                return transformedIndex
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (outText.isEmpty()) return 0
                val safeOffset = offset.coerceIn(0, outText.length)
                var originalIndex = 0
                var transformedIndex = 0

                while (transformedIndex < safeOffset && originalIndex < originalText.length) {
                    if (outText[transformedIndex] == ',') {
                        transformedIndex++
                    } else {
                        originalIndex++
                        transformedIndex++
                    }
                }
                return originalIndex
            }
        }

        return TransformedText(AnnotatedString(outText), offsetMapping)
    }
}

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
