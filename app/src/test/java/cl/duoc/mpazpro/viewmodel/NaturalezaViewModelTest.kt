package cl.duoc.mpazpro.viewmodel

import cl.duoc.mpazpro.data.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.IOException

// Pruebas unitarias del minijuego educativo.
@OptIn(ExperimentalCoroutinesApi::class)
class NaturalezaViewModelTest {

    @Before
    fun preparar() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun finalizar() {
        Dispatchers.resetMain()
    }

    // Simula la API sin realizar solicitudes por Internet.
    private fun apiSimulada(
        respuesta: NaturalezaResponse
    ): NaturalezaApi {

        return object : NaturalezaApi {

            override suspend fun consultarEspecies(
                ids: String
            ): NaturalezaResponse {
                return respuesta
            }
        }
    }

    // Genera cinco especies ficticias para las pruebas.
    private fun respuestaValida(
        licencia: String = "cc-by"
    ): NaturalezaResponse {

        val especies = listOf(
            431220 to "Lapageria rosea",
            126404 to "Araucaria araucana",
            123353 to "Jubaea chilensis",
            407502 to "Nothofagus antarctica",
            533508 to "Porlieria chilensis"
        )

        return NaturalezaResponse(
            results = especies.map { (id, nombre) ->

                EspecieDto(
                    id = id,
                    name = nombre,
                    fotoPrincipal = FotoNaturalezaDto(
                        id = id.toLong(),
                        url = "https://example.org/$id/square.jpg",
                        attribution = "Autor de prueba",
                        imagenMediana =
                            "https://example.org/$id/medium.jpg",
                        licencia = licencia
                    ),
                    fotos = emptyList()
                )
            }
        )
    }

    @Test
    fun estadoInicialSinPreguntas() {

        val vm = NaturalezaViewModel(
            apiSimulada(respuestaValida())
        )

        val estado = vm.estado.value

        assertFalse(estado.cargando)
        assertFalse(estado.terminado)
        assertTrue(estado.preguntas.isEmpty())
        assertEquals(0, estado.puntos)
        assertEquals(0, estado.estrellas)
        assertNull(estado.error)
    }

    @Test
    fun iniciarJuegoCargaCincoPreguntas() = runTest {

        val vm = NaturalezaViewModel(
            apiSimulada(respuestaValida())
        )

        vm.iniciarJuego()

        assertTrue(vm.estado.value.cargando)

        advanceUntilIdle()

        val estado = vm.estado.value

        assertEquals(5, estado.preguntas.size)
        assertFalse(estado.cargando)
        assertNull(estado.error)

        estado.preguntas.forEach { pregunta ->

            assertEquals(4, pregunta.alternativas.size)

            assertEquals(
                4,
                pregunta.alternativas.toSet().size
            )

            assertTrue(
                pregunta.alternativas.contains(
                    pregunta.planta.nombre
                )
            )
        }
    }

    @Test
    fun respuestaCorrectaEntregaDiezPuntos() = runTest {

        val vm = NaturalezaViewModel(
            apiSimulada(respuestaValida())
        )

        vm.iniciarJuego()
        advanceUntilIdle()

        val pregunta = vm.estado.value.preguntaActual!!

        vm.responder(pregunta.planta.nombre)

        assertEquals(10, vm.estado.value.puntos)
        assertEquals(1, vm.estado.value.estrellas)
        assertTrue(vm.estado.value.respondida)
    }

    @Test
    fun respuestaIncorrectaNoEntregaPuntos() = runTest {

        val vm = NaturalezaViewModel(
            apiSimulada(respuestaValida())
        )

        vm.iniciarJuego()
        advanceUntilIdle()

        val pregunta = vm.estado.value.preguntaActual!!

        val incorrecta = pregunta.alternativas.first {
            it != pregunta.planta.nombre
        }

        vm.responder(incorrecta)

        assertEquals(0, vm.estado.value.puntos)
        assertTrue(vm.estado.value.respondida)
    }

    @Test
    fun impideResponderDosVeces() = runTest {

        val vm = NaturalezaViewModel(
            apiSimulada(respuestaValida())
        )

        vm.iniciarJuego()
        advanceUntilIdle()

        val correcta =
            vm.estado.value.preguntaActual!!.planta.nombre

        vm.responder(correcta)
        vm.responder(correcta)

        assertEquals(10, vm.estado.value.puntos)
    }

    @Test
    fun rechazaAlternativaInvalida() = runTest {

        val vm = NaturalezaViewModel(
            apiSimulada(respuestaValida())
        )

        vm.iniciarJuego()
        advanceUntilIdle()

        vm.responder("Alternativa inexistente")

        assertFalse(vm.estado.value.respondida)
        assertEquals(0, vm.estado.value.puntos)
    }

    @Test
    fun noAvanzaSinResponder() = runTest {

        val vm = NaturalezaViewModel(
            apiSimulada(respuestaValida())
        )

        vm.iniciarJuego()
        advanceUntilIdle()

        vm.siguiente()

        assertEquals(0, vm.estado.value.indice)
        assertFalse(vm.estado.value.terminado)
    }

    @Test
    fun avanzarLimpiaSeleccionAnterior() = runTest {

        val vm = NaturalezaViewModel(
            apiSimulada(respuestaValida())
        )

        vm.iniciarJuego()
        advanceUntilIdle()

        vm.responder(
            vm.estado.value.preguntaActual!!.planta.nombre
        )

        vm.siguiente()

        assertEquals(1, vm.estado.value.indice)
        assertNull(vm.estado.value.seleccion)
        assertEquals(10, vm.estado.value.puntos)
    }

    @Test
    fun completarPartidaEntregaCincuentaPuntos() = runTest {

        val vm = NaturalezaViewModel(
            apiSimulada(respuestaValida())
        )

        vm.iniciarJuego()
        advanceUntilIdle()

        repeat(5) {

            val pregunta = vm.estado.value.preguntaActual!!

            vm.responder(pregunta.planta.nombre)
            vm.siguiente()
        }

        assertTrue(vm.estado.value.terminado)
        assertEquals(50, vm.estado.value.puntos)
        assertEquals(5, vm.estado.value.estrellas)
    }

    @Test
    fun volverAJugarReiniciaPuntuacion() = runTest {

        val vm = NaturalezaViewModel(
            apiSimulada(respuestaValida())
        )

        vm.iniciarJuego()
        advanceUntilIdle()

        vm.responder(
            vm.estado.value.preguntaActual!!.planta.nombre
        )

        vm.volverAJugar()

        assertEquals(0, vm.estado.value.puntos)
        assertEquals(0, vm.estado.value.indice)
        assertNull(vm.estado.value.seleccion)
        assertFalse(vm.estado.value.terminado)
        assertEquals(5, vm.estado.value.preguntas.size)
    }

    @Test
    fun respuestaVaciaMuestraError() = runTest {

        val vm = NaturalezaViewModel(
            apiSimulada(
                NaturalezaResponse(
                    results = emptyList()
                )
            )
        )

        vm.iniciarJuego()
        advanceUntilIdle()

        assertNotNull(vm.estado.value.error)
        assertTrue(vm.estado.value.preguntas.isEmpty())
        assertFalse(vm.estado.value.cargando)
    }

    @Test
    fun rechazaFotografiasSinLicenciaPermitida() = runTest {

        val vm = NaturalezaViewModel(
            apiSimulada(
                respuestaValida(
                    licencia = "all-rights-reserved"
                )
            )
        )

        vm.iniciarJuego()
        advanceUntilIdle()

        assertNotNull(vm.estado.value.error)
        assertTrue(vm.estado.value.preguntas.isEmpty())
    }

    @Test
    fun errorDeConexionSeControlaCorrectamente() = runTest {

        val api = object : NaturalezaApi {

            override suspend fun consultarEspecies(
                ids: String
            ): NaturalezaResponse {

                throw IOException("Sin conexión")
            }
        }

        val vm = NaturalezaViewModel(api)

        vm.iniciarJuego()
        advanceUntilIdle()

        assertNotNull(vm.estado.value.error)

        assertTrue(
            vm.estado.value.error.orEmpty()
                .contains("Internet")
        )

        assertFalse(vm.estado.value.cargando)
    }
}