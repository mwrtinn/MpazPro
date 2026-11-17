package cl.duoc.mpazpro.navigation

/**
 * Eventos de navegación que emite el [cl.duoc.mpazpro.viewmodel.MainViewModel].
 * La UI los escucha y ejecuta la acción real sobre el NavController (Guía 10).
 */
sealed class NavigationEvent {

    /**
     * Navegar a un destino.
     *
     * @param route Destino (objeto tipado [Screen]).
     * @param popUpToRoute Destino hasta el cual se limpia la pila (o `null` para no limpiar).
     * @param inclusive Si es `true`, [popUpToRoute] también se elimina de la pila.
     * @param singleTop Evita copias repetidas del mismo destino en la cima de la pila.
     */
    data class NavigateTo(
        val route: Screen,
        val popUpToRoute: Screen? = null,
        val inclusive: Boolean = false,
        val singleTop: Boolean = false
    ) : NavigationEvent()

    /** Volver a la pantalla anterior de la pila. */
    data object PopBackStack : NavigationEvent()

    /** Navegar "hacia arriba" en la jerarquía de la aplicación. */
    data object NavigateUp : NavigationEvent()
}
