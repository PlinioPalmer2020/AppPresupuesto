package com.pliniopalmer.presupuesto.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseBottomSheet(
    subcategoriaNombre: String,
    onDismissRequest: () -> Unit,
    onConfirm: (monto: Double, descripcion: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var amountText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf<String?>(null) }
    var descError by remember { mutableStateOf<String?>(null) }

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
                text = "Registrar Gasto en $subcategoriaNombre",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = amountText,
                onValueChange = { newValue ->
                    // Filtro que solo permite dígitos y un solo punto
                    val filteredValue = newValue.filter { it.isDigit() || it == '.' }
                    // Asegurar que solo haya un punto decimal
                    val pointCount = filteredValue.count { it == '.' }
                    if (pointCount <= 1) {
                        // Limitar a máximo 2 decimales
                        val parts = filteredValue.split(".")
                        if (parts.size == 1 || (parts.size == 2 && parts[1].length <= 2)) {
                            amountText = filteredValue
                            amountError = null
                        }
                    }
                },
                label = { Text("Monto Gastado ($)") },
                placeholder = { Text("0.00") },
                isError = amountError != null,
                singleLine = true,
                visualTransformation = CurrencyVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            amountError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                    descError = null
                },
                label = { Text("Descripción del Gasto") },
                placeholder = { Text("Ej. Inscripción, Mensualidad, Carnet") },
                isError = descError != null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth()
            )
            descError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    var valid = true

                    if (amount == null || amount <= 0) {
                        amountError = "Ingresa un monto válido mayor a $0"
                        valid = false
                    }
                    if (description.isBlank()) {
                        descError = "La descripción es obligatoria"
                        valid = false
                    }

                    if (valid && amount != null) {
                        onConfirm(amount, description.trim())
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Gasto")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
