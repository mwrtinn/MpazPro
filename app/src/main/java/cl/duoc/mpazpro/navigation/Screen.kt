package cl.duoc.mpazpro.navigation

/**
 * Rutas tipadas de la aplicación (sealed class, como en la Guía 10).
 *
 * - Las pantallas sin argumentos son `data object`.
 * - Las pantallas con argumentos son `data class`: guardan el valor y construyen la ruta final con
 *   [buildRoute], evitando errores al escribir el String a mano.
 * - Cada una expone su patrón en `ROUTE` (con los `{argumentos}`) para declararla en el NavHost.
 */
sealed class Screen(val route: String) {

    /** Construye la ruta final (con los argumentos ya reemplazados). */
    open fun buildRoute(): String = route

    data object Inicio : Screen("inicio_page")

    data class Login(val rol: String = "") : Screen(ROUTE) {
        override fun buildRoute() = ROUTE.replace("{rol}", rol)

        companion object { const val ROUTE = "login_page/{rol}" }
    }

    data class EstudianteHome(val rut: String = "") : Screen(ROUTE) {
        override fun buildRoute() = ROUTE.replace("{rut}", rut)

        companion object { const val ROUTE = "estudiante_page/{rut}" }
    }

    data class EstudianteObjetivo(val rut: String = "", val oaId: Int = 0) : Screen(ROUTE) {
        override fun buildRoute() = ROUTE.replace("{rut}", rut).replace("{oaId}", oaId.toString())

        companion object { const val ROUTE = "estudiante_page/{rut}/objetivo/{oaId}" }
    }

    data class Quiz(val rut: String = "", val oaId: Int = 0, val tipo: String = "") : Screen(ROUTE) {
        override fun buildRoute() =
            ROUTE.replace("{rut}", rut).replace("{oaId}", oaId.toString()).replace("{tipo}", tipo)

        companion object { const val ROUTE = "estudiante_page/{rut}/quiz/{oaId}/{tipo}" }
    }

    data class Docente(val rut: String = "") : Screen(ROUTE) {
        override fun buildRoute() = ROUTE.replace("{rut}", rut)

        companion object { const val ROUTE = "docente_page/{rut}" }
    }

    data class Utp(val rut: String = "") : Screen(ROUTE) {
        override fun buildRoute() = ROUTE.replace("{rut}", rut)

        companion object { const val ROUTE = "utp_page/{rut}" }
    }
}
