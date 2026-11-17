package cl.duoc.mpazpro.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import cl.duoc.mpazpro.model.LoginEvent
import cl.duoc.mpazpro.model.Rol
import cl.duoc.mpazpro.ui.screen.DocenteScreen
import cl.duoc.mpazpro.ui.screen.EstudianteHomeScreen
import cl.duoc.mpazpro.ui.screen.EstudianteObjetivoScreen
import cl.duoc.mpazpro.ui.screen.InicioScreen
import cl.duoc.mpazpro.ui.screen.LoginScreen
import cl.duoc.mpazpro.ui.screen.QuizScreen
import cl.duoc.mpazpro.ui.screen.UtpScreen
import cl.duoc.mpazpro.viewmodel.DocenteViewModel
import cl.duoc.mpazpro.viewmodel.EstudianteViewModel
import cl.duoc.mpazpro.viewmodel.LoginViewModel
import cl.duoc.mpazpro.viewmodel.MainViewModel
import cl.duoc.mpazpro.viewmodel.UtpViewModel
import cl.duoc.mpazpro.viewmodel.ViewModelFactory

/**
 * Grafo de navegación de la app.
 *
 * Toda la navegación pasa por el [MainViewModel]: las pantallas piden ir a otro destino y el
 * ViewModel emite un [NavigationEvent]; aquí se escuchan esos eventos y se ejecutan sobre el
 * [NavHostController] (patrón de la Guía 10).
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    factory: ViewModelFactory
) {
    // Fuera del NavHost: estos ViewModels viven mientras viva la Activity
    val mainViewModel: MainViewModel = viewModel(factory = factory)
    val loginViewModel: LoginViewModel = viewModel(factory = factory)
    val loginState by loginViewModel.state.collectAsState()

    // 1) Eventos de navegación emitidos por el MainViewModel
    LaunchedEffect(Unit) {
        mainViewModel.navigationEvents.collect { evento ->
            when (evento) {
                is NavigationEvent.NavigateTo -> navController.navigate(evento.route.buildRoute()) {
                    evento.popUpToRoute?.let { destino ->
                        popUpTo(destino.route) { inclusive = evento.inclusive }
                    }
                    launchSingleTop = evento.singleTop
                }
                is NavigationEvent.PopBackStack -> navController.popBackStack()
                is NavigationEvent.NavigateUp -> navController.navigateUp()
            }
        }
    }

    // 2) Acceso correcto: según el perfil se navega a su panel
    LaunchedEffect(Unit) {
        loginViewModel.eventos.collect { evento ->
            when (evento) {
                is LoginEvent.Autenticado -> {
                    val destino = when (evento.rol) {
                        Rol.ESTUDIANTE -> Screen.EstudianteHome(evento.rut)
                        Rol.DOCENTE -> Screen.Docente(evento.rut)
                        else -> Screen.Utp(evento.rut)
                    }
                    mainViewModel.navigateTo(destino, popUpToRoute = Screen.Inicio)
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Inicio.route,
        enterTransition = { slideInHorizontally(tween(300)) { it / 4 } + fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(300)) },
        popExitTransition = { slideOutHorizontally(tween(300)) { it / 4 } + fadeOut(tween(300)) }
    ) {
        composable(Screen.Inicio.route) {
            InicioScreen(onSeleccionarRol = { rol -> mainViewModel.navigateTo(Screen.Login(rol)) })
        }

        composable(
            route = Screen.Login.ROUTE,
            arguments = listOf(navArgument("rol") { type = NavType.StringType })
        ) { entry ->
            val rol = entry.arguments?.getString("rol") ?: Rol.ESTUDIANTE
            LaunchedEffect(rol) { loginViewModel.limpiar() }
            LoginScreen(
                rol = rol,
                state = loginState,
                onRutChange = loginViewModel::onRutChanged,
                onContrasenaChange = loginViewModel::onContrasenaChanged,
                onRecordarChange = loginViewModel::onRecordarRutChanged,
                onIngresar = { loginViewModel.ingresar(rol) },
                onVolver = mainViewModel::navigateBack
            )
        }

        // ---------- Estudiante: las tres pantallas comparten el mismo ViewModel ----------
        composable(
            route = Screen.EstudianteHome.ROUTE,
            arguments = listOf(navArgument("rut") { type = NavType.StringType })
        ) { entry ->
            val rut = entry.arguments?.getString("rut").orEmpty()
            val vm: EstudianteViewModel = viewModel(factory = factory)
            EstudianteHomeScreen(
                rut = rut,
                viewModel = vm,
                onAbrirObjetivo = { oaId -> mainViewModel.navigateTo(Screen.EstudianteObjetivo(rut, oaId)) },
                onLogout = mainViewModel::cerrarSesion
            )
        }

        composable(
            route = Screen.EstudianteObjetivo.ROUTE,
            arguments = listOf(
                navArgument("rut") { type = NavType.StringType },
                navArgument("oaId") { type = NavType.IntType }
            )
        ) { entry ->
            val rut = entry.arguments?.getString("rut").orEmpty()
            val oaId = entry.arguments?.getInt("oaId") ?: 0
            val padre = remember(entry) { navController.getBackStackEntry(Screen.EstudianteHome.ROUTE) }
            val vm: EstudianteViewModel = viewModel(viewModelStoreOwner = padre, factory = factory)
            EstudianteObjetivoScreen(
                oaId = oaId,
                viewModel = vm,
                onIniciarQuiz = { tipo -> mainViewModel.navigateTo(Screen.Quiz(rut, oaId, tipo)) },
                onVolver = mainViewModel::navigateBack
            )
        }

        composable(
            route = Screen.Quiz.ROUTE,
            arguments = listOf(
                navArgument("rut") { type = NavType.StringType },
                navArgument("oaId") { type = NavType.IntType },
                navArgument("tipo") { type = NavType.StringType }
            )
        ) { entry ->
            val oaId = entry.arguments?.getInt("oaId") ?: 0
            val tipo = entry.arguments?.getString("tipo").orEmpty()
            val padre = remember(entry) { navController.getBackStackEntry(Screen.EstudianteHome.ROUTE) }
            val vm: EstudianteViewModel = viewModel(viewModelStoreOwner = padre, factory = factory)
            QuizScreen(oaId = oaId, tipo = tipo, viewModel = vm, onVolver = mainViewModel::navigateBack)
        }

        // ---------- Docente y UTP ----------
        composable(
            route = Screen.Docente.ROUTE,
            arguments = listOf(navArgument("rut") { type = NavType.StringType })
        ) {
            val vm: DocenteViewModel = viewModel(factory = factory)
            DocenteScreen(viewModel = vm, onLogout = mainViewModel::cerrarSesion)
        }

        composable(
            route = Screen.Utp.ROUTE,
            arguments = listOf(navArgument("rut") { type = NavType.StringType })
        ) {
            val vm: UtpViewModel = viewModel(factory = factory)
            UtpScreen(viewModel = vm, onLogout = mainViewModel::cerrarSesion)
        }
    }
}
