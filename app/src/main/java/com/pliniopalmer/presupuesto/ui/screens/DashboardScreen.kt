package com.pliniopalmer.presupuesto.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pliniopalmer.presupuesto.data.local.entity.CategoriaPrincipalEntity
import com.pliniopalmer.presupuesto.data.local.entity.IngresoEntity
import com.pliniopalmer.presupuesto.ui.components.AddCategoryDialog
import com.pliniopalmer.presupuesto.ui.components.AddIncomeBottomSheet
import com.pliniopalmer.presupuesto.ui.components.CategoryCard
import com.pliniopalmer.presupuesto.ui.components.CreateUserDialog
import com.pliniopalmer.presupuesto.ui.components.FinancialSummaryCard
import com.pliniopalmer.presupuesto.ui.components.IncomeHistoryDialog
import com.pliniopalmer.presupuesto.ui.components.UserSelectorBar
import com.pliniopalmer.presupuesto.ui.model.CategoriaSummary
import com.pliniopalmer.presupuesto.ui.viewmodel.MainViewModel

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onCategoryClick: (Long) -> Unit,
    onReportsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val ingresos by viewModel.ingresos.collectAsState()

    var showAddIncomeSheet by remember { mutableStateOf(false) }
    var showIncomeHistoryDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<CategoriaPrincipalEntity?>(null) }
    var categoryToDelete by remember { mutableStateOf<CategoriaSummary?>(null) }
    var showCreateUserDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            UserSelectorBar(
                usuarios = uiState.usuarios,
                activeUsuario = uiState.activeUsuario,
                onSelectUsuario = { viewModel.selectUsuario(it) },
                onCreateUsuarioClick = { showCreateUserDialog = true },
                onManageIncomeClick = { showAddIncomeSheet = true },
                onReportsClick = onReportsClick
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    categoryToEdit = null
                    showAddCategoryDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nueva Categoría") }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Resumen Financiero",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    if (ingresos.isNotEmpty()) {
                        TextButton(onClick = { showIncomeHistoryDialog = true }) {
                            Text("Ver Historial (${ingresos.size})")
                        }
                    }
                }
            }

            item {
                FinancialSummaryCard(overview = uiState.overview)
            }

            item {
                Text(
                    text = "Categorías Principales",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (uiState.categorias.isEmpty()) {
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
                                text = "No tienes categorías registradas",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Toca el botón '+ Nueva Categoría' para empezar a estructurar tu presupuesto (ej. Universidad, Servicios, Entretenimiento).",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(uiState.categorias, key = { it.categoria.id }) { catSummary ->
                    CategoryCard(
                        categorySummary = catSummary,
                        onClick = { onCategoryClick(catSummary.categoria.id) },
                        onEditClick = {
                            categoryToEdit = catSummary.categoria
                            showAddCategoryDialog = true
                        },
                        onDeleteClick = {
                            categoryToDelete = catSummary
                        }
                    )
                }
            }
        }
    }

    // Dialogs & BottomSheets
    if (showAddIncomeSheet) {
        AddIncomeBottomSheet(
            onDismissRequest = { showAddIncomeSheet = false },
            onConfirm = { monto, descripcion ->
                showAddIncomeSheet = false
                viewModel.addIngreso(monto, descripcion)
            }
        )
    }

    if (showIncomeHistoryDialog) {
        IncomeHistoryDialog(
            ingresos = ingresos,
            onDismissRequest = { showIncomeHistoryDialog = false },
            onDeleteIngreso = { ingreso ->
                viewModel.deleteIngreso(ingreso)
            }
        )
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            categoryToEdit = categoryToEdit,
            onDismissRequest = {
                showAddCategoryDialog = false
                categoryToEdit = null
            },
            onConfirm = { nombre ->
                val editing = categoryToEdit
                if (editing != null) {
                    viewModel.updateCategoria(editing.copy(nombre = nombre))
                } else {
                    viewModel.addCategoria(nombre)
                }
                showAddCategoryDialog = false
                categoryToEdit = null
            }
        )
    }

    if (showCreateUserDialog) {
        CreateUserDialog(
            onDismissRequest = { showCreateUserDialog = false },
            onConfirm = { nombre ->
                viewModel.createUsuario(nombre)
                showCreateUserDialog = false
            }
        )
    }

    categoryToDelete?.let { catSummary ->
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("Eliminar Categoría") },
            text = {
                Text("¿Estás seguro de que deseas eliminar la categoría '${catSummary.categoria.nombre}'? Se eliminarán todas sus subcategorías y gastos asociados.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCategoria(catSummary.categoria.id)
                        categoryToDelete = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
