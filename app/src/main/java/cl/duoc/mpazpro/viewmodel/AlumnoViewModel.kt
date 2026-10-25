package cl.duoc.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mpazpro.data.dao.DesafioDao
import cl.duoc.mpazpro.data.entity.DesafioEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlumnoViewModel(
    private val desafioDao: DesafioDao
) : ViewModel() {

    private val _desafios = MutableStateFlow<List<DesafioEntity>>(emptyList())
    val desafios: StateFlow<List<DesafioEntity>> = _desafios.asStateFlow()

    fun cargarDesafiosAlumno(rut: String) {
        viewModelScope.launch {
            desafioDao.getDesafiosPorAlumno(rut).collect { lista ->
                _desafios.value = lista
            }
        }
    }

    fun realizarIntento(desafio: DesafioEntity, puntajeLogrado: Int) {
        if (desafio.intentosRealizados < desafio.limiteIntentos) {
            viewModelScope.launch {
                val actualizado = desafio.copy(
                    intentosRealizados = desafio.intentosRealizados + 1,
                    puntajeObtenido = maxOf(desafio.puntajeObtenido, puntajeLogrado)
                )
                desafioDao.actualizarDesafio(actualizado)
            }
        }
    }
}