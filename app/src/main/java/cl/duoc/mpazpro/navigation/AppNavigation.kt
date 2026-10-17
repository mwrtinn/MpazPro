package cl.duoc.mpazpro.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cl.duoc.mpazpro.ui.screen.InicioScreen
import cl.duoc.mpazpro.ui.screen.LoginScreen
import cl.duoc.mpazpro.ui.screen.LoginEstudianteScreen
import cl.duoc.mpazpro.viewmodel.MainViewModel


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

        }

        composable(Screen.PerfilEstudiante.route) {

        }
    }
}

