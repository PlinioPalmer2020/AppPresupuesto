package com.pliniopalmer.presupuesto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pliniopalmer.presupuesto.data.local.AppDatabase
import com.pliniopalmer.presupuesto.data.local.UserPreferences
import com.pliniopalmer.presupuesto.data.repository.PresupuestoRepository
import com.pliniopalmer.presupuesto.ui.screens.CategoryDetailScreen
import com.pliniopalmer.presupuesto.ui.screens.CreateInitialUserScreen
import com.pliniopalmer.presupuesto.ui.screens.DashboardScreen
import com.pliniopalmer.presupuesto.ui.theme.PresupuestoTheme
import com.pliniopalmer.presupuesto.ui.viewmodel.MainViewModel
import com.pliniopalmer.presupuesto.ui.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {

    private val database by lazy { AppDatabase.getDatabase(applicationContext) }
    private val userPreferences by lazy { UserPreferences(applicationContext) }
    private val repository by lazy { PresupuestoRepository(database, userPreferences) }

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PresupuestoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PresupuestoApp(viewModel = mainViewModel)
                }
            }
        }
    }
}

@Composable
fun PresupuestoApp(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val navController = rememberNavController()

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        val startDestination = if (uiState.usuarios.isEmpty()) {
            "initial_setup"
        } else {
            "dashboard"
        }

        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable("initial_setup") {
                CreateInitialUserScreen(
                    onCreateUser = { nombre ->
                        viewModel.createUsuario(nombre)
                        navController.navigate("dashboard") {
                            popUpTo("initial_setup") { inclusive = true }
                        }
                    }
                )
            }

            composable("dashboard") {
                DashboardScreen(
                    viewModel = viewModel,
                    onCategoryClick = { categoriaId ->
                        navController.navigate("category_detail/$categoriaId")
                    },
                    onReportsClick = {
                        navController.navigate("reports")
                    }
                )
            }

            composable(
                route = "category_detail/{categoriaId}",
                arguments = listOf(
                    navArgument("categoriaId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val categoriaId = backStackEntry.arguments?.getLong("categoriaId") ?: 0L
                CategoryDetailScreen(
                    categoriaId = categoriaId,
                    viewModel = viewModel,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable("reports") {
                com.pliniopalmer.presupuesto.ui.screens.ReportScreen(
                    viewModel = viewModel,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
