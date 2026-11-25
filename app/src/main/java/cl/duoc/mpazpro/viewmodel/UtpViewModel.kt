package cl.duoc.mpazpro.viewmodel

import cl.duoc.mpazpro.data.local.entity.ContenidoEntity
import cl.duoc.mpazpro.model.ContenidoRevision
import cl.duoc.mpazpro.model.EstadoContenido
import cl.duoc.mpazpro.model.RevisionState
import cl.duoc.mpazpro.repository.AulaRemotaRepository
import cl.duoc.mpazpro.utils.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Gestiona la revisión de contenidos mediante el microservicio.
class UtpViewModel(
    private val repository: AulaRemotaRepository
) : OperacionRemotaViewModel() {

    private val _contenidos =
        MutableStateFlow<List<ContenidoRevision>>(emptyList())

    val contenidos = _contenidos.asStateFlow()

    // Recupera los contenidos junto a sus objetivos.
    fun recargar() = ejecutar {

        val objetivos = repository.objetivos()
        val contenidosRemotos = repository.contenidos()

        _contenidos.value = contenidosRemotos.map { contenido ->

            ContenidoRevision(
                contenido,
                objetivos.firstOrNull {
                    it.id == contenido.oaId
                }
            )
        }
    }

    private fun actualizarRevision(
        contenido: ContenidoEntity
    ) {

        _contenidos.update { lista ->

            lista.map { revision ->

                if (revision.contenido.id == contenido.id)
                    revision.copy(contenido = contenido)
                else
                    revision
            }
        }
    }

    private val _revision =
        MutableStateFlow(RevisionState())

    val revision: StateFlow<RevisionState> =
        _revision.asStateFlow()

    // Aprueba un contenido directamente en Spring Boot.
    fun aprobar(contenido: ContenidoEntity) {

        ejecutar {

            val actualizado = repository.revisarContenido(
                contenido.id,
                EstadoContenido.APROBADO,
                ""
            )

            actualizarRevision(actualizado)
        }
    }

    fun abrirRechazo(contenido: ContenidoEntity) {

        _revision.value = RevisionState(contenido)
    }

    fun onObservacionChanged(valor: String) {

        _revision.update {

            it.copy(
                observacion = valor,
                error = Validaciones.validarObservacion(valor)
            )
        }
    }

    fun cerrarRechazo() {

        _revision.value = RevisionState()
    }

    // Rechazar requiere una observación para el docente.
    fun confirmarRechazo() {

        val r = _revision.value
        val contenido = r.contenido ?: return

        val error =
            Validaciones.validarObservacion(r.observacion)

        if (error != null) {

            _revision.update {
                it.copy(error = error)
            }

            return
        }

        ejecutar {

            val actualizado = repository.revisarContenido(
                contenido.id,
                EstadoContenido.RECHAZADO,
                r.observacion.trim()
            )

            actualizarRevision(actualizado)

            _revision.value = RevisionState()
        }
    }

    // Carga los contenidos al ingresar al panel UTP.
    init {
        recargar()
    }
}