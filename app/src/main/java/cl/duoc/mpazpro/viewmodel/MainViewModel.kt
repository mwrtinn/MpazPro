package cl.duoc.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mpazpro.navigation.NavigationEvent
import cl.duoc.mpazpro.navigation.Screen
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * ViewModel principal: centraliza la **navegación** de la app (patrón de la Guía 10).
 *
 * Las pantallas no manipulan el NavController: piden la navegación al ViewModel, que emite un
 * [NavigationEvent]. La capa de navegación ([cl.duoc.mpazpro.navigation.AppNavigation]) escucha esos
 * eventos y los ejecuta. Así la lógica de "a dónde ir" queda separada de la interfaz.
 */
class MainViewModel : ViewModel() {

    // Canal interno: solo el ViewModel puede emitir eventos
    private val _navigationEvents = MutableSharedFlow<NavigationEvent>(extraBufferCapacity = 8)

    /** Flujo de solo lectura que observa la UI. */
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()

    /** Navega a [screen]. */
    fun navigateTo(
        screen: Screen,
        popUpToRoute: Screen? = null,
        inclusive: Boolean = false,
        singleTop: Boolean = false
    ) {
        viewModelScope.launch {
            _navigationEvents.emit(NavigationEvent.NavigateTo(screen, popUpToRoute, inclusive, singleTop))
        }
    }

    /** Vuelve a la pantalla anterior. */
    fun navigateBack() {
        viewModelScope.launch { _navigationEvents.emit(NavigationEvent.PopBackStack) }
    }

    /** Navega hacia arriba en la jerarquía. */
    fun navigateUp() {
        viewModelScope.launch { _navigationEvents.emit(NavigationEvent.NavigateUp) }
    }

    /** Cierra la sesión: vuelve al inicio y limpia toda la pila de navegación. */
    fun cerrarSesion() = navigateTo(Screen.Inicio, popUpToRoute = Screen.Inicio, inclusive = true)
}
