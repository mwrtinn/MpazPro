package cl.duoc.mpazpro.repository

import cl.duoc.mpazpro.data.local.dao.*
import cl.duoc.mpazpro.data.local.entity.*
import cl.duoc.mpazpro.model.EstadoContenido
import cl.duoc.mpazpro.model.Rol
import cl.duoc.mpazpro.model.TipoPregunta
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

// Pruebas del repositorio local mediante DAO simulados.
class AulaRepositoryTest {

    private val usuariosGuardados =
        mutableListOf<UsuarioEntity>()

    private val objetivosGuardados =
        mutableListOf<ObjetivoEntity>()

    private val contenidosGuardados =
        mutableListOf<ContenidoEntity>()

    private val preguntasGuardadas =
        mutableListOf<PreguntaEntity>()

    private val respuestasGuardadas =
        mutableListOf<RespuestaEntity>()

    private val informesGuardados =
        mutableListOf<InformeEntity>()

    // Simula el almacenamiento de usuarios.
    private val usuariosDao = object : UsuarioDao {

        override suspend fun login(
            rut: String,
            hash: String,
            rol: String
        ): UsuarioEntity? {

            error("La autenticación debe realizarse remotamente")
        }

        override fun porRol(rol: String) = flowOf(
            usuariosGuardados.filter { it.rol == rol }
        )

        override suspend fun contar(): Int =
            usuariosGuardados.size

        override suspend fun insertar(
            usuario: UsuarioEntity
        ) {

            usuariosGuardados.add(usuario)
        }
    }

    // Simula los objetivos de aprendizaje.
    private val objetivosDao = object : ObjetivoDao {

        override fun todos() =
            flowOf(objetivosGuardados.toList())

        override fun asignados() = flowOf(
            objetivosGuardados.filter { it.asignado }
        )

        override fun observar(id: Int) = flowOf(
            objetivosGuardados.firstOrNull { it.id == id }
        )

        override suspend fun contar(): Int =
            objetivosGuardados.size

        override suspend fun insertar(
            objetivo: ObjetivoEntity
        ): Long {

            val id = objetivosGuardados.size + 101

            objetivosGuardados.add(
                objetivo.copy(id = id)
            )

            return id.toLong()
        }

        override suspend fun actualizar(
            objetivo: ObjetivoEntity
        ) {

            objetivosGuardados.removeAll {
                it.id == objetivo.id
            }

            objetivosGuardados.add(objetivo)
        }
    }

    // Simula los contenidos educativos.
    private val contenidosDao = object : ContenidoDao {

        override fun todos() =
            flowOf(contenidosGuardados.toList())

        override fun aprobadosDe(oaId: Int) = flowOf(
            contenidosGuardados.filter {
                it.oaId == oaId &&
                        it.estado == EstadoContenido.APROBADO
            }
        )

        override suspend fun insertar(
            contenido: ContenidoEntity
        ) {

            contenidosGuardados.add(contenido)
        }

        override suspend fun actualizar(
            contenido: ContenidoEntity
        ) {

            contenidosGuardados.removeAll {
                it.id == contenido.id
            }

            contenidosGuardados.add(contenido)
        }
    }

    // Simula las preguntas de aprendizaje.
    private val preguntasDao = object : PreguntaDao {

        override fun todas() =
            flowOf(preguntasGuardadas.toList())

        override suspend fun de(
            oaId: Int,
            tipo: String
        ): List<PreguntaEntity> {

            return preguntasGuardadas.filter {
                it.oaId == oaId && it.tipo == tipo
            }
        }

        override suspend fun insertar(
            pregunta: PreguntaEntity
        ) {

            preguntasGuardadas.add(pregunta)
        }
    }

    // Simula las respuestas de los estudiantes.
    private val respuestasDao = object : RespuestaDao {

        override fun deAlumno(rut: String) = flowOf(
            respuestasGuardadas.filter {
                it.rutAlumno == rut
            }
        )

        override fun todas() =
            flowOf(respuestasGuardadas.toList())

        override suspend fun ultimoIntento(
            rut: String,
            oaId: Int,
            tipo: String
        ): Int {

            return respuestasGuardadas.filter {
                it.rutAlumno == rut &&
                        it.oaId == oaId &&
                        it.tipo == tipo
            }.maxOfOrNull { it.intento } ?: 0
        }

        override suspend fun insertar(
            respuesta: RespuestaEntity
        ) {

            respuestasGuardadas.add(respuesta)
        }
    }

    // Simula los informes pedagógicos.
    private val informesDao = object : InformeDao {

        override fun todos() =
            flowOf(informesGuardados.toList())

        override suspend fun guardar(
            informe: InformeEntity
        ) {

            informesGuardados.removeAll {
                it.clave == informe.clave
            }

            informesGuardados.add(informe)
        }
    }

    private val repository = AulaRepository(
        usuarios = usuariosDao,
        objetivos = objetivosDao,
        contenidos = contenidosDao,
        preguntas = preguntasDao,
        respuestas = respuestasDao,
        informes = informesDao
    )

    // ---------- Carga inicial ----------

    @Test
    fun cargaInicialGeneraDatosFicticios() = runBlocking {

        repository.sembrarSiVacio()

        assertEquals(4, usuariosGuardados.size)
        assertEquals(4, objetivosGuardados.size)
        assertEquals(5, contenidosGuardados.size)
        assertEquals(16, preguntasGuardadas.size)

        val ids = objetivosGuardados.map {
            it.id
        }.toSet()

        // Comprueba que se utilicen los IDs devueltos por el DAO.
        assertTrue(
            contenidosGuardados.all {
                it.oaId in ids
            }
        )

        assertTrue(
            preguntasGuardadas.all {
                it.oaId in ids
            }
        )

        assertTrue(
            objetivosGuardados.all {
                it.id >= 101
            }
        )

        val estudiantes =
            repository.estudiantes().first()

        assertEquals(2, estudiantes.size)

        assertTrue(
            estudiantes.all {
                it.rol == Rol.ESTUDIANTE
            }
        )
    }

    @Test
    fun cargaInicialNoDuplicaRegistros() = runBlocking {

        repository.sembrarSiVacio()
        repository.sembrarSiVacio()

        assertEquals(4, usuariosGuardados.size)
        assertEquals(4, objetivosGuardados.size)
        assertEquals(5, contenidosGuardados.size)
        assertEquals(16, preguntasGuardadas.size)
    }

    // ---------- Objetivos ----------

    @Test
    fun consultarYActualizarObjetivos() = runBlocking {

        repository.sembrarSiVacio()

        val objetivos = repository.objetivos().first()

        assertEquals(4, objetivos.size)

        val asignados =
            repository.objetivosAsignados().first()

        assertEquals(2, asignados.size)

        val objetivo = asignados.first()

        val actualizado = objetivo.copy(
            asignado = false,
            intentosPrueba = 3
        )

        repository.actualizarObjetivo(actualizado)

        val recuperado =
            repository.observarObjetivo(objetivo.id).first()

        assertEquals(actualizado, recuperado)

        assertEquals(
            1,
            repository.objetivosAsignados().first().size
        )

        assertNull(
            repository.observarObjetivo(999).first()
        )
    }

    // ---------- Contenidos ----------

    @Test
    fun crearYActualizarContenidoLocal() = runBlocking {

        val contenido = ContenidoEntity(
            id = 1,
            oaId = 101,
            titulo = "Contenido de prueba",
            explicacion = "Explicación educativa",
            ejemplo = "Dos más dos es cuatro",
            icono = "📚",
            estado = EstadoContenido.PENDIENTE
        )

        repository.crearContenido(contenido)

        assertEquals(
            contenido,
            repository.contenidos().first().single()
        )

        assertTrue(
            repository.contenidosAprobados(101)
                .first().isEmpty()
        )

        val aprobado = contenido.copy(
            estado = EstadoContenido.APROBADO
        )

        repository.actualizarContenido(aprobado)

        assertEquals(
            aprobado,
            repository.contenidosAprobados(101)
                .first().single()
        )

        assertTrue(
            repository.contenidosAprobados(999)
                .first().isEmpty()
        )
    }

    // ---------- Preguntas ----------

    @Test
    fun consultarPreguntasPorObjetivoYTipo() = runBlocking {

        val pregunta = PreguntaEntity(
            id = 1,
            oaId = 101,
            tipo = TipoPregunta.DESAFIO,
            dificultad = 1,
            enunciado = "¿Cuánto es 2 más 2?",
            opcionA = "3",
            opcionB = "4",
            opcionC = "5",
            correcta = 1
        )

        repository.crearPregunta(pregunta)

        assertEquals(
            1,
            repository.preguntas().first().size
        )

        assertEquals(
            listOf(pregunta),
            repository.preguntasDe(
                101,
                TipoPregunta.DESAFIO
            )
        )

        assertTrue(
            repository.preguntasDe(
                101,
                TipoPregunta.PRUEBA
            ).isEmpty()
        )
    }

    // ---------- Respuestas ----------

    @Test
    fun guardarRespuestasYConsultarIntentos() = runBlocking {

        val respuesta = RespuestaEntity(
            id = 1,
            rutAlumno = "111111111",
            oaId = 101,
            preguntaId = 1,
            tipo = TipoPregunta.PRUEBA,
            dificultad = 1,
            intento = 1,
            elegida = 1,
            esCorrecta = true,
            fecha = 1700000000000L
        )

        assertEquals(
            0,
            repository.ultimoIntento(
                "111111111",
                101,
                TipoPregunta.PRUEBA
            )
        )

        repository.guardarRespuesta(respuesta)

        repository.guardarRespuesta(
            respuesta.copy(
                id = 2,
                intento = 2
            )
        )

        repository.guardarRespuesta(
            respuesta.copy(
                id = 3,
                rutAlumno = "222222222"
            )
        )

        assertEquals(
            3,
            repository.respuestas().first().size
        )

        assertEquals(
            2,
            repository.respuestasDeAlumno("111111111")
                .first().size
        )

        assertEquals(
            2,
            repository.ultimoIntento(
                "111111111",
                101,
                TipoPregunta.PRUEBA
            )
        )
    }

    // ---------- Informes ----------

    @Test
    fun guardarInformeYActualizarValidacion() = runBlocking {

        val informe = InformeEntity(
            clave = "111111111-101",
            rutAlumno = "111111111",
            oaId = 101,
            rendimiento = 80,
            fortalezas = "Resuelve sumas",
            refuerzo = "Practicar multiplicaciones",
            fecha = "2026-11-24",
            validado = false
        )

        repository.guardarInforme(informe)

        assertEquals(
            informe,
            repository.informes().first().single()
        )

        val validado = informe.copy(
            validado = true
        )

        repository.guardarInforme(validado)

        assertEquals(
            1,
            repository.informes().first().size
        )

        assertEquals(
            validado,
            repository.informes().first().single()
        )
    }
}