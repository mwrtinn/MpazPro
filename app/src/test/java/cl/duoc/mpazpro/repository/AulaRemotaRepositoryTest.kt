package cl.duoc.mpazpro.repository

import cl.duoc.mpazpro.data.remote.*
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Verifica las solicitudes HTTP del repositorio remoto.
class AulaRemotaRepositoryTest {

    private lateinit var servidor: MockWebServer
    private lateinit var repository: AulaRemotaRepository

    private val gson = Gson()

    @Before
    fun preparar() {

        servidor = MockWebServer()
        servidor.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(servidor.url("/"))
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()

        repository = AulaRemotaRepository(
            usuarios = retrofit.create(
                UsuariosApi::class.java
            ),
            aprendizaje = retrofit.create(
                AprendizajeApi::class.java
            )
        )
    }

    @After
    fun finalizar() {

        servidor.shutdown()
    }

    // Simula una respuesta JSON del microservicio.
    private fun responder(
        datos: Any,
        codigo: Int = 200
    ) {

        servidor.enqueue(
            MockResponse()
                .setResponseCode(codigo)
                .setHeader(
                    "Content-Type",
                    "application/json"
                )
                .setBody(gson.toJson(datos))
        )
    }

    private fun responderSinContenido() {

        servidor.enqueue(
            MockResponse().setResponseCode(204)
        )
    }

    private fun solicitud(): RecordedRequest {

        return requireNotNull(
            servidor.takeRequest(
                2,
                TimeUnit.SECONDS
            )
        )
    }

    private fun objetivo(
        id: Int = 1,
        asignado: Boolean = true
    ) = ObjetivoDto(
        id = id,
        asignatura = "Matemática",
        unidad = "Unidad 1",
        codigo = "MA03 OA 01",
        eje = "Números",
        descripcion = "Resolver ejercicios",
        referencia = "Currículum Nacional",
        asignado = asignado,
        intentosPrueba = 3
    )

    private fun contenido(
        estado: String = "PENDIENTE"
    ) = ContenidoDto(
        id = 1,
        oaId = 1,
        titulo = "Sumas",
        explicacion = "Sumar cantidades",
        ejemplo = "2 + 2 = 4",
        icono = "📚",
        estado = estado
    )

    private fun pregunta(
        id: Int = 1,
        dificultad: Int = 1
    ) = PreguntaDto(
        id = id,
        oaId = 1,
        tipo = "DESAFIO",
        dificultad = dificultad,
        enunciado = "¿Cuánto es 2 más 2?",
        opcionA = "3",
        opcionB = "4",
        opcionC = "5",
        correcta = 1
    )

    private fun respuesta() = RespuestaDto(
        id = 1,
        rutAlumno = "111111111",
        oaId = 1,
        preguntaId = 1,
        tipo = "DESAFIO",
        dificultad = 1,
        intento = 1,
        elegida = 1,
        esCorrecta = true,
        fecha = 1700000000000L,
        correcta = 1
    )

    private fun informe(
        validado: Boolean = false
    ) = InformeDto(
        clave = "111111111-1",
        rutAlumno = "111111111",
        oaId = 1,
        rendimiento = 100,
        fortalezas = "Resuelve sumas",
        refuerzo = "Continuar practicando",
        fecha = "2026-11-24",
        validado = validado
    )

    @Test
    fun consultaEstudiantesPorRol() = runBlocking {

        responder(
            listOf(
                UsuarioDto(
                    rut = "111111111",
                    nombre = "Estudiante de prueba",
                    rol = "ESTUDIANTE",
                    curso = "3 Básico"
                )
            )
        )

        val estudiantes = repository.estudiantes()

        assertEquals(1, estudiantes.size)
        assertEquals("ESTUDIANTE", estudiantes.first().rol)

        val peticion = solicitud()

        assertEquals("GET", peticion.method)
        assertEquals(
            "/api/usuarios",
            peticion.requestUrl?.encodedPath
        )

        assertEquals(
            "ESTUDIANTE",
            peticion.requestUrl?.queryParameter("rol")
        )
    }

    @Test
    fun listarUsuariosRecuperaDatos() = runBlocking {

        responder(
            listOf(
                UsuarioDto(
                    "123456785",
                    "Docente de prueba",
                    "DOCENTE",
                    "3 Básico"
                )
            )
        )

        val usuarios = repository.listarUsuarios()

        assertEquals(1, usuarios.size)
        assertEquals("DOCENTE", usuarios.first().rol)
        assertEquals("/api/usuarios", solicitud().path)
    }

    @Test
    fun objetivosAsignadosFiltraCorrectamente() = runBlocking {

        responder(
            listOf(
                objetivo(1, true),
                objetivo(2, false)
            )
        )

        val objetivos = repository.objetivosAsignados()

        assertEquals(1, objetivos.size)
        assertEquals(1, objetivos.first().id)
        assertEquals("/api/objetivos", solicitud().path)
    }

    @Test
    fun buscarObjetivoConsultaIdentificador() = runBlocking {

        responder(objetivo())

        val resultado = repository.buscarObjetivo(1)

        assertEquals(1, resultado.id)
        assertEquals("/api/objetivos/1", solicitud().path)
    }

    @Test
    fun crearObjetivoUtilizaPost() = runBlocking {

        responder(objetivo(), 201)

        val creado = repository.crearObjetivo(
            objetivo().aEntidad().copy(id = 0)
        )

        assertEquals(1, creado.id)

        val peticion = solicitud()

        assertEquals("POST", peticion.method)
        assertEquals("/api/objetivos", peticion.path)
    }

    @Test
    fun actualizarObjetivoUtilizaPut() = runBlocking {

        responder(objetivo(asignado = false))

        val actualizado = repository.actualizarObjetivo(
            objetivo().aEntidad().copy(asignado = false)
        )

        assertFalse(actualizado.asignado)

        val peticion = solicitud()

        assertEquals("PUT", peticion.method)
        assertEquals("/api/objetivos/1", peticion.path)
    }

    @Test
    fun eliminarObjetivoUtilizaDelete() = runBlocking {

        responderSinContenido()

        repository.eliminarObjetivo(1)

        val peticion = solicitud()

        assertEquals("DELETE", peticion.method)
        assertEquals("/api/objetivos/1", peticion.path)
    }

    @Test
    fun listarContenidosRecuperaRegistros() = runBlocking {

        responder(listOf(contenido()))

        val resultado = repository.contenidos()

        assertEquals(1, resultado.size)
        assertEquals("Sumas", resultado.first().titulo)
        assertEquals("/api/contenidos", solicitud().path)
    }

    @Test
    fun estudianteSoloRecibeContenidosAprobados() = runBlocking {

        responder(
            listOf(
                contenido("APROBADO"),
                contenido("PENDIENTE").copy(id = 2),
                contenido("RECHAZADO").copy(id = 3)
            )
        )

        val contenidos =
            repository.contenidosAprobados(1)

        assertEquals(1, contenidos.size)
        assertEquals("APROBADO", contenidos.first().estado)

        assertEquals(
            "1",
            solicitud().requestUrl?.queryParameter("oaId")
        )
    }

    @Test
    fun crearContenidoUtilizaPost() = runBlocking {

        responder(contenido(), 201)

        val creado = repository.crearContenido(
            contenido().aEntidad().copy(id = 0)
        )

        assertEquals(1, creado.id)

        val peticion = solicitud()

        assertEquals("POST", peticion.method)
        assertEquals("/api/contenidos", peticion.path)
    }

    @Test
    fun editarContenidoUtilizaPut() = runBlocking {

        responder(
            contenido().copy(
                titulo = "Sumas editadas"
            )
        )

        val actualizado = repository.actualizarContenido(
            contenido().aEntidad().copy(
                titulo = "Sumas editadas"
            )
        )

        assertEquals("Sumas editadas", actualizado.titulo)

        val peticion = solicitud()

        assertEquals("PUT", peticion.method)
        assertEquals("/api/contenidos/1", peticion.path)
    }

    @Test
    fun revisarContenidoUtilizaPatch() = runBlocking {

        responder(contenido("APROBADO"))

        val aprobado = repository.revisarContenido(
            1,
            "APROBADO",
            ""
        )

        assertEquals("APROBADO", aprobado.estado)

        val peticion = solicitud()

        assertEquals("PATCH", peticion.method)
        assertEquals("/api/contenidos/1/estado", peticion.path)

        val json = JsonParser.parseString(
            peticion.body.readUtf8()
        ).asJsonObject

        assertEquals(
            "APROBADO",
            json.get("estado").asString
        )
    }

    @Test
    fun eliminarContenidoUtilizaDelete() = runBlocking {

        responderSinContenido()

        repository.eliminarContenido(1)

        val peticion = solicitud()

        assertEquals("DELETE", peticion.method)
        assertEquals("/api/contenidos/1", peticion.path)
    }

    @Test
    fun listarPreguntasRecuperaRegistros() = runBlocking {

        responder(listOf(pregunta()))

        val preguntas = repository.preguntas()

        assertEquals(1, preguntas.size)
        assertEquals(1, preguntas.first().correcta)
        assertEquals("/api/preguntas", solicitud().path)
    }

    @Test
    fun preguntasSeOrdenanPorDificultad() = runBlocking {

        responder(
            listOf(
                pregunta(3, 3),
                pregunta(2, 2),
                pregunta(1, 1)
            )
        )

        val preguntas = repository.preguntasDe(
            1,
            "DESAFIO"
        )

        assertEquals(
            listOf(1, 2, 3),
            preguntas.map { it.dificultad }
        )

        val peticion = solicitud()

        assertEquals(
            "DESAFIO",
            peticion.requestUrl?.queryParameter("tipo")
        )

        assertEquals(
            "1",
            peticion.requestUrl?.queryParameter("oaId")
        )
    }

    @Test
    fun crearPreguntaUtilizaPost() = runBlocking {

        responder(pregunta(), 201)

        val creada = repository.crearPregunta(
            pregunta().aEntidad().copy(id = 0)
        )

        assertEquals(1, creada.id)

        val peticion = solicitud()

        assertEquals("POST", peticion.method)
        assertEquals("/api/preguntas", peticion.path)
    }

    @Test
    fun actualizarPreguntaUtilizaPut() = runBlocking {

        responder(
            pregunta().copy(dificultad = 2)
        )

        val actualizada = repository.actualizarPregunta(
            pregunta().aEntidad().copy(dificultad = 2)
        )

        assertEquals(2, actualizada.dificultad)

        val peticion = solicitud()

        assertEquals("PUT", peticion.method)
        assertEquals("/api/preguntas/1", peticion.path)
    }

    @Test
    fun eliminarPreguntaUtilizaDelete() = runBlocking {

        responderSinContenido()

        repository.eliminarPregunta(1)

        val peticion = solicitud()

        assertEquals("DELETE", peticion.method)
        assertEquals("/api/preguntas/1", peticion.path)
    }

    @Test
    fun conflictoAlEliminarPreguntaSePropaga() = runBlocking {

        responder(
            mapOf(
                "mensaje" to "Pregunta con respuestas"
            ),
            409
        )

        try {

            repository.eliminarPregunta(1)

            fail("Se esperaba un error HTTP 409")

        } catch (e: HttpException) {

            assertEquals(409, e.code())
        }

        assertEquals("DELETE", solicitud().method)
    }

    @Test
    fun registrarRespuestaUtilizaCorreccionRemota() = runBlocking {

        responder(respuesta())

        val resultado = repository.registrarRespuesta(
            preguntaId = 1,
            intento = 1,
            elegida = 1
        )

        assertTrue(resultado.esCorrecta)
        assertEquals(1, resultado.correcta)

        val peticion = solicitud()

        assertEquals("POST", peticion.method)
        assertEquals("/api/respuestas", peticion.path)

        val json = JsonParser.parseString(
            peticion.body.readUtf8()
        ).asJsonObject

        assertEquals(1, json.get("preguntaId").asInt)
        assertEquals(1, json.get("intento").asInt)
        assertEquals(1, json.get("elegida").asInt)

        assertFalse(json.has("esCorrecta"))
        assertFalse(json.has("rutAlumno"))
    }

    @Test
    fun listarRespuestasRecuperaResultados() = runBlocking {

        responder(listOf(respuesta()))

        val respuestas = repository.respuestas()

        assertEquals(1, respuestas.size)
        assertTrue(respuestas.first().esCorrecta)
        assertEquals("/api/respuestas", solicitud().path)
    }

    @Test
    fun consultarRespuestasPorEstudiante() = runBlocking {

        responder(listOf(respuesta()))

        val respuestas =
            repository.respuestasDeAlumno("111111111")

        assertEquals(1, respuestas.size)

        assertEquals(
            "/api/respuestas/alumno/111111111",
            solicitud().path
        )
    }

    @Test
    fun consultarIntentosRecuperaDisponibilidad() = runBlocking {

        responder(
            IntentosDto(
                rutAlumno = "111111111",
                oaId = 1,
                tipo = "PRUEBA",
                ultimoIntento = 1,
                intentosPermitidos = 3,
                intentosRestantes = 2,
                disponible = true
            )
        )

        val resultado = repository.consultarIntentos(
            1,
            "PRUEBA"
        )

        assertEquals(2, resultado.intentosRestantes)
        assertTrue(resultado.disponible)

        val peticion = solicitud()

        assertEquals(
            "PRUEBA",
            peticion.requestUrl?.queryParameter("tipo")
        )

        assertEquals(
            "1",
            peticion.requestUrl?.queryParameter("oaId")
        )
    }

    @Test
    fun ultimoIntentoRecuperaValorRemoto() = runBlocking {

        responder(
            IntentosDto(
                rutAlumno = "111111111",
                oaId = 1,
                tipo = "PRUEBA",
                ultimoIntento = 2,
                intentosPermitidos = 3,
                intentosRestantes = 1,
                disponible = true
            )
        )

        val ultimo = repository.ultimoIntento(
            "111111111",
            1,
            "PRUEBA"
        )

        assertEquals(2, ultimo)
        assertEquals(
            "111111111",
            solicitud().requestUrl?.queryParameter("rut")
        )
    }

    @Test
    fun listarInformesRecuperaRendimiento() = runBlocking {

        responder(listOf(informe()))

        val informes = repository.informes()

        assertEquals(1, informes.size)
        assertEquals(100, informes.first().rendimiento)
        assertEquals("/api/informes", solicitud().path)
    }

    @Test
    fun generarInformeUtilizaServidor() = runBlocking {

        responder(informe())

        val resultado = repository.generarInforme(
            "111111111",
            1
        )

        assertEquals(100, resultado.rendimiento)

        val peticion = solicitud()

        assertEquals("PUT", peticion.method)

        assertEquals(
            "/api/informes/111111111-1",
            peticion.path
        )
    }

    @Test
    fun validarInformeUtilizaPatch() = runBlocking {

        responder(informe(true))

        val resultado = repository.validarInforme(
            "111111111-1"
        )

        assertTrue(resultado.validado)

        val peticion = solicitud()

        assertEquals("PATCH", peticion.method)

        assertEquals(
            "/api/informes/111111111-1/validar",
            peticion.path
        )
    }
}