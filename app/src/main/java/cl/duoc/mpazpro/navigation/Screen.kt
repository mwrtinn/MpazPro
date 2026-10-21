package cl.duoc.mpazpro.navigation

sealed class Screen(val route: String) {
    data object Inicio : Screen("inicio")
    data object LoginEstudiante : Screen("login_estudiante")
    data object Login : Screen("login")
    data object Evaluacion : Screen("evaluacion")

    data object PerfilEstudiante : Screen("perfil_estudiante/{rut}") {
        fun createRoute(rut: String) = "perfil_estudiante/$rut"
    }

    data object HomeDocente : Screen("home_docente/{rut}") {
        fun createRoute(rut: String) = "home_docente/$rut"
    }
}