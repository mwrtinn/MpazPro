package cl.duoc.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mpazpro.data.dao.RegistroActividadDao
import cl.duoc.mpazpro.data.entity.RegistroActividadEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DocenteFormState(
    val rutAlumno: String = "",
    val rutAlumnoError: String? = null,
    val asignatura: String = "",
    val asignaturaError: String? = null,
    val observacion: String = "",
    val observacionError: String? = null,
    val estado: String = "Presente",
    val isSuccess: Boolean = false
)

class DocenteViewModel(
    private val registroDao: RegistroActividadDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocenteFormState())
    val uiState: StateFlow<DocenteFormState> = _uiState.asStateFlow()

    fun onRutChanged(value: String) {
        val error = if (value.isBlank()) "El RUT es obligatorio" else null
        _uiState.value = _uiState.value.copy(rutAlumno = value, rutAlumnoError = error, isSuccess = false)
    }

    fun onAsignaturaChanged(value: String) {
        val error = if (value.isBlank()) "La asignatura no puede estar vacía" else null
        _uiState.value = _uiState.value.copy(asignatura = value, asignaturaError = error, isSuccess = false)
    }

    fun onObservacionChanged(value: String) {
        val error = if (value.length < 5) "Mínimo 5 caracteres" else null
        _uiState.value = _uiState.value.copy(observacion = value, observacionError = error, isSuccess = false)
    }

    fun onEstadoChanged(value: String) {
        _uiState.value = _uiState.value.copy(estado = value)
    }

    fun registrarActividad() {
        val current = _uiState.value
        val rutErr = if (current.rutAlumno.isBlank()) "Campo obligatorio" else null
        val asigErr = if (current.asignatura.isBlank()) "Campo obligatorio" else null
        val obsErr = if (current.observacion.length < 5) "Mínimo 5 caracteres" else null

        if (rutErr != null || asigErr != null || obsErr != null) {
            _uiState.value = current.copy(
                rutAlumnoError = rutErr,
                asignaturaError = asigErr,
                observacionError = obsErr
            )
            return
        }

        viewModelScope.launch {
            val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            registroDao.insertarRegistro(
                RegistroActividadEntity(
                    rutEstudiante = current.rutAlumno.trim(),
                    asignatura = current.asignatura.trim(),
                    observacion = current.observacion.trim(),
                    fecha = fechaActual,
                    estado = current.estado
                )
            )
            _uiState.value = DocenteFormState(isSuccess = true)
        }
    }
}