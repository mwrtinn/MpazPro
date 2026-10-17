package cl.duoc.mpazpro.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mpazpro.data.AppDatabase
import cl.duoc.mpazpro.data.entity.DocenteEntity
import cl.duoc.mpazpro.data.entity.EstudianteEntity
import cl.duoc.mpazpro.navigation.NavigationEvent
import cl.duoc.mpazpro.navigation.Screen
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).docenteDao()
    private val estudianteDao = AppDatabase.getDatabase(application).estudianteDao()

    init {
        inicializarDocentes()
        inicializarEstudiantes()
    }


    //LOGIN DOCENTE

    fun validarLogin(usuarioInput: String, contrasenaInput: String, onResultado: (Boolean) -> Unit) {
        viewModelScope.launch {
            val docente = dao.login(usuarioInput, contrasenaInput)
            if (docente != null) {
                _navigationEvent.emit(NavigationEvent.NavegarA(Screen.Inicio))
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

    //LOGIN ALUMNO
    fun validarLoginEstudiante(rutInput: String, contrasenaInput: String, onResultado: (Boolean) -> Unit) {
        viewModelScope.launch {
            val estudiante = estudianteDao.login(rutInput, contrasenaInput)
            if (estudiante != null) {
                _navigationEvent.emit(NavigationEvent.NavegarA(Screen.Evaluacion))
                onResultado(true)
            } else {
                onResultado(false)
            }
        }
    }

    private fun inicializarEstudiantes() {
        viewModelScope.launch {
            val cantidad = estudianteDao.contarEstudiantes()
            if (cantidad == 0) {
                estudianteDao.insertarEstudiante(
                    EstudianteEntity(rut = "111111111", contrasena = "1234", nombreCompleto = "Alumno Prueba 1")
                )
                estudianteDao.insertarEstudiante(
                    EstudianteEntity(rut = "222222222", contrasena = "1234", nombreCompleto = "Alumno Prueba 2")
                )
            }
        }
    }


    //NAV
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