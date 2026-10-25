package cl.duoc.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mpazpro.data.dao.DesafioDao
import cl.duoc.mpazpro.data.entity.DesafioEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DocenteFormState(
    val rutAlumno: String = "",
    val rutAlumnoError: String? = null,
    val titulo: String = "",
    val tituloError: String? = null,
    val descripcion: String = "",
    val limiteIntentos: String = "3",
    val puntajeMaximo: String = "100",
    val informePedagogico: String = "",
    val isSuccess: Boolean = false
)

class DocenteViewModel(
    private val desafioDao: DesafioDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocenteFormState())
    val uiState: StateFlow<DocenteFormState> = _uiState.asStateFlow()

    fun onRutChanged(valuer: String) { _uiState.value = _uiState.value.copy(rutAlumno = valuer, rutAlumnoError = null) }
    fun onTituloChanged(valuer: String) { _uiState.value = _uiState.value.copy(titulo = valuer, tituloError = null) }
    fun onDescripcionChanged(valuer: String) { _uiState.value = _uiState.value.copy(descripcion = valuer) }
    fun onLimiteIntentosChanged(valuer: String) { _uiState.value = _uiState.value.copy(limiteIntentos = valuer) }
    fun onInformeChanged(valuer: String) { _uiState.value = _uiState.value.copy(informePedagogico = valuer) }

    fun crearDesafio() {
        val state = _uiState.value
        if (state.rutAlumno.isBlank()) {
            _uiState.value = state.copy(rutAlumnoError = "El RUT es obligatorio")
            return
        }
        if (state.titulo.isBlank()) {
            _uiState.value = state.copy(tituloError = "El título es obligatorio")
            return
        }

        viewModelScope.launch {
            desafioDao.insertarDesafio(
                DesafioEntity(
                    titulo = state.titulo.trim(),
                    descripcion = state.descripcion.trim(),
                    limiteIntentos = state.limiteIntentos.toIntOrNull() ?: 3,
                    puntajeMaximo = state.puntajeMaximo.toIntOrNull() ?: 100,
                    rutAlumno = state.rutAlumno.trim(),
                    informePedagogico = state.informePedagogico.trim()
                )
            )
            _uiState.value = DocenteFormState(isSuccess = true)
        }
    }
}