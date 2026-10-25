package cl.duoc.mpazpro.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import cl.duoc.mpazpro.ui.screen.*
import cl.duoc.mpazpro.viewmodel.AlumnoViewModel
import cl.duoc.mpazpro.viewmodel.DocenteViewModel
import cl.duoc.mpazpro.viewmodel.MainViewModel
import cl.duoc.mpazpro.viewmodel.ViewModelFactory

@Composable
fun AppNavigation(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    factory: ViewModelFactory
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Inicio.route
    ) {
        // Pantalla de Inicio
        composable(Screen.Inicio.route) {
            InicioScreen(
                onNavegarEstudiante = { navController.navigate(Screen.LoginEstudiante.route) },
                onNavegarDocente = { navController.navigate(Screen.Login.route) }
            )
        }

        // Login de Estudiante
        composable(Screen.LoginEstudiante.route) {
            LoginEstudianteScreen(
                onLoginClick = { rutIngresado ->
                    navController.navigate(Screen.PerfilEstudiante.createRoute(rutIngresado))
                }
            )
        }

        // Login de Docente
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginClick = { usuario, clave ->
                    mainViewModel.validarLoginDocente(usuario, clave) { exito ->
                        if (exito) {
                            navController.navigate(Screen.HomeDocente.createRoute(usuario))
                        }
                    }
                }
            )
        }

        // Perfil / Portal Alumno
        composable(
            route = Screen.PerfilEstudiante.route,
            arguments = listOf(navArgument("rut") { type = NavType.StringType })
        ) { backStackEntry ->
            val rut = backStackEntry.arguments?.getString("rut") ?: ""
            val alumnoViewModel: AlumnoViewModel = viewModel(factory = factory)

            AlumnoScreen(
                rutAlumno = rut,
                viewModel = alumnoViewModel,
                onLogout = {
                    navController.navigate(Screen.Inicio.route) {
                        popUpTo(Screen.Inicio.route) { inclusive = true }
                    }
                }
            )
        }

        // Home / Panel Docente
        composable(
            route = Screen.HomeDocente.route,
            arguments = listOf(navArgument("rut") { type = NavType.StringType })
        ) { backStackEntry ->
            val rut = backStackEntry.arguments?.getString("rut") ?: ""
            val docenteViewModel: DocenteViewModel = viewModel(factory = factory)

            DocenteScreen(
                rutDocente = rut,
                viewModel = docenteViewModel,
                onLogout = {
                    navController.navigate(Screen.Inicio.route) {
                        popUpTo(Screen.Inicio.route) { inclusive = true }
                    }
                }
            )
        }
    }
}