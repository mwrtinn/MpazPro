package cl.duoc.mpazpro.viewmodel

import cl.duoc.mpazpro.data.remote.*
import cl.duoc.mpazpro.model.TipoPregunta
import org.junit.After
import org.junit.Before
import org.junit.Assert.*
import org.junit.Test

// Pruebas automatizadas del aprendizaje del estudiante.
class EstudianteViewModelTest : BasePruebaRemota() {

    private val rut = "111111111"

    @Before
    fun iniciarSesionPrueba() {

        SesionRemota.iniciar(
            LoginResponse(
                rut = rut,
                nombre = "Estudiante de prueba",
                rol = "ESTUDIANTE",
                curso = "3 Básico",
                token = "token-ficticio"
            )
        )
    }

    @After
    fun cerrarSesionPrueba() {

        SesionRemota.cerrar()
    }

    private fun crearModelo(): EstudianteViewModel {

        return registrar(
            EstudianteViewModel(repository)
        )
    }

    // Simula los datos necesarios para comenzar un quiz.
    private fun prepararQuiz(
        tipo: String = TipoPregunta.DESAFIO,
        asignado: Boolean = true,
        preguntas: List<PreguntaDto> = listOf(
            pregunta().copy(
                tipo = tipo,
                correcta = null
            )
        ),
        historial: List<RespuestaDto> = emptyList(),
        ultimo: Int = 0,
        permitidos: Int = 3,
        disponible: Boolean = true
    ) {

        responder(
            objetivo(asignado).copy(
                intentosPrueba = permitidos
            )
        )

        responder(preguntas)
        responder(historial)

        responder(
            IntentosDto(
                rutAlumno = rut,
                oaId = 1,
                tipo = tipo,
                ultimoIntento = ultimo,
                intentosPermitidos = permitidos,
                intentosRestantes =
                    (permitidos - ultimo).coerceAtLeast(0),
                disponible = disponible
            )
        )
    }

    // Inicia una evaluación con datos simulados.
    private suspend fun iniciarQuiz(
        vm: EstudianteViewModel,
        tipo: String = TipoPregunta.DESAFIO
    ) {

        prepararQuiz(tipo = tipo)

        vm.iniciarQuiz(1, tipo)

        esperar(vm)

        assertNull(vm.errorRemoto.value)
    }

    // ---------- Sesión y objetivos ----------

    @Test
    fun estadoInicialSinDatos() = prueba {

        val vm = crearModelo()

        assertTrue(vm.objetivos.value.isEmpty())
        assertTrue(vm.contenidos.value.isEmpty())
        assertTrue(vm.respuestas.value.isEmpty())

        assertNull(vm.objetivo.value)
        assertFalse(vm.pruebaPendiente.value)
    }

    @Test
    fun iniciarSesionCargaObjetivosAsignados() = prueba {

        val vm = crearModelo()

        responder(
            listOf(
                objetivo(true),
                objetivo(false).copy(id = 2)
            )
        )

        responder(listOf(respuesta()))

        vm.iniciarSesion(rut)

        esperar(vm)

        assertEquals(1, vm.objetivos.value.size)
        assertTrue(vm.objetivos.value.first().asignado)

        assertEquals(1, vm.respuestas.value.size)
        assertNull(vm.errorRemoto.value)
    }

    @Test
    fun rechazaRutDistintoDeLaSesion() = prueba {

        val vm = crearModelo()

        vm.iniciarSesion("222222222")

        esperar(vm)

        assertNotNull(vm.errorRemoto.value)
        assertTrue(vm.objetivos.value.isEmpty())
        assertEquals(0, servidor.requestCount)
    }

    @Test
    fun seleccionarObjetivoCargaContenidosAprobados() = prueba {

        val vm = crearModelo()

        responder(objetivo())

        responder(
            listOf(
                contenido("APROBADO"),
                contenido("PENDIENTE").copy(id = 2),
                contenido("RECHAZADO").copy(id = 3)
            )
        )

        responder(emptyList<RespuestaDto>())

        responder(
            listOf(
                pregunta().copy(
                    tipo = TipoPregunta.PRUEBA,
                    correcta = null
                )
            )
        )

        vm.seleccionarObjetivo(1)

        esperar(vm)

        assertEquals(1, vm.objetivo.value?.id)
        assertEquals(1, vm.contenidos.value.size)

        assertEquals(
            "APROBADO",
            vm.contenidos.value.first().estado
        )

        assertFalse(vm.pruebaPendiente.value)
    }

    @Test
    fun detectaPruebaPendienteDeCompletar() = prueba {

        val vm = crearModelo()

        responder(objetivo())

        responder(
            listOf(contenido("APROBADO"))
        )

        responder(
            listOf(
                respuesta().copy(
                    tipo = TipoPregunta.PRUEBA
                )
            )
        )

        responder(
            listOf(
                pregunta().copy(
                    tipo = TipoPregunta.PRUEBA,
                    correcta = null
                ),
                pregunta().copy(
                    id = 2,
                    tipo = TipoPregunta.PRUEBA,
                    correcta = null
                )
            )
        )

        vm.seleccionarObjetivo(1)

        esperar(vm)

        assertTrue(vm.pruebaPendiente.value)
    }

    @Test
    fun pruebaCompletaNoApareceComoPendiente() = prueba {

        val vm = crearModelo()

        responder(objetivo())

        responder(
            listOf(contenido("APROBADO"))
        )

        responder(
            listOf(
                respuesta().copy(
                    tipo = TipoPregunta.PRUEBA
                )
            )
        )

        responder(
            listOf(
                pregunta().copy(
                    tipo = TipoPregunta.PRUEBA,
                    correcta = null
                )
            )
        )

        vm.seleccionarObjetivo(1)

        esperar(vm)

        assertFalse(vm.pruebaPendiente.value)
    }

    // ---------- Inicio de evaluaciones ----------

    @Test
    fun iniciarDesafioCargaPreguntas() = prueba {

        val vm = crearModelo()

        iniciarQuiz(vm)

        val quiz = vm.quiz.value

        assertEquals(
            TipoPregunta.DESAFIO,
            quiz.tipo
        )

        assertEquals(1, quiz.preguntas.size)
        assertEquals(0, quiz.indice)
        assertEquals(1, quiz.intento)

        assertFalse(quiz.terminado)
        assertFalse(quiz.cargando)

        // La respuesta correcta permanece oculta.
        assertEquals(
            -1,
            quiz.preguntaActual?.correcta
        )
    }

    @Test
    fun evaluacionSinPreguntasQuedaBloqueada() = prueba {

        val vm = crearModelo()

        prepararQuiz(
            preguntas = emptyList()
        )

        vm.iniciarQuiz(
            1,
            TipoPregunta.DESAFIO
        )

        esperar(vm)

        assertNotNull(
            vm.quiz.value.bloqueadoMensaje
        )

        assertTrue(vm.quiz.value.preguntas.isEmpty())
    }

    @Test
    fun objetivoNoAsignadoBloqueaEvaluacion() = prueba {

        val vm = crearModelo()

        prepararQuiz(
            asignado = false
        )

        vm.iniciarQuiz(
            1,
            TipoPregunta.DESAFIO
        )

        esperar(vm)

        assertNotNull(
            vm.quiz.value.bloqueadoMensaje
        )
    }

    @Test
    fun pruebaSinIntentosDisponiblesQuedaBloqueada() = prueba {

        val vm = crearModelo()

        prepararQuiz(
            tipo = TipoPregunta.PRUEBA,
            ultimo = 3,
            disponible = false
        )

        vm.iniciarQuiz(
            1,
            TipoPregunta.PRUEBA
        )

        esperar(vm)

        assertTrue(
            vm.quiz.value.bloqueadoMensaje.orEmpty()
                .contains("intentos")
        )
    }

    @Test
    fun nuevoIntentoUtilizaNumeracionDelServidor() = prueba {

        val vm = crearModelo()

        prepararQuiz(
            tipo = TipoPregunta.PRUEBA,
            ultimo = 1,
            disponible = true
        )

        vm.iniciarQuiz(
            1,
            TipoPregunta.PRUEBA
        )

        esperar(vm)

        assertEquals(2, vm.quiz.value.intento)
        assertEquals(3, vm.quiz.value.intentosPermitidos)
    }

    @Test
    fun reanudaUltimoIntentoIncompleto() = prueba {

        val vm = crearModelo()

        prepararQuiz(
            tipo = TipoPregunta.PRUEBA,

            preguntas = listOf(
                pregunta().copy(
                    tipo = TipoPregunta.PRUEBA,
                    correcta = null
                ),
                pregunta().copy(
                    id = 2,
                    tipo = TipoPregunta.PRUEBA,
                    correcta = null
                )
            ),

            historial = listOf(
                respuesta().copy(
                    tipo = TipoPregunta.PRUEBA,
                    intento = 3
                )
            ),

            ultimo = 3,
            permitidos = 3,
            disponible = false
        )

        vm.iniciarQuiz(
            1,
            TipoPregunta.PRUEBA
        )

        esperar(vm)

        val quiz = vm.quiz.value

        assertEquals(3, quiz.intento)
        assertEquals(1, quiz.indice)
        assertEquals(1, quiz.resultados.size)
        assertEquals(2, quiz.preguntaActual?.id)

        assertNull(quiz.bloqueadoMensaje)
    }

    // ---------- Selección y respuestas ----------

    @Test
    fun seleccionarAlternativaActualizaEstado() = prueba {

        val vm = crearModelo()

        iniciarQuiz(vm)

        vm.seleccionar(1)

        assertEquals(
            1,
            vm.quiz.value.seleccion
        )
    }

    @Test
    fun rechazaAlternativasFueraDeRango() = prueba {

        val vm = crearModelo()

        iniciarQuiz(vm)

        vm.seleccionar(1)
        vm.seleccionar(5)
        vm.seleccionar(-2)

        assertEquals(
            1,
            vm.quiz.value.seleccion
        )
    }

    @Test
    fun noPermiteAvanzarSinResponder() = prueba {

        val vm = crearModelo()

        iniciarQuiz(vm)

        vm.siguiente()

        assertEquals(0, vm.quiz.value.indice)
        assertFalse(vm.quiz.value.terminado)
        assertTrue(vm.quiz.value.resultados.isEmpty())
    }

    @Test
    fun desafioCorrectoUtilizaRespuestaDelServidor() = prueba {

        val vm = crearModelo()

        iniciarQuiz(vm)

        responder(respuesta())

        vm.seleccionar(1)
        vm.responder()

        esperar(vm)

        val quiz = vm.quiz.value

        assertTrue(quiz.respondida)
        assertEquals(1, quiz.resultados.size)

        assertTrue(
            quiz.resultados.first().correcta
        )

        assertEquals(1, vm.respuestas.value.size)

        assertEquals(
            1,
            quiz.preguntaActual?.correcta
        )
    }

    @Test
    fun desafioIncorrectoMuestraCorreccionRemota() = prueba {

        val vm = crearModelo()

        iniciarQuiz(vm)

        responder(
            respuesta().copy(
                elegida = 0,
                esCorrecta = false,
                correcta = 1
            )
        )

        vm.seleccionar(0)
        vm.responder()

        esperar(vm)

        assertTrue(vm.quiz.value.respondida)

        assertFalse(
            vm.quiz.value.resultados.first().correcta
        )

        assertEquals(
            1,
            vm.quiz.value.preguntaActual?.correcta
        )
    }

    @Test
    fun noPermiteRegistrarDosVecesLaMismaRespuesta() = prueba {

        val vm = crearModelo()

        iniciarQuiz(vm)

        responder(respuesta())

        vm.seleccionar(1)
        vm.responder()

        esperar(vm)

        vm.responder()
        vm.seleccionar(2)

        assertEquals(1, vm.quiz.value.resultados.size)
        assertEquals(1, vm.quiz.value.seleccion)

        // Cuatro consultas iniciales y una respuesta.
        assertEquals(5, servidor.requestCount)
    }

    @Test
    fun terminarDesafioMuestraResumen() = prueba {

        val vm = crearModelo()

        iniciarQuiz(vm)

        responder(respuesta())

        vm.seleccionar(1)
        vm.responder()

        esperar(vm)

        vm.siguiente()

        assertTrue(vm.quiz.value.terminado)
        assertEquals(1, vm.quiz.value.resultados.size)
    }

    @Test
    fun pruebaFinalAvanzaAutomaticamente() = prueba {

        val vm = crearModelo()

        prepararQuiz(
            tipo = TipoPregunta.PRUEBA,

            preguntas = listOf(
                pregunta().copy(
                    tipo = TipoPregunta.PRUEBA,
                    correcta = null
                ),
                pregunta().copy(
                    id = 2,
                    tipo = TipoPregunta.PRUEBA,
                    correcta = null
                )
            )
        )

        vm.iniciarQuiz(
            1,
            TipoPregunta.PRUEBA
        )

        esperar(vm)

        responder(
            respuesta().copy(
                tipo = TipoPregunta.PRUEBA
            )
        )

        vm.seleccionar(1)
        vm.responder()

        esperar(vm)

        assertEquals(1, vm.quiz.value.indice)
        assertEquals(2, vm.quiz.value.preguntaActual?.id)

        assertFalse(vm.quiz.value.respondida)
        assertFalse(vm.quiz.value.terminado)

        responder(
            respuesta().copy(
                id = 2,
                preguntaId = 2,
                tipo = TipoPregunta.PRUEBA
            )
        )

        vm.seleccionar(1)
        vm.responder()

        esperar(vm)

        assertTrue(vm.quiz.value.terminado)
        assertEquals(2, vm.quiz.value.resultados.size)
    }

    // ---------- Manejo de errores ----------

    @Test
    fun errorAlResponderConservaLaPreguntaActual() = prueba {

        val vm = crearModelo()

        iniciarQuiz(vm)

        responder(
            mapOf(
                "mensaje" to "Conflicto al responder"
            ),
            409
        )

        vm.seleccionar(1)
        vm.responder()

        esperar(vm)

        assertNotNull(vm.errorRemoto.value)

        assertEquals(0, vm.quiz.value.indice)
        assertFalse(vm.quiz.value.respondida)

        assertTrue(vm.quiz.value.resultados.isEmpty())
        assertTrue(vm.respuestas.value.isEmpty())
    }

    @Test
    fun errorAlCargarEvaluacionPermiteReintentar() = prueba {

        val vm = crearModelo()

        responder(
            mapOf(
                "mensaje" to "Error del servidor"
            ),
            500
        )

        vm.iniciarQuiz(
            1,
            TipoPregunta.DESAFIO
        )

        esperar(vm)

        assertNotNull(vm.errorRemoto.value)
        assertFalse(vm.cargando.value)
        assertFalse(vm.quiz.value.cargando)

        iniciarQuiz(vm)

        assertNull(vm.errorRemoto.value)
        assertEquals(1, vm.quiz.value.preguntas.size)
    }
}