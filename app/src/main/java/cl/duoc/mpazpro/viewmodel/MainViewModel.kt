package cl.duoc.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mpazpro.navigation.NavigationEvent
import cl.duoc.mpazpro.navigation.Screen
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun navegarA(destino: Screen) {
        viewModelScope.launch {
            _navigationEvent.emit(NavigationEvent.NavegarA(destino))
        }
    }

    fun volverAtras() {
        viewModelScope.launch {
            _navigationEvent.emit(NavigationEvent.VolverAtras)
        }
    }
}