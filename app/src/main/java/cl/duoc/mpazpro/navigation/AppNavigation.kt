package cl.duoc.mpazpro.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import cl.duoc.mpazpro.data.AppDatabase
import cl.duoc.mpazpro.ui.screen.InicioScreen
import cl.duoc.mpazpro.ui.screen.LoginScreen
import cl.duoc.mpazpro.ui.screen.LoginEstudianteScreen
import cl.duoc.mpazpro.ui.screen.DocenteScreen
import cl.duoc.mpazpro.ui.screen.AlumnoScreen
import cl.duoc.mpazpro.viewmodel.MainViewModel
import cl.duoc.mpazpro.viewmodel.DocenteViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    mainViewModel: MainViewModel
) {
    LaunchedEffect(key1 = true) {
        mainViewModel.navigationEvent.collect { evento ->
            when (evento) {
                is NavigationEvent.NavegarA -> {
                    navController.navigate(evento.destino.route)
                }
                is NavigationEvent.VolverAtras -> {
                    navController.popBackStack()
                }
            }
        }
    }

    // RUTAS
    NavHost(
        navController = navController,
        startDestination = Screen.Inicio.route
    ) {

        // PANTALLAS DE INICIO Y ACCESO
        composable(Screen.Inicio.route) {
            InicioScreen(mainViewModel = mainViewModel)
        }

        composable(Screen.LoginEstudiante.route) {
            LoginEstudianteScreen(mainViewModel)
        }

        composable(Screen.Login.route) {
            LoginScreen(mainViewModel)
        }

        // PANTALLAS FUTURAS
        composable(Screen.Evaluacion.route) {
            // Implementación futura
        }

        // PANTALLA ALUMNO (PerfilEstudiante)
        composable(
            route = Screen.PerfilEstudiante.route,
            arguments = listOf(navArgument("rut") { type = NavType.StringType })
        ) { backStackEntry ->
            val rut = backStackEntry.arguments?.getString("rut") ?: ""
            val context = LocalContext.current
            val db = AppDatabase.getDatabase(context)

            AlumnoScreen(
                rutAlumno = rut,
                registroDao = db.registroActividadDao(),
                onLogout = {
                    navController.navigate(Screen.Inicio.route) {
                        popUpTo(Screen.Inicio.route) { inclusive = true }
                    }
                }
            )
        }

        // PANTALLA DOCENTE
        composable(
            route = Screen.HomeDocente.route,
            arguments = listOf(navArgument("rut") { type = NavType.StringType })
        ) { backStackEntry ->
            val rut = backStackEntry.arguments?.getString("rut") ?: ""
            val context = LocalContext.current
            val db = AppDatabase.getDatabase(context)

            val viewModel: DocenteViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return DocenteViewModel(db.registroActividadDao()) as T
                    }
                }
            )

            DocenteScreen(
                rutDocente = rut,
                viewModel = viewModel,
                onLogout = {
                    navController.navigate(Screen.Inicio.route) {
                        popUpTo(Screen.Inicio.route) { inclusive = true }
                    }
                }
            )
        }
    }
}