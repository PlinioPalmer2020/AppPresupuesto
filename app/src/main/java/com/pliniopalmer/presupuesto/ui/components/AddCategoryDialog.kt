package com.pliniopalmer.presupuesto.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.pliniopalmer.presupuesto.data.local.entity.CategoriaPrincipalEntity

@Composable
fun AddCategoryDialog(
    categoryToEdit: CategoriaPrincipalEntity? = null,
    onDismissRequest: () -> Unit,
    onConfirm: (nombre: String) -> Unit
) {
    var name by remember { mutableStateOf(categoryToEdit?.nombre ?: "") }
    var isError by remember { mutableStateOf(false) }

    val isEditing = categoryToEdit != null

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(if (isEditing) "Editar Categoría" else "Nueva Categoría Principal") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Ingresa el nombre de la categoría principal (ej. Universidad, Luz, Entretenimiento):")
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        isError = false
                    },
                    label = { Text("Nombre de Categoría") },
                    placeholder = { Text("Ej. Universidad") },
                    isError = isError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth()
                )
                if (isError) {
                    Text(
                        text = "El nombre es obligatorio",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        isError = true
                    } else {
                        onConfirm(name.trim())
                    }
                }
            ) {
                Text(if (isEditing) "Guardar Cambios" else "Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar")
            }
        }
    )
}
