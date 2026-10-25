package cl.duoc.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mpazpro.data.dao.DocenteDao
import cl.duoc.mpazpro.data.dao.EstudianteDao
import cl.duoc.mpazpro.data.entity.DocenteEntity
import cl.duoc.mpazpro.data.entity.EstudianteEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

sealed class MainNavigationEvent {
    data class NavegarAStudent(val rut: String) : MainNavigationEvent()
    data class NavegarADocente(val rut: String) : MainNavigationEvent()
    object ErrorLogin : MainNavigationEvent()
}

class MainViewModel(
    private val docenteDao: DocenteDao,
    private val estudianteDao: EstudianteDao
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<MainNavigationEvent>()
    val navigationEvent: SharedFlow<MainNavigationEvent> = _navigationEvent.asSharedFlow()

    init {
        inicializarDatosBase()
    }

    private fun inicializarDatosBase() {
        viewModelScope.launch {
            if (docenteDao.contarDocentes() == 0) {
                docenteDao.insertarDocente(
                    DocenteEntity(usuario = "profe1", contrasena = "1234", nombreCompleto = "Profesor Juan Pérez")
                )
                docenteDao.insertarDocente(
                    DocenteEntity(usuario = "profe2", contrasena = "4321", nombreCompleto = "Profesora María Gómez")
                )
            }

            if (estudianteDao.contarEstudiantes() == 0) {
                estudianteDao.insertarEstudiante(
                    EstudianteEntity(rut = "111111111", contrasena = "1234", nombreCompleto = "Alumno Prueba 1")
                )
                estudianteDao.insertarEstudiante(
                    EstudianteEntity(rut = "222222222", contrasena = "1234", nombreCompleto = "Alumno Prueba 2")
                )
            }
        }
    }

    fun validarLoginDocente(usuarioInput: String, contrasenaInput: String, onResultado: (Boolean) -> Unit) {
        viewModelScope.launch {
            val docente = docenteDao.login(usuarioInput, contrasenaInput)
            if (docente != null) {
                _navigationEvent.emit(MainNavigationEvent.NavegarADocente(docente.usuario))
                onResultado(true)
            } else {
                _navigationEvent.emit(MainNavigationEvent.ErrorLogin)
                onResultado(false)
            }
        }
    }

    fun validarLoginEstudiante(rutInput: String, contrasenaInput: String, onResultado: (Boolean) -> Unit) {
        viewModelScope.launch {
            val estudiante = estudianteDao.login(rutInput, contrasenaInput)
            if (estudiante != null) {
                _navigationEvent.emit(MainNavigationEvent.NavegarAStudent(estudiante.rut))
                onResultado(true)
            } else {
                _navigationEvent.emit(MainNavigationEvent.ErrorLogin)
                onResultado(false)
            }
        }
    }
}