package cl.duoc.mpazpro.viewmodel

import cl.duoc.mpazpro.data.local.entity.ContenidoEntity
import cl.duoc.mpazpro.data.local.entity.ObjetivoEntity
import cl.duoc.mpazpro.data.local.entity.RespuestaEntity
import cl.duoc.mpazpro.data.remote.SesionRemota
import cl.duoc.mpazpro.data.remote.aEntidad
import cl.duoc.mpazpro.model.QuizUiState
import cl.duoc.mpazpro.model.ResultadoPregunta
import cl.duoc.mpazpro.model.TipoPregunta
import cl.duoc.mpazpro.repository.AulaRemotaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Gestiona los aprendizajes y evaluaciones desde Spring Boot.
class EstudianteViewModel(
    private val repository: AulaRemotaRepository
) : OperacionRemotaViewModel() {

    private val _objetivos =
        MutableStateFlow<List<ObjetivoEntity>>(emptyList())

    val objetivos = _objetivos.asStateFlow()

    private val _respuestas =
        MutableStateFlow<List<RespuestaEntity>>(emptyList())

    val respuestas = _respuestas.asStateFlow()

    private val _objetivo =
        MutableStateFlow<ObjetivoEntity?>(null)

    val objetivo = _objetivo.asStateFlow()

    private val _contenidos =
        MutableStateFlow<List<ContenidoEntity>>(emptyList())

    val contenidos = _contenidos.asStateFlow()

    private val _pruebaPendiente =
        MutableStateFlow(false)

    val pruebaPendiente = _pruebaPendiente.asStateFlow()

    private val _quiz =
        MutableStateFlow(QuizUiState())

    val quiz = _quiz.asStateFlow()

    // ---------- Inicio del estudiante ----------

    fun iniciarSesion(rut: String) {

        ejecutar {

            // Comprueba que la navegación corresponda a la sesión.
            check(SesionRemota.rut == rut)

            val nuevosObjetivos =
                repository.objetivosAsignados()

            val nuevasRespuestas =
                repository.respuestas()

            _objetivos.value =
                nuevosObjetivos.sortedBy { it.id }

            _respuestas.value = nuevasRespuestas
        }
    }

    // ---------- Objetivos y contenidos ----------

    fun seleccionarObjetivo(id: Int) {

        if (cargando.value) return

        _objetivo.value = null
        _contenidos.value = emptyList()
        _pruebaPendiente.value = false

        ejecutar {

            val oa = repository.buscarObjetivo(id)

            val material =
                repository.contenidosAprobados(id)

            val historial =
                repository.respuestas()

            val preguntasPrueba = repository.preguntasDe(
                id,
                TipoPregunta.PRUEBA
            )

            val ultimo = historial
                .filter {
                    it.oaId == id &&
                            it.tipo == TipoPregunta.PRUEBA
                }
                .maxOfOrNull { it.intento } ?: 0

            val respondidas = historial.filter {

                it.oaId == id &&
                        it.tipo == TipoPregunta.PRUEBA &&
                        it.intento == ultimo

            }.map { it.preguntaId }.toSet()

            // Detecta si todavía hay preguntas de un intento activo.
            _pruebaPendiente.value =
                ultimo > 0 &&
                        ultimo <= oa.intentosPrueba &&
                        preguntasPrueba.any {
                            it.id !in respondidas
                        }

            _objetivo.value = oa
            _contenidos.value = material
            _respuestas.value = historial
        }
    }

    // ---------- Inicio de evaluaciones ----------

    fun iniciarQuiz(oaId: Int, tipo: String) {

        if (cargando.value) return

        val titulo = if (tipo == TipoPregunta.PRUEBA)
            "Prueba final"
        else
            "Desafíos"

        _quiz.value = QuizUiState(
            cargando = true,
            tipo = tipo,
            titulo = titulo
        )

        ejecutar {

            try {

                val oa = repository.buscarObjetivo(oaId)

                val preguntas =
                    repository.preguntasDe(oaId, tipo)

                val historial = repository.respuestas()

                val intentos = repository.consultarIntentos(
                    oaId = oaId,
                    tipo = tipo
                )

                _respuestas.value = historial

                val ultimo = intentos.ultimoIntento

                val anteriores = historial.filter {

                    it.oaId == oaId &&
                            it.tipo == tipo &&
                            it.intento == ultimo

                }.associateBy { it.preguntaId }

                // Separa las preguntas respondidas de las pendientes.
                val respondidas = preguntas.filter {
                    anteriores.containsKey(it.id)
                }

                val pendientes = preguntas.filter {
                    !anteriores.containsKey(it.id)
                }

                val reanudar =
                    ultimo > 0 &&
                            respondidas.isNotEmpty() &&
                            pendientes.isNotEmpty() &&
                            (
                                    tipo != TipoPregunta.PRUEBA ||
                                            ultimo <= oa.intentosPrueba
                                    )

                val bloqueado = when {

                    !oa.asignado ->
                        "Este objetivo no está disponible."

                    preguntas.isEmpty() ->
                        "Tu docente aún no agrega preguntas."

                    tipo == TipoPregunta.PRUEBA &&
                            !reanudar &&
                            !intentos.disponible ->
                        "Ya utilizaste los intentos permitidos."

                    else -> null
                }

                if (bloqueado != null) {

                    _quiz.value = QuizUiState(
                        cargando = false,
                        tipo = tipo,
                        titulo = titulo,
                        bloqueadoMensaje = bloqueado
                    )

                    return@ejecutar
                }

                // Recupera las respuestas de un intento incompleto.
                val resultadosPrevios = if (reanudar) {

                    respondidas.map { pregunta ->

                        val respuesta =
                            anteriores.getValue(pregunta.id)

                        ResultadoPregunta(
                            pregunta = pregunta.copy(
                                correcta = if (
                                    respuesta.esCorrecta
                                ) respuesta.elegida else -1
                            ),
                            elegida = respuesta.elegida,
                            correcta = respuesta.esCorrecta
                        )
                    }

                } else {
                    emptyList()
                }

                _quiz.value = QuizUiState(
                    cargando = false,
                    tipo = tipo,
                    titulo = titulo,

                    preguntas = if (reanudar)
                        respondidas + pendientes
                    else
                        preguntas,

                    indice = resultadosPrevios.size,

                    resultados = resultadosPrevios,

                    intento = if (reanudar)
                        ultimo
                    else
                        ultimo + 1,

                    intentosPermitidos = oa.intentosPrueba
                )

            } finally {

                _quiz.update {
                    it.copy(cargando = false)
                }
            }
        }
    }

    // ---------- Registro de respuestas ----------

    fun seleccionar(alternativa: Int) {

        if (cargando.value || alternativa !in 0..2) return

        _quiz.update {

            if (it.respondida || it.terminado)
                it
            else
                it.copy(seleccion = alternativa)
        }
    }

    // Envía la alternativa y espera la corrección del servidor.
    fun responder() {

        if (cargando.value) return

        val s = _quiz.value
        val pregunta = s.preguntaActual ?: return

        if (s.respondida ||
            s.terminado ||
            s.seleccion !in 0..2
        ) return

        ejecutar {

            val respuesta = repository.registrarRespuesta(
                preguntaId = pregunta.id,
                intento = s.intento,
                elegida = s.seleccion
            )

            val alternativaCorrecta =
                respuesta.correcta
                    ?: if (respuesta.esCorrecta)
                        respuesta.elegida
                    else
                        -1

            val preguntaCorregida = pregunta.copy(
                correcta = alternativaCorrecta
            )

            val preguntasActualizadas =
                s.preguntas.toMutableList()

            preguntasActualizadas[s.indice] =
                preguntaCorregida

            // Actualiza el progreso con la respuesta persistida.
            _respuestas.update { lista ->

                lista.filterNot {
                    it.id == respuesta.id
                } + respuesta.aEntidad()
            }

            val resultado = ResultadoPregunta(
                pregunta = preguntaCorregida,
                elegida = respuesta.elegida,
                correcta = respuesta.esCorrecta
            )

            val actualizado = s.copy(
                preguntas = preguntasActualizadas,
                resultados = s.resultados + resultado
            )

            // La prueba final muestra los resultados al terminar.
            if (s.tipo == TipoPregunta.PRUEBA) {

                avanzar(actualizado)

            } else {

                _quiz.value = actualizado.copy(
                    respondida = true
                )
            }
        }
    }

    fun siguiente() {

        if (cargando.value) return

        val s = _quiz.value

        if (s.respondida) {
            avanzar(s)
        }
    }

    // Avanza a la siguiente pregunta o finaliza la evaluación.
    private fun avanzar(s: QuizUiState) {

        _quiz.value = if (
            s.indice >= s.preguntas.lastIndex
        ) {

            s.copy(
                terminado = true,
                respondida = false,
                seleccion = -1
            )

        } else {

            s.copy(
                indice = s.indice + 1,
                seleccion = -1,
                respondida = false
            )
        }
    }
}