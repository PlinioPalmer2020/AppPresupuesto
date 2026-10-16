package com.pliniopalmer.presupuesto.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pliniopalmer.presupuesto.data.local.entity.UsuarioEntity

@Composable
fun UserSelectorBar(
    usuarios: List<UsuarioEntity>,
    activeUsuario: UsuarioEntity?,
    onSelectUsuario: (Long) -> Unit,
    onCreateUsuarioClick: () -> Unit,
    onManageIncomeClick: () -> Unit,
    onReportsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Row(
                    modifier = Modifier
                        .clickable { expanded = true }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Perfil",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = activeUsuario?.nombre ?: "Seleccionar Perfil",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Menu perfiles"
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    usuarios.forEach { user ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = user.nombre,
                                    fontWeight = if (user.id == activeUsuario?.id) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                expanded = false
                                onSelectUsuario(user.id)
                            }
                        )
                    }

                    if (usuarios.isNotEmpty()) {
                        HorizontalDivider()
                    }

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Nuevo Perfil")
                            }
                        },
                        onClick = {
                            expanded = false
                            onCreateUsuarioClick()
                        }
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onReportsClick
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Assessment,
                        contentDescription = "Reportes",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                TextButton(
                    onClick = onManageIncomeClick
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = "Ingresos",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Ingresos",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
