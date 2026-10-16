package com.pliniopalmer.presupuesto.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pliniopalmer.presupuesto.data.local.entity.SubcategoriaEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubcategoryBottomSheet(
    dineroRealDisponible: Double,
    subcategoriaToEdit: SubcategoriaEntity? = null,
    onDismissRequest: () -> Unit,
    onConfirm: (nombre: String, presupuesto: Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf(subcategoriaToEdit?.nombre ?: "") }
    var budgetText by remember { mutableStateOf(subcategoriaToEdit?.presupuesto?.toString() ?: "") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var budgetError by remember { mutableStateOf<String?>(null) }

    val isEditing = subcategoriaToEdit != null
    val originalPresupuesto = subcategoriaToEdit?.presupuesto ?: 0.0
    // Real cash available considering any budget already assigned to this subcategory if editing
    val effectiveAvailableCash = dineroRealDisponible + originalPresupuesto

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = if (isEditing) "Ajustar Presupuesto" else "Nueva Subcategoría",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                    .padding(12.dp)
            ) {
                Text(
                    text = "Dinero Real Disponible: ${formatCurrency(effectiveAvailableCash)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = null
                },
                label = { Text("Nombre de Subcategoría") },
                placeholder = { Text("Ej. UNAPEC, ITLA") },
                isError = nameError != null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth()
            )
            nameError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = budgetText,
                onValueChange = { newValue ->
                    // Filtro que solo permite dígitos y un solo punto
                    val filteredValue = newValue.filter { it.isDigit() || it == '.' }
                    // Asegurar que solo haya un punto decimal
                    val pointCount = filteredValue.count { it == '.' }
                    if (pointCount <= 1) {
                        // Limitar a máximo 2 decimales
                        val parts = filteredValue.split(".")
                        if (parts.size == 1 || (parts.size == 2 && parts[1].length <= 2)) {
                            budgetText = filteredValue
                            budgetError = null
                        }
                    }
                },
                label = { Text("Presupuesto Asignado ($)") },
                placeholder = { Text("0.00") },
                isError = budgetError != null,
                singleLine = true,
                visualTransformation = CurrencyVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            budgetError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val budget = budgetText.toDoubleOrNull()
                    var valid = true

                    if (name.isBlank()) {
                        nameError = "El nombre es obligatorio"
                        valid = false
                    }
                    if (budget == null || budget < 0) {
                        budgetError = "Ingresa un presupuesto válido (>= 0)"
                        valid = false
                    }

                    if (valid && budget != null) {
                        onConfirm(name.trim(), budget)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditing) "Guardar Cambios" else "Asignar Presupuesto")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
