package cl.duoc.mpazpro.viewmodel

import androidx.lifecycle.viewModelScope
import cl.duoc.mpazpro.data.remote.*
import cl.duoc.mpazpro.repository.AulaRemotaRepository
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.flow.first
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.Executors

// Configuración compartida para las pruebas remotas.
@OptIn(ExperimentalCoroutinesApi::class)
abstract class BasePruebaRemota {

    protected lateinit var servidor: MockWebServer

    protected lateinit var repository: AulaRemotaRepository

    private lateinit var dispatcher: ExecutorCoroutineDispatcher

    private val gson = Gson()

    private val modelos =
        mutableListOf<OperacionRemotaViewModel>()

    @Before
    fun prepararBase() {

        dispatcher = Executors
            .newSingleThreadExecutor()
            .asCoroutineDispatcher()

        Dispatchers.setMain(dispatcher)

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
    fun finalizarBase() {

        modelos.forEach {
            it.viewModelScope.cancel()
        }

        modelos.clear()

        try {
            servidor.shutdown()
        } finally {
            Dispatchers.resetMain()
            dispatcher.close()
        }
    }

    // Ejecuta las pruebas en un hilo controlado.
    protected fun prueba(
        bloque: suspend () -> Unit
    ) {

        runBlocking(dispatcher) {

            withTimeout(20000) {
                bloque()
            }
        }
    }

    protected fun <T : OperacionRemotaViewModel> registrar(
        modelo: T
    ): T {

        modelos.add(modelo)
        return modelo
    }

    // Espera que termine una operación del ViewModel.
    protected suspend fun esperar(
        modelo: OperacionRemotaViewModel
    ) {

        withTimeout(15000) {

            modelo.cargando.first {
                !it
            }
        }
    }

    // Simula una respuesta JSON.
    protected fun responder(
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

    protected fun sinContenido() {

        servidor.enqueue(
            MockResponse()
                .setResponseCode(204)
        )
    }

    // ---------- Datos ficticios de prueba ----------

    protected fun objetivo(
        asignado: Boolean = true
    ) = ObjetivoDto(
        id = 1,
        asignatura = "Matemática",
        unidad = "Unidad 1",
        codigo = "MA03 OA 01",
        eje = "Números",
        descripcion = "Resolver ejercicios matemáticos",
        referencia = "Currículum Nacional",
        asignado = asignado,
        intentosPrueba = 3
    )

    protected fun contenido(
        estado: String = "PENDIENTE"
    ) = ContenidoDto(
        id = 1,
        oaId = 1,
        titulo = "Aprendiendo a sumar",
        explicacion = "Sumamos cantidades para obtener un total.",
        ejemplo = "Dos más tres es igual a cinco.",
        icono = "📚",
        estado = estado
    )

    protected fun pregunta() = PreguntaDto(
        id = 1,
        oaId = 1,
        tipo = "DESAFIO",
        dificultad = 1,
        enunciado = "¿Cuánto es dos más tres?",
        opcionA = "4",
        opcionB = "5",
        opcionC = "6",
        correcta = 1
    )

    protected fun estudiante() = UsuarioDto(
        rut = "111111111",
        nombre = "Estudiante de prueba",
        rol = "ESTUDIANTE",
        curso = "3 Básico"
    )

    protected fun respuesta() = RespuestaDto(
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

    protected fun informe(
        validado: Boolean = false
    ) = InformeDto(
        clave = "111111111-1",
        rutAlumno = "111111111",
        oaId = 1,
        rendimiento = 100,
        fortalezas = "Resuelve sumas correctamente",
        refuerzo = "Continuar practicando",
        fecha = "2026-11-24",
        validado = validado
    )
}