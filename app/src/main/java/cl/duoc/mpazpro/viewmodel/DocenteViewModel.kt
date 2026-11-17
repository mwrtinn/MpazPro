package cl.duoc.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mpazpro.data.local.entity.ContenidoEntity
import cl.duoc.mpazpro.data.local.entity.InformeEntity
import cl.duoc.mpazpro.data.local.entity.ObjetivoEntity
import cl.duoc.mpazpro.data.local.entity.PreguntaEntity
import cl.duoc.mpazpro.model.ConfigIntentosState
import cl.duoc.mpazpro.model.ContenidoErrores
import cl.duoc.mpazpro.model.ContenidoUiState
import cl.duoc.mpazpro.model.EstadoContenido
import cl.duoc.mpazpro.model.FilaResultado
import cl.duoc.mpazpro.model.PreguntaErrores
import cl.duoc.mpazpro.model.PreguntaUiState
import cl.duoc.mpazpro.repository.AulaRepository
import cl.duoc.mpazpro.utils.GeneradorInforme
import cl.duoc.mpazpro.utils.Validaciones
import cl.duoc.mpazpro.utils.claveInforme
import cl.duoc.mpazpro.utils.fechaActual
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel del perfil docente.
 *
 * Gestiona: asignación de objetivos e intentos de la prueba final, el formulario de contenidos
 * (con imagen de apoyo), el formulario de preguntas y los informes pedagógicos.
 * Toda la validación se hace aquí (con [Validaciones]); la UI solo muestra el estado.
 */
class DocenteViewModel(private val repository: AulaRepository) : ViewModel() {

    private fun <T> Flow<T>.estado(inicial: T): StateFlow<T> =
        stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), inicial)

    val objetivos = repository.objetivos().estado(emptyList())
    val contenidos = repository.contenidos().estado(emptyList())
    val preguntas = repository.preguntas().estado(emptyList())

    /** Un estudiante frente a cada objetivo asignado, con sus respuestas e informe. */
    val filas: StateFlow<List<FilaResultado>> = combine(
        repository.estudiantes(), repository.objetivosAsignados(), repository.respuestas(), repository.informes()
    ) { estudiantes, asignados, respuestas, informes ->
        estudiantes.flatMap { e ->
            asignados.map { oa ->
                FilaResultado(
                    estudiante = e,
                    objetivo = oa,
                    respuestas = respuestas.filter { it.rutAlumno == e.rut && it.oaId == oa.id },
                    informe = informes.firstOrNull { it.clave == claveInforme(e.rut, oa.id) }
                )
            }
        }
    }.estado(emptyList())

    // =========================================================================
    // Objetivos: asignar e intentos de la prueba final
    // =========================================================================

    private val _config = MutableStateFlow(ConfigIntentosState())
    val config: StateFlow<ConfigIntentosState> = _config.asStateFlow()

    fun alternarAsignado(objetivo: ObjetivoEntity, asignado: Boolean) {
        viewModelScope.launch { repository.actualizarObjetivo(objetivo.copy(asignado = asignado)) }
    }

    fun abrirConfig(objetivo: ObjetivoEntity) {
        _config.value = ConfigIntentosState(objetivo, objetivo.intentosPrueba.toString())
    }

    fun onIntentosChanged(valor: String) {
        _config.update { it.copy(valor = valor, error = Validaciones.validarIntentos(valor)) }
    }

    fun cerrarConfig() {
        _config.value = ConfigIntentosState()
    }

    fun guardarConfig() {
        val c = _config.value
        val objetivo = c.objetivo ?: return
        val error = Validaciones.validarIntentos(c.valor)
        if (error != null) {
            _config.update { it.copy(error = error) }
            return
        }
        viewModelScope.launch {
            repository.actualizarObjetivo(objetivo.copy(intentosPrueba = c.valor.trim().toInt()))
            _config.value = ConfigIntentosState()
        }
    }

    // =========================================================================
    // Formulario de contenido (queda PENDIENTE hasta que UTP lo revise)
    // =========================================================================

    private val _contenido = MutableStateFlow(ContenidoUiState())
    val contenido: StateFlow<ContenidoUiState> = _contenido.asStateFlow()

    fun onContenidoOaChanged(oaId: Int) = _contenido.update {
        it.copy(oaId = oaId, errores = it.errores.copy(oa = null), guardado = false)
    }

    fun onTituloChanged(titulo: String) = _contenido.update {
        it.copy(titulo = titulo, errores = it.errores.copy(titulo = Validaciones.validarTitulo(titulo)), guardado = false)
    }

    fun onExplicacionChanged(explicacion: String) = _contenido.update {
        it.copy(explicacion = explicacion, errores = it.errores.copy(explicacion = Validaciones.validarExplicacion(explicacion)), guardado = false)
    }

    fun onEjemploChanged(ejemplo: String) = _contenido.update {
        it.copy(ejemplo = ejemplo, errores = it.errores.copy(ejemplo = Validaciones.validarEjemplo(ejemplo)), guardado = false)
    }

    fun onIconoChanged(icono: String) = _contenido.update { it.copy(icono = icono) }

    /** Recibe la ruta de la imagen tomada con la cámara o elegida en la galería (`null` la quita). */
    fun onImagenChanged(ruta: String?) = _contenido.update { it.copy(imagenPath = ruta, guardado = false) }

    /** Valida todo el formulario de contenido: `true` si es válido; si no, actualiza los errores. */
    fun validarContenido(): Boolean {
        val c = _contenido.value
        val errores = ContenidoErrores(
            oa = if (c.oaId == null) "Selecciona un objetivo" else null,
            titulo = Validaciones.validarTitulo(c.titulo),
            explicacion = Validaciones.validarExplicacion(c.explicacion),
            ejemplo = Validaciones.validarEjemplo(c.ejemplo)
        )
        _contenido.update { it.copy(errores = errores) }
        return errores == ContenidoErrores()
    }

    fun crearContenido() {
        if (!validarContenido()) return
        val c = _contenido.value
        val oaId = c.oaId ?: return
        viewModelScope.launch {
            repository.crearContenido(
                ContenidoEntity(
                    oaId = oaId, titulo = c.titulo.trim(), explicacion = c.explicacion.trim(),
                    ejemplo = c.ejemplo.trim(), icono = c.icono, estado = EstadoContenido.PENDIENTE,
                    imagenPath = c.imagenPath
                )
            )
            _contenido.value = ContenidoUiState(oaId = oaId, guardado = true)
        }
    }

    // =========================================================================
    // Formulario de pregunta
    // =========================================================================

    private val _pregunta = MutableStateFlow(PreguntaUiState())
    val pregunta: StateFlow<PreguntaUiState> = _pregunta.asStateFlow()

    fun onPreguntaOaChanged(oaId: Int) = _pregunta.update {
        it.copy(oaId = oaId, errores = it.errores.copy(oa = null), guardado = false)
    }

    fun onEnunciadoChanged(enunciado: String) = _pregunta.update {
        it.copy(enunciado = enunciado, errores = it.errores.copy(enunciado = Validaciones.validarEnunciado(enunciado)), guardado = false)
    }

    fun onOpcionAChanged(valor: String) = _pregunta.update {
        it.copy(opcionA = valor, errores = it.errores.copy(opcionA = Validaciones.validarAlternativa(valor)), guardado = false)
    }

    fun onOpcionBChanged(valor: String) = _pregunta.update {
        val error = Validaciones.validarAlternativa(valor) ?: Validaciones.alternativaRepetida(valor, listOf(it.opcionA))
        it.copy(opcionB = valor, errores = it.errores.copy(opcionB = error), guardado = false)
    }

    fun onOpcionCChanged(valor: String) = _pregunta.update {
        val error = Validaciones.validarAlternativa(valor) ?: Validaciones.alternativaRepetida(valor, listOf(it.opcionA, it.opcionB))
        it.copy(opcionC = valor, errores = it.errores.copy(opcionC = error), guardado = false)
    }

    fun onTipoChanged(tipo: String) = _pregunta.update { it.copy(tipo = tipo) }
    fun onDificultadChanged(nivel: Int) = _pregunta.update { it.copy(dificultad = nivel) }
    fun onCorrectaChanged(indice: Int) = _pregunta.update { it.copy(correcta = indice) }

    /** Valida todo el formulario de pregunta: `true` si es válido; si no, actualiza los errores. */
    fun validarPregunta(): Boolean {
        val p = _pregunta.value
        val errores = PreguntaErrores(
            oa = if (p.oaId == null) "Selecciona un objetivo" else null,
            enunciado = Validaciones.validarEnunciado(p.enunciado),
            opcionA = Validaciones.validarAlternativa(p.opcionA),
            opcionB = Validaciones.validarAlternativa(p.opcionB) ?: Validaciones.alternativaRepetida(p.opcionB, listOf(p.opcionA)),
            opcionC = Validaciones.validarAlternativa(p.opcionC) ?: Validaciones.alternativaRepetida(p.opcionC, listOf(p.opcionA, p.opcionB))
        )
        _pregunta.update { it.copy(errores = errores) }
        return errores == PreguntaErrores()
    }

    fun crearPregunta() {
        if (!validarPregunta()) return
        val p = _pregunta.value
        val oaId = p.oaId ?: return
        viewModelScope.launch {
            repository.crearPregunta(
                PreguntaEntity(
                    oaId = oaId, tipo = p.tipo, dificultad = p.dificultad, enunciado = p.enunciado.trim(),
                    opcionA = p.opcionA.trim(), opcionB = p.opcionB.trim(), opcionC = p.opcionC.trim(),
                    correcta = p.correcta
                )
            )
            _pregunta.value = PreguntaUiState(oaId = oaId, tipo = p.tipo, dificultad = p.dificultad, guardado = true)
        }
    }

    /** Oculta los mensajes de "guardado" de ambos formularios. */
    fun limpiarMensajes() {
        _contenido.update { it.copy(guardado = false) }
        _pregunta.update { it.copy(guardado = false) }
    }

    // =========================================================================
    // Informes (siempre sujetos a validación docente)
    // =========================================================================

    fun generarInforme(fila: FilaResultado) {
        viewModelScope.launch {
            val r = GeneradorInforme.generar(fila.respuestas)
            repository.guardarInforme(
                InformeEntity(
                    clave = claveInforme(fila.estudiante.rut, fila.objetivo.id), rutAlumno = fila.estudiante.rut,
                    oaId = fila.objetivo.id, rendimiento = r.rendimiento, fortalezas = r.fortalezas,
                    refuerzo = r.refuerzo, fecha = fechaActual(), validado = false
                )
            )
        }
    }

    fun validarInforme(informe: InformeEntity) {
        viewModelScope.launch { repository.guardarInforme(informe.copy(validado = true)) }
    }
}
