package cl.duoc.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mpazpro.data.remote.SesionRemota
import cl.duoc.mpazpro.navigation.NavigationEvent
import cl.duoc.mpazpro.navigation.Screen
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

// Centraliza la navegación y el cierre de sesión.
class MainViewModel : ViewModel() {

    private val _navigationEvents =
        MutableSharedFlow<NavigationEvent>(
            extraBufferCapacity = 8
        )

    val navigationEvents: SharedFlow<NavigationEvent> =
        _navigationEvents.asSharedFlow()

    // Navega hacia una pantalla.
    fun navigateTo(
        screen: Screen,
        popUpToRoute: Screen? = null,
        inclusive: Boolean = false,
        singleTop: Boolean = false
    ) {
        viewModelScope.launch {

            _navigationEvents.emit(
                NavigationEvent.NavigateTo(
                    screen,
                    popUpToRoute,
                    inclusive,
                    singleTop
                )
            )
        }
    }

    // Regresa a la pantalla anterior.
    fun navigateBack() {
        viewModelScope.launch {
            _navigationEvents.emit(
                NavigationEvent.PopBackStack
            )
        }
    }

    fun navigateUp() {
        viewModelScope.launch {
            _navigationEvents.emit(
                NavigationEvent.NavigateUp
            )
        }
    }

    // Elimina el JWT y regresa a la pantalla de inicio.
    fun cerrarSesion() {

        SesionRemota.cerrar()

        navigateTo(
            Screen.Inicio,
            popUpToRoute = Screen.Inicio,
            inclusive = true
        )
    }
}