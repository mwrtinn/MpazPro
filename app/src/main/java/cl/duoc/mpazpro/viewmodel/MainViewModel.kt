package cl.duoc.mpazpro.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mpazpro.data.AppDatabase
import cl.duoc.mpazpro.data.entity.DocenteEntity
import cl.duoc.mpazpro.navigation.NavigationEvent
import cl.duoc.mpazpro.navigation.Screen
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).docenteDao()

    init {
        inicializarDocentes()
    }

    fun validarLogin(usuarioInput: String, contrasenaInput: String, onResultado: (Boolean) -> Unit) {
        viewModelScope.launch {
            val docente = dao.login(usuarioInput, contrasenaInput)
            if (docente != null) {
                _navigationEvent.emit(NavigationEvent.NavegarA(Screen.Inicio)) // O la pantalla que corresponda
                onResultado(true)
            } else {
                onResultado(false)
            }
        }
    }
    private fun inicializarDocentes() {
        viewModelScope.launch {
            val cantidad = dao.contarDocentes()
            if (cantidad == 0) {
                dao.insertarDocente(
                    DocenteEntity(usuario = "profe1", contrasena = "1234", nombreCompleto = "Profesor Juan Pérez")
                )
                dao.insertarDocente(
                    DocenteEntity(usuario = "profe2", contrasena = "4321", nombreCompleto = "Profesora María Gómez")
                )
            }
        }
    }

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