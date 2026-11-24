package cl.duoc.mpazpro.viewmodel

import cl.duoc.mpazpro.data.local.entity.*
import cl.duoc.mpazpro.model.*
import cl.duoc.mpazpro.repository.AulaRemotaRepository
import cl.duoc.mpazpro.utils.Validaciones
import cl.duoc.mpazpro.utils.claveInforme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Gestiona las operaciones del docente mediante Spring Boot.
class DocenteViewModel(
    private val repository: AulaRemotaRepository
) : OperacionRemotaViewModel() {

    private val _objetivos =
        MutableStateFlow<List<ObjetivoEntity>>(emptyList())
    val objetivos = _objetivos.asStateFlow()

    private val _contenidos =
        MutableStateFlow<List<ContenidoEntity>>(emptyList())
    val contenidos = _contenidos.asStateFlow()

    private val _preguntas =
        MutableStateFlow<List<PreguntaEntity>>(emptyList())
    val preguntas = _preguntas.asStateFlow()

    private val _filas =
        MutableStateFlow<List<FilaResultado>>(emptyList())
    val filas = _filas.asStateFlow()

    private var estudiantes = emptyList<UsuarioEntity>()
    private var respuestas = emptyList<RespuestaEntity>()
    private var informes = emptyList<InformeEntity>()

    // Conserva temporalmente las imágenes locales.
    private val imagenesLocales = mutableMapOf<Int, String>()

    private val _contenidoEditando =
        MutableStateFlow<Int?>(null)
    val contenidoEditando = _contenidoEditando.asStateFlow()

    private val _preguntaEditando =
        MutableStateFlow<Int?>(null)
    val preguntaEditando = _preguntaEditando.asStateFlow()

    // ---------- Carga de datos ----------

    fun recargar() = ejecutar {
        cargarDatos()
    }

    private suspend fun cargarDatos() {

        val nuevosObjetivos = repository.objetivos()
        val nuevosContenidos = repository.contenidos()
        val nuevasPreguntas = repository.preguntas()
        val nuevosEstudiantes = repository.estudiantes()
        val nuevasRespuestas = repository.respuestas()
        val nuevosInformes = repository.informes()

        _objetivos.value = nuevosObjetivos.sortedBy { it.id }

        _contenidos.value = nuevosContenidos.map { contenido ->
            contenido.copy(
                imagenPath = imagenesLocales[contenido.id]
                    ?: contenido.imagenPath
            )
        }.sortedBy { it.id }

        _preguntas.value = nuevasPreguntas.sortedBy { it.id }

        estudiantes = nuevosEstudiantes
        respuestas = nuevasRespuestas
        informes = nuevosInformes

        reconstruirFilas()
    }

    private fun reconstruirFilas() {

        _filas.value = estudiantes.flatMap { estudiante ->

            _objetivos.value.filter { it.asignado }.map { objetivo ->

                FilaResultado(
                    estudiante = estudiante,
                    objetivo = objetivo,
                    respuestas = respuestas.filter {
                        it.rutAlumno == estudiante.rut &&
                                it.oaId == objetivo.id
                    },
                    informe = informes.firstOrNull {
                        it.clave == claveInforme(
                            estudiante.rut,
                            objetivo.id
                        )
                    }
                )
            }
        }
    }

    private fun actualizarObjetivoLocal(objetivo: ObjetivoEntity) {

        _objetivos.update { lista ->
            lista.map {
                if (it.id == objetivo.id) objetivo else it
            }
        }

        reconstruirFilas()
    }

    private fun actualizarInformeLocal(informe: InformeEntity) {

        informes = informes.filterNot {
            it.clave == informe.clave
        } + informe

        reconstruirFilas()
    }

    // ---------- Objetivos e intentos ----------

    private val _config = MutableStateFlow(ConfigIntentosState())
    val config = _config.asStateFlow()

    fun alternarAsignado(
        objetivo: ObjetivoEntity,
        asignado: Boolean
    ) {

        ejecutar {

            val actualizado = repository.actualizarObjetivo(
                objetivo.copy(asignado = asignado)
            )

            actualizarObjetivoLocal(actualizado)
        }
    }

    fun abrirConfig(objetivo: ObjetivoEntity) {

        _config.value = ConfigIntentosState(
            objetivo,
            objetivo.intentosPrueba.toString()
        )
    }

    fun onIntentosChanged(valor: String) {

        _config.update {
            it.copy(
                valor = valor,
                error = Validaciones.validarIntentos(valor)
            )
        }
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

        ejecutar {

            val actualizado = repository.actualizarObjetivo(
                objetivo.copy(
                    intentosPrueba = c.valor.trim().toInt()
                )
            )

            actualizarObjetivoLocal(actualizado)

            _config.value = ConfigIntentosState()
        }
    }

    // ---------- Formulario de contenidos ----------

    private val _contenido = MutableStateFlow(ContenidoUiState())
    val contenido = _contenido.asStateFlow()

    fun onContenidoOaChanged(oaId: Int) = _contenido.update {

        it.copy(
            oaId = oaId,
            errores = it.errores.copy(oa = null),
            guardado = false
        )
    }

    fun onTituloChanged(titulo: String) = _contenido.update {

        it.copy(
            titulo = titulo,
            errores = it.errores.copy(
                titulo = Validaciones.validarTitulo(titulo)
            ),
            guardado = false
        )
    }

    fun onExplicacionChanged(valor: String) = _contenido.update {

        it.copy(
            explicacion = valor,
            errores = it.errores.copy(
                explicacion = Validaciones.validarExplicacion(valor)
            ),
            guardado = false
        )
    }

    fun onEjemploChanged(valor: String) = _contenido.update {

        it.copy(
            ejemplo = valor,
            errores = it.errores.copy(
                ejemplo = Validaciones.validarEjemplo(valor)
            ),
            guardado = false
        )
    }

    fun onIconoChanged(icono: String) = _contenido.update {
        it.copy(icono = icono, guardado = false)
    }

    fun onImagenChanged(ruta: String?) = _contenido.update {
        it.copy(imagenPath = ruta, guardado = false)
    }

    fun validarContenido(): Boolean {

        val c = _contenido.value

        val errores = ContenidoErrores(
            oa = if (c.oaId == null)
                "Selecciona un objetivo" else null,
            titulo = Validaciones.validarTitulo(c.titulo),
            explicacion = Validaciones.validarExplicacion(c.explicacion),
            ejemplo = Validaciones.validarEjemplo(c.ejemplo)
        )

        _contenido.update { it.copy(errores = errores) }

        return errores == ContenidoErrores()
    }

    // Carga un contenido existente en el formulario.
    fun editarContenido(c: ContenidoEntity) {

        if (cargando.value) return

        _contenidoEditando.value = c.id

        _contenido.value = ContenidoUiState(
            oaId = c.oaId,
            titulo = c.titulo,
            explicacion = c.explicacion,
            ejemplo = c.ejemplo,
            icono = c.icono,
            imagenPath = c.imagenPath
        )
    }

    fun cancelarEdicionContenido() {

        if (cargando.value) return

        val oaId = _contenido.value.oaId

        _contenidoEditando.value = null
        _contenido.value = ContenidoUiState(oaId = oaId)
    }

    // Crea o actualiza según el modo del formulario.
    fun crearContenido() {

        if (cargando.value || !validarContenido()) return

        val c = _contenido.value
        val oaId = c.oaId ?: return
        val id = _contenidoEditando.value

        ejecutar {

            val datos = ContenidoEntity(
                id = id ?: 0,
                oaId = oaId,
                titulo = c.titulo.trim(),
                explicacion = c.explicacion.trim(),
                ejemplo = c.ejemplo.trim(),
                icono = c.icono,
                estado = EstadoContenido.PENDIENTE,
                observacionUtp = "",
                imagenPath = c.imagenPath
            )

            val guardado = if (id == null) {
                repository.crearContenido(datos)
            } else {
                repository.actualizarContenido(datos)
            }

            if (c.imagenPath != null) {

                imagenesLocales[guardado.id] = c.imagenPath

            } else {

                imagenesLocales.remove(guardado.id)
            }

            _contenidos.update { lista ->

                (
                        lista.filterNot { it.id == guardado.id } +
                                guardado.copy(imagenPath = c.imagenPath)
                        ).sortedBy { it.id }
            }

            _contenidoEditando.value = null

            _contenido.value = ContenidoUiState(
                oaId = oaId,
                guardado = true
            )
        }
    }

    // Elimina el contenido después de la confirmación.
    fun eliminarContenido(contenido: ContenidoEntity) {

        ejecutar {

            repository.eliminarContenido(contenido.id)

            imagenesLocales.remove(contenido.id)

            _contenidos.update { lista ->
                lista.filterNot { it.id == contenido.id }
            }

            if (_contenidoEditando.value == contenido.id) {

                _contenidoEditando.value = null
                _contenido.value = ContenidoUiState()
            }
        }
    }

    // ---------- Formulario de preguntas ----------

    private val _pregunta = MutableStateFlow(PreguntaUiState())
    val pregunta = _pregunta.asStateFlow()

    fun onPreguntaOaChanged(oaId: Int) = _pregunta.update {

        it.copy(
            oaId = oaId,
            errores = it.errores.copy(oa = null),
            guardado = false
        )
    }

    fun onEnunciadoChanged(valor: String) = _pregunta.update {

        it.copy(
            enunciado = valor,
            errores = it.errores.copy(
                enunciado = Validaciones.validarEnunciado(valor)
            ),
            guardado = false
        )
    }

    fun onOpcionAChanged(valor: String) = _pregunta.update {

        it.copy(
            opcionA = valor,
            errores = it.errores.copy(
                opcionA = Validaciones.validarAlternativa(valor)
            ),
            guardado = false
        )
    }

    fun onOpcionBChanged(valor: String) = _pregunta.update {

        val error = Validaciones.validarAlternativa(valor)
            ?: Validaciones.alternativaRepetida(
                valor,
                listOf(it.opcionA)
            )

        it.copy(
            opcionB = valor,
            errores = it.errores.copy(opcionB = error),
            guardado = false
        )
    }

    fun onOpcionCChanged(valor: String) = _pregunta.update {

        val error = Validaciones.validarAlternativa(valor)
            ?: Validaciones.alternativaRepetida(
                valor,
                listOf(it.opcionA, it.opcionB)
            )

        it.copy(
            opcionC = valor,
            errores = it.errores.copy(opcionC = error),
            guardado = false
        )
    }

    fun onTipoChanged(tipo: String) = _pregunta.update {
        it.copy(tipo = tipo, guardado = false)
    }

    fun onDificultadChanged(nivel: Int) = _pregunta.update {
        it.copy(dificultad = nivel, guardado = false)
    }

    fun onCorrectaChanged(indice: Int) = _pregunta.update {
        it.copy(correcta = indice, guardado = false)
    }

    fun validarPregunta(): Boolean {

        val p = _pregunta.value

        val errores = PreguntaErrores(
            oa = if (p.oaId == null)
                "Selecciona un objetivo" else null,

            enunciado = Validaciones.validarEnunciado(p.enunciado),

            opcionA = Validaciones.validarAlternativa(p.opcionA),

            opcionB = Validaciones.validarAlternativa(p.opcionB)
                ?: Validaciones.alternativaRepetida(
                    p.opcionB,
                    listOf(p.opcionA)
                ),

            opcionC = Validaciones.validarAlternativa(p.opcionC)
                ?: Validaciones.alternativaRepetida(
                    p.opcionC,
                    listOf(p.opcionA, p.opcionB)
                )
        )

        _pregunta.update { it.copy(errores = errores) }

        return errores == PreguntaErrores()
    }

    // Carga una pregunta existente para editarla.
    fun editarPregunta(p: PreguntaEntity) {

        if (cargando.value) return

        _preguntaEditando.value = p.id

        _pregunta.value = PreguntaUiState(
            oaId = p.oaId,
            tipo = p.tipo,
            dificultad = p.dificultad,
            enunciado = p.enunciado,
            opcionA = p.opcionA,
            opcionB = p.opcionB,
            opcionC = p.opcionC,
            correcta = p.correcta
        )
    }

    fun cancelarEdicionPregunta() {

        if (cargando.value) return

        val oaId = _pregunta.value.oaId

        _preguntaEditando.value = null
        _pregunta.value = PreguntaUiState(oaId = oaId)
    }

    // Crea o actualiza una pregunta en el servidor.
    fun crearPregunta() {

        if (cargando.value || !validarPregunta()) return

        val p = _pregunta.value
        val oaId = p.oaId ?: return
        val id = _preguntaEditando.value

        ejecutar {

            val datos = PreguntaEntity(
                id = id ?: 0,
                oaId = oaId,
                tipo = p.tipo,
                dificultad = p.dificultad,
                enunciado = p.enunciado.trim(),
                opcionA = p.opcionA.trim(),
                opcionB = p.opcionB.trim(),
                opcionC = p.opcionC.trim(),
                correcta = p.correcta
            )

            val guardada = if (id == null) {

                repository.crearPregunta(datos)

            } else {

                repository.actualizarPregunta(datos)
            }

            _preguntas.update { lista ->

                (
                        lista.filterNot { it.id == guardada.id } +
                                guardada
                        ).sortedBy { it.id }
            }

            _preguntaEditando.value = null

            _pregunta.value = PreguntaUiState(
                oaId = oaId,
                tipo = p.tipo,
                dificultad = p.dificultad,
                guardado = true
            )
        }
    }

    // El servidor protege las preguntas ya respondidas.
    fun eliminarPregunta(pregunta: PreguntaEntity) {

        ejecutar {

            repository.eliminarPregunta(pregunta.id)

            _preguntas.update { lista ->
                lista.filterNot { it.id == pregunta.id }
            }

            if (_preguntaEditando.value == pregunta.id) {

                _preguntaEditando.value = null
                _pregunta.value = PreguntaUiState()
            }
        }
    }

    fun limpiarMensajes() {

        _contenido.update { it.copy(guardado = false) }
        _pregunta.update { it.copy(guardado = false) }
    }

    // ---------- Informes pedagógicos ----------

    fun generarInforme(fila: FilaResultado) {

        ejecutar {

            val informe = repository.generarInforme(
                fila.estudiante.rut,
                fila.objetivo.id
            )

            actualizarInformeLocal(informe)
        }
    }

    fun validarInforme(informe: InformeEntity) {

        ejecutar {

            actualizarInformeLocal(
                repository.validarInforme(informe.clave)
            )
        }
    }

    init {
        recargar()
    }
}