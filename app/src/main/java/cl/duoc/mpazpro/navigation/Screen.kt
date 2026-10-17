package cl.duoc.mpazpro.navigation

sealed class Screen(val route: String) {
    data object Inicio : Screen("inicio")

    data object LoginEstudiante : Screen("login_estudiante")
    data object Login : Screen("login")
    data object Evaluacion : Screen("evaluacion")
    data object PerfilEstudiante : Screen("perfil_estudiante")
}