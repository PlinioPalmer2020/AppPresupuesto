package com.pliniopalmer.presupuesto.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pliniopalmer.presupuesto.data.local.entity.GastoEntity
import com.pliniopalmer.presupuesto.data.local.entity.SubcategoriaEntity
import com.pliniopalmer.presupuesto.ui.components.AddExpenseBottomSheet
import com.pliniopalmer.presupuesto.ui.components.AddSubcategoryBottomSheet
import com.pliniopalmer.presupuesto.ui.components.SubcategoryCard
import com.pliniopalmer.presupuesto.ui.components.formatCurrency
import com.pliniopalmer.presupuesto.ui.components.getStatusColor
import com.pliniopalmer.presupuesto.ui.components.getStatusLabel
import com.pliniopalmer.presupuesto.ui.model.SubcategoriaSummary
import com.pliniopalmer.presupuesto.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    categoriaId: Long,
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val categorySummary = uiState.categorias.find { it.categoria.id == categoriaId }

    var showAddSubcategorySheet by remember { mutableStateOf(false) }
    var subcategoryToEdit by remember { mutableStateOf<SubcategoriaSummary?>(null) }
    var subcategoryForExpense by remember { mutableStateOf<SubcategoriaSummary?>(null) }
    var subcategoryToDelete by remember { mutableStateOf<SubcategoriaSummary?>(null) }
    var expenseToDelete by remember { mutableStateOf<GastoEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = categorySummary?.categoria?.nombre ?: "Detalle de Categoría",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            if (categorySummary != null) {
                ExtendedFloatingActionButton(
                    onClick = {
                        subcategoryToEdit = null
                        showAddSubcategorySheet = true
                    },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Nueva Subcategoría") }
                )
            }
        }
    ) { innerPadding ->
        if (categorySummary == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Categoría no encontrada")
            }
        } else {
            val statusColor = getStatusColor(categorySummary.status)
            val statusLabel = getStatusLabel(categorySummary.status, categorySummary.restante)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Category Overview Card
                item {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Presupuesto Total",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = formatCurrency(categorySummary.presupuestoTotal),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(statusColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "$statusLabel: ${formatCurrency(categorySummary.restante)}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Gastado: ${formatCurrency(categorySummary.gastadoTotal)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Subcategorías: ${categorySummary.subcategorias.size}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            LinearProgressIndicator(
                                progress = { categorySummary.porcentajeRestante },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = statusColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Subcategorías",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (categorySummary.subcategorias.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No hay subcategorías en esta categoría",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Crea subcategorías (ej. UNAPEC, ITLA) para asignarles parte de tu Dinero Real Disponible.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(categorySummary.subcategorias, key = { it.subcategoria.id }) { subSummary ->
                        SubcategoryCard(
                            summary = subSummary,
                            onAddExpenseClick = {
                                subcategoryForExpense = subSummary
                            },
                            onEditBudgetClick = {
                                subcategoryToEdit = subSummary
                                showAddSubcategorySheet = true
                            },
                            onDeleteClick = {
                                subcategoryToDelete = subSummary
                            },
                            onDeleteExpenseClick = { gasto ->
                                expenseToDelete = gasto
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialogs & BottomSheets
    if (showAddSubcategorySheet) {
        AddSubcategoryBottomSheet(
            dineroRealDisponible = uiState.overview.dineroRealDisponible,
            subcategoriaToEdit = subcategoryToEdit?.subcategoria,
            presupuestoActual = subcategoryToEdit?.let { it.restante + it.gastadoTotal } ?: 0.0,
            onDismissRequest = {
                showAddSubcategorySheet = false
                subcategoryToEdit = null
            },
            onConfirm = { nombre, presupuesto ->
                val editing = subcategoryToEdit
                if (editing != null) {
                    viewModel.updateSubcategoria(editing.subcategoria.copy(nombre = nombre))
                    viewModel.updateSubcategoriaPresupuesto(editing.subcategoria.id, viewModel.currentMes.value.toString(), presupuesto)
                } else {
                    viewModel.addSubcategoria(categoriaId, nombre, presupuesto)
                }
                showAddSubcategorySheet = false
                subcategoryToEdit = null
            }
        )
    }

    subcategoryForExpense?.let { subSummary ->
        AddExpenseBottomSheet(
            subcategoriaNombre = subSummary.subcategoria.nombre,
            onDismissRequest = { subcategoryForExpense = null },
            onConfirm = { monto, descripcion ->
                viewModel.addGasto(subSummary.subcategoria.id, monto, descripcion)
                subcategoryForExpense = null
            }
        )
    }

    subcategoryToDelete?.let { subSummary ->
        AlertDialog(
            onDismissRequest = { subcategoryToDelete = null },
            title = { Text("Eliminar Subcategoría") },
            text = {
                val presu = subSummary.restante + subSummary.gastadoTotal
                Text("¿Deseas eliminar la subcategoría '${subSummary.subcategoria.nombre}'? Su presupuesto asignado de ${formatCurrency(presu)} volverá automáticamente a tu Dinero Real Disponible.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSubcategoria(subSummary.subcategoria.id)
                        subcategoryToDelete = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { subcategoryToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    expenseToDelete?.let { gasto ->
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            title = { Text("Eliminar Gasto") },
            text = {
                Text("¿Deseas eliminar el gasto de ${formatCurrency(gasto.monto)} ('${gasto.descripcion}')? Este saldo se devolverá a la subcategoría.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteGasto(gasto.id)
                        expenseToDelete = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
