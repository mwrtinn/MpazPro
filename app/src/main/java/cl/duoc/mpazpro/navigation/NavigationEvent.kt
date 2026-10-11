package cl.duoc.mpazpro.navigation

sealed class NavigationEvent {
    data class NavegarA(val destino: Screen) : NavigationEvent()
    data object VolverAtras : NavigationEvent()
}