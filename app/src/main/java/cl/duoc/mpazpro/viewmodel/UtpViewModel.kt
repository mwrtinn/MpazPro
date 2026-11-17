package cl.duoc.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mpazpro.data.local.entity.ContenidoEntity
import cl.duoc.mpazpro.model.ContenidoRevision
import cl.duoc.mpazpro.model.EstadoContenido
import cl.duoc.mpazpro.model.RevisionState
import cl.duoc.mpazpro.repository.AulaRepository
import cl.duoc.mpazpro.utils.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** La jefatura de UTP revisa y autoriza los contenidos antes de que los vean los estudiantes. */
class UtpViewModel(private val repository: AulaRepository) : ViewModel() {

    /** Contenidos junto con su objetivo de aprendizaje. */
    val contenidos: StateFlow<List<ContenidoRevision>> = combine(
        repository.contenidos(), repository.objetivos()
    ) { contenidos, objetivos ->
        contenidos.map { c -> ContenidoRevision(c, objetivos.firstOrNull { it.id == c.oaId }) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _revision = MutableStateFlow(RevisionState())
    val revision: StateFlow<RevisionState> = _revision.asStateFlow()

    fun aprobar(contenido: ContenidoEntity) {
        viewModelScope.launch {
            repository.actualizarContenido(contenido.copy(estado = EstadoContenido.APROBADO, observacionUtp = ""))
        }
    }

    fun abrirRechazo(contenido: ContenidoEntity) {
        _revision.value = RevisionState(contenido)
    }

    fun onObservacionChanged(valor: String) {
        _revision.update { it.copy(observacion = valor, error = Validaciones.validarObservacion(valor)) }
    }

    fun cerrarRechazo() {
        _revision.value = RevisionState()
    }

    /** Rechazar exige una observación para que el docente sepa qué corregir. */
    fun confirmarRechazo() {
        val r = _revision.value
        val contenido = r.contenido ?: return
        val error = Validaciones.validarObservacion(r.observacion)
        if (error != null) {
            _revision.update { it.copy(error = error) }
            return
        }
        viewModelScope.launch {
            repository.actualizarContenido(contenido.copy(estado = EstadoContenido.RECHAZADO, observacionUtp = r.observacion.trim()))
            _revision.value = RevisionState()
        }
    }
}
