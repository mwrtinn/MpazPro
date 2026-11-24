package cl.duoc.mpazpro.viewmodel

import cl.duoc.mpazpro.data.local.entity.ContenidoEntity
import cl.duoc.mpazpro.data.local.entity.InformeEntity
import cl.duoc.mpazpro.data.local.entity.ObjetivoEntity
import cl.duoc.mpazpro.data.local.entity.PreguntaEntity
import cl.duoc.mpazpro.data.local.entity.UsuarioEntity
import cl.duoc.mpazpro.data.local.entity.RespuestaEntity
import cl.duoc.mpazpro.model.ConfigIntentosState
import cl.duoc.mpazpro.model.ContenidoErrores
import cl.duoc.mpazpro.model.ContenidoUiState
import cl.duoc.mpazpro.model.EstadoContenido
import cl.duoc.mpazpro.model.FilaResultado
import cl.duoc.mpazpro.model.PreguntaErrores
import cl.duoc.mpazpro.model.PreguntaUiState
import cl.duoc.mpazpro.repository.AulaRemotaRepository
import cl.duoc.mpazpro.utils.Validaciones
import cl.duoc.mpazpro.utils.claveInforme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Gestiona el panel docente utilizando los datos de Spring Boot.
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

    // Conserva temporalmente las imágenes seleccionadas.
    private val imagenesLocales = mutableMapOf<Int, String>()

    fun recargar() = ejecutar {
        cargarDatos()
    }

    // Consulta ambos microservicios y actualiza el panel.
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

    // Relaciona los estudiantes con sus objetivos y resultados.
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

    private fun actualizarObjetivoLocal(
        objetivo: ObjetivoEntity
    ) {

        _objetivos.update { lista ->

            lista.map {
                if (it.id == objetivo.id) objetivo else it
            }
        }

        reconstruirFilas()
    }

    private fun actualizarInformeLocal(
        informe: InformeEntity
    ) {

        informes = informes.filterNot {
            it.clave == informe.clave
        } + informe

        reconstruirFilas()
    }

    // ---------- Objetivos e intentos ----------

    private val _config =
        MutableStateFlow(ConfigIntentosState())

    val config: StateFlow<ConfigIntentosState> =
        _config.asStateFlow()

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

            _config.update {
                it.copy(error = error)
            }

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

    private val _contenido =
        MutableStateFlow(ContenidoUiState())

    val contenido: StateFlow<ContenidoUiState> =
        _contenido.asStateFlow()

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

    fun onExplicacionChanged(
        explicacion: String
    ) = _contenido.update {

        it.copy(
            explicacion = explicacion,
            errores = it.errores.copy(
                explicacion =
                    Validaciones.validarExplicacion(explicacion)
            ),
            guardado = false
        )
    }

    fun onEjemploChanged(ejemplo: String) = _contenido.update {

        it.copy(
            ejemplo = ejemplo,
            errores = it.errores.copy(
                ejemplo = Validaciones.validarEjemplo(ejemplo)
            ),
            guardado = false
        )
    }

    fun onIconoChanged(icono: String) = _contenido.update {

        it.copy(icono = icono)
    }

    // Recibe la imagen seleccionada desde la cámara o galería.
    fun onImagenChanged(ruta: String?) = _contenido.update {

        it.copy(
            imagenPath = ruta,
            guardado = false
        )
    }

    // Valida los campos antes de enviar el contenido.
    fun validarContenido(): Boolean {

        val c = _contenido.value

        val errores = ContenidoErrores(
            oa = if (c.oaId == null)
                "Selecciona un objetivo" else null,

            titulo = Validaciones.validarTitulo(c.titulo),

            explicacion =
                Validaciones.validarExplicacion(c.explicacion),

            ejemplo = Validaciones.validarEjemplo(c.ejemplo)
        )

        _contenido.update {
            it.copy(errores = errores)
        }

        return errores == ContenidoErrores()
    }

    // El contenido queda pendiente de revisión por UTP.
    fun crearContenido() {

        if (!validarContenido()) return

        val c = _contenido.value
        val oaId = c.oaId ?: return

        ejecutar {

            val creado = repository.crearContenido(

                ContenidoEntity(
                    oaId = oaId,
                    titulo = c.titulo.trim(),
                    explicacion = c.explicacion.trim(),
                    ejemplo = c.ejemplo.trim(),
                    icono = c.icono,
                    estado = EstadoContenido.PENDIENTE,
                    imagenPath = c.imagenPath
                )
            )

            c.imagenPath?.let {
                imagenesLocales[creado.id] = it
            }

            _contenidos.update {

                it + creado.copy(
                    imagenPath = c.imagenPath
                )
            }

            _contenido.value = ContenidoUiState(
                oaId = oaId,
                guardado = true
            )
        }
    }

    // ---------- Formulario de preguntas ----------

    private val _pregunta =
        MutableStateFlow(PreguntaUiState())

    val pregunta: StateFlow<PreguntaUiState> =
        _pregunta.asStateFlow()

    fun onPreguntaOaChanged(oaId: Int) = _pregunta.update {

        it.copy(
            oaId = oaId,
            errores = it.errores.copy(oa = null),
            guardado = false
        )
    }

    fun onEnunciadoChanged(
        enunciado: String
    ) = _pregunta.update {

        it.copy(
            enunciado = enunciado,
            errores = it.errores.copy(
                enunciado =
                    Validaciones.validarEnunciado(enunciado)
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

        it.copy(tipo = tipo)
    }

    fun onDificultadChanged(nivel: Int) = _pregunta.update {

        it.copy(dificultad = nivel)
    }

    fun onCorrectaChanged(indice: Int) = _pregunta.update {

        it.copy(correcta = indice)
    }

    // Valida el formulario y evita alternativas repetidas.
    fun validarPregunta(): Boolean {

        val p = _pregunta.value

        val errores = PreguntaErrores(

            oa = if (p.oaId == null)
                "Selecciona un objetivo" else null,

            enunciado =
                Validaciones.validarEnunciado(p.enunciado),

            opcionA =
                Validaciones.validarAlternativa(p.opcionA),

            opcionB =
                Validaciones.validarAlternativa(p.opcionB)
                    ?: Validaciones.alternativaRepetida(
                        p.opcionB,
                        listOf(p.opcionA)
                    ),

            opcionC =
                Validaciones.validarAlternativa(p.opcionC)
                    ?: Validaciones.alternativaRepetida(
                        p.opcionC,
                        listOf(p.opcionA, p.opcionB)
                    )
        )

        _pregunta.update {
            it.copy(errores = errores)
        }

        return errores == PreguntaErrores()
    }

    // Registra una nueva pregunta en el microservicio.
    fun crearPregunta() {

        if (!validarPregunta()) return

        val p = _pregunta.value
        val oaId = p.oaId ?: return

        ejecutar {

            val creada = repository.crearPregunta(

                PreguntaEntity(
                    oaId = oaId,
                    tipo = p.tipo,
                    dificultad = p.dificultad,
                    enunciado = p.enunciado.trim(),
                    opcionA = p.opcionA.trim(),
                    opcionB = p.opcionB.trim(),
                    opcionC = p.opcionC.trim(),
                    correcta = p.correcta
                )
            )

            _preguntas.update {
                it + creada
            }

            _pregunta.value = PreguntaUiState(
                oaId = oaId,
                tipo = p.tipo,
                dificultad = p.dificultad,
                guardado = true
            )
        }
    }

    // Oculta los mensajes de guardado.
    fun limpiarMensajes() {

        _contenido.update {
            it.copy(guardado = false)
        }

        _pregunta.update {
            it.copy(guardado = false)
        }
    }

    // ---------- Informes pedagógicos ----------

    // El servidor calcula el rendimiento del estudiante.
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

    // Carga los datos al ingresar al panel docente.
    init {
        recargar()
    }
}