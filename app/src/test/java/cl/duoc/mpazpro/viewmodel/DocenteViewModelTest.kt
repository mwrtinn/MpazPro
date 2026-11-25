package cl.duoc.mpazpro.viewmodel

import org.junit.Assert.*
import org.junit.Test

// Pruebas automatizadas de la gestión docente.
class DocenteViewModelTest : BasePruebaRemota() {

    // Prepara las respuestas utilizadas al cargar el panel.
    private fun prepararDatos() {

        responder(listOf(objetivo()))
        responder(listOf(contenido()))
        responder(listOf(pregunta()))
        responder(listOf(estudiante()))
        responder(listOf(respuesta()))
        responder(listOf(informe()))
    }

    private suspend fun crearModelo(): DocenteViewModel {

        prepararDatos()

        val vm = registrar(
            DocenteViewModel(repository)
        )

        esperar(vm)

        assertNull(vm.errorRemoto.value)

        return vm
    }

    // Completa un formulario válido de contenido.
    private fun completarContenido(
        vm: DocenteViewModel
    ) {

        vm.onContenidoOaChanged(1)

        vm.onTituloChanged(
            "Aprendiendo a multiplicar"
        )

        vm.onExplicacionChanged(
            "Multiplicar consiste en sumar una cantidad varias veces."
        )

        vm.onEjemploChanged(
            "Tres veces dos es igual a seis."
        )

        vm.onIconoChanged("📚")
    }

    // Completa un formulario válido de pregunta.
    private fun completarPregunta(
        vm: DocenteViewModel
    ) {

        vm.onPreguntaOaChanged(1)

        vm.onEnunciadoChanged(
            "¿Cuánto es cuatro más cinco?"
        )

        vm.onOpcionAChanged("8")
        vm.onOpcionBChanged("9")
        vm.onOpcionCChanged("10")

        vm.onTipoChanged("DESAFIO")
        vm.onDificultadChanged(1)
        vm.onCorrectaChanged(1)
    }

    @Test
    fun cargaDatosDelPanelDocente() = prueba {

        val vm = crearModelo()

        assertEquals(1, vm.objetivos.value.size)
        assertEquals(1, vm.contenidos.value.size)
        assertEquals(1, vm.preguntas.value.size)
        assertEquals(1, vm.filas.value.size)

        assertFalse(vm.cargando.value)
    }

    @Test
    fun recargarMantieneDatosDelServidor() = prueba {

        val vm = crearModelo()

        prepararDatos()

        vm.recargar()

        esperar(vm)

        assertEquals(1, vm.objetivos.value.size)
        assertEquals(1, vm.contenidos.value.size)

        assertNull(vm.errorRemoto.value)
    }

    @Test
    fun modificarAsignacionDeObjetivo() = prueba {

        val vm = crearModelo()

        responder(
            objetivo(asignado = false)
        )

        vm.alternarAsignado(
            vm.objetivos.value.first(),
            false
        )

        esperar(vm)

        assertFalse(
            vm.objetivos.value.first().asignado
        )

        assertTrue(vm.filas.value.isEmpty())
    }

    @Test
    fun abrirYCerrarConfiguracion() = prueba {

        val vm = crearModelo()

        val objetivo =
            vm.objetivos.value.first()

        vm.abrirConfig(objetivo)

        assertEquals(
            objetivo,
            vm.config.value.objetivo
        )

        assertEquals(
            "3",
            vm.config.value.valor
        )

        vm.cerrarConfig()

        assertNull(vm.config.value.objetivo)
    }

    @Test
    fun rechazaCantidadInvalidaDeIntentos() = prueba {

        val vm = crearModelo()

        vm.abrirConfig(
            vm.objetivos.value.first()
        )

        vm.onIntentosChanged("0")

        vm.guardarConfig()

        assertNotNull(vm.config.value.error)

        assertEquals(
            3,
            vm.objetivos.value.first()
                .intentosPrueba
        )
    }

    @Test
    fun guardarCantidadValidaDeIntentos() = prueba {

        val vm = crearModelo()

        vm.abrirConfig(
            vm.objetivos.value.first()
        )

        vm.onIntentosChanged("2")

        responder(
            objetivo().copy(
                intentosPrueba = 2
            )
        )

        vm.guardarConfig()

        esperar(vm)

        assertEquals(
            2,
            vm.objetivos.value.first()
                .intentosPrueba
        )

        assertNull(vm.config.value.objetivo)
    }

    @Test
    fun contenidoVacioNoEsValido() = prueba {

        val vm = crearModelo()

        assertFalse(vm.validarContenido())

        vm.crearContenido()

        assertNotNull(
            vm.contenido.value.errores.oa
        )

        assertNotNull(
            vm.contenido.value.errores.titulo
        )

        assertEquals(
            1,
            vm.contenidos.value.size
        )
    }

    @Test
    fun crearContenidoValido() = prueba {

        val vm = crearModelo()

        completarContenido(vm)

        assertTrue(vm.validarContenido())

        responder(
            contenido().copy(
                id = 2,
                titulo = "Aprendiendo a multiplicar"
            ),
            201
        )

        vm.crearContenido()

        esperar(vm)

        assertEquals(
            2,
            vm.contenidos.value.size
        )

        assertTrue(
            vm.contenido.value.guardado
        )

        vm.limpiarMensajes()

        assertFalse(
            vm.contenido.value.guardado
        )
    }

    @Test
    fun editarContenidoCargaFormulario() = prueba {

        val vm = crearModelo()

        val contenido =
            vm.contenidos.value.first()

        vm.editarContenido(contenido)

        assertEquals(
            contenido.id,
            vm.contenidoEditando.value
        )

        assertEquals(
            contenido.titulo,
            vm.contenido.value.titulo
        )

        vm.cancelarEdicionContenido()

        assertNull(
            vm.contenidoEditando.value
        )
    }

    @Test
    fun guardarEdicionNoDuplicaContenido() = prueba {

        val vm = crearModelo()

        vm.editarContenido(
            vm.contenidos.value.first()
        )

        vm.onTituloChanged(
            "Contenido educativo actualizado"
        )

        responder(
            contenido().copy(
                titulo = "Contenido educativo actualizado"
            )
        )

        vm.crearContenido()

        esperar(vm)

        assertNull(vm.errorRemoto.value)

        assertEquals(
            1,
            vm.contenidos.value.size
        )

        assertEquals(
            "Contenido educativo actualizado",
            vm.contenidos.value.first().titulo
        )

        assertNull(
            vm.contenidoEditando.value
        )
    }

    @Test
    fun eliminarContenidoActualizaListado() = prueba {

        val vm = crearModelo()

        sinContenido()

        vm.eliminarContenido(
            vm.contenidos.value.first()
        )

        esperar(vm)

        assertTrue(
            vm.contenidos.value.isEmpty()
        )
    }

    @Test
    fun eliminarContenidoEnEdicionLimpiaFormulario() = prueba {

        val vm = crearModelo()

        val contenido =
            vm.contenidos.value.first()

        vm.editarContenido(contenido)

        sinContenido()

        vm.eliminarContenido(contenido)

        esperar(vm)

        assertNull(
            vm.contenidoEditando.value
        )

        assertTrue(
            vm.contenidos.value.isEmpty()
        )
    }

    @Test
    fun imagenLocalSeConservaEnFormulario() = prueba {

        val vm = crearModelo()

        val ruta =
            "/storage/emulated/0/foto.jpg"

        vm.onImagenChanged(ruta)

        assertEquals(
            ruta,
            vm.contenido.value.imagenPath
        )

        vm.onImagenChanged(null)

        assertNull(
            vm.contenido.value.imagenPath
        )
    }

    @Test
    fun preguntaVaciaNoEsValida() = prueba {

        val vm = crearModelo()

        assertFalse(vm.validarPregunta())

        vm.crearPregunta()

        assertNotNull(
            vm.pregunta.value.errores.oa
        )

        assertNotNull(
            vm.pregunta.value.errores.enunciado
        )
    }

    @Test
    fun preguntaConAlternativasRepetidasEsInvalida() = prueba {

        val vm = crearModelo()

        completarPregunta(vm)

        vm.onOpcionBChanged("8")
        vm.onOpcionCChanged("8")

        assertFalse(vm.validarPregunta())

        assertNotNull(
            vm.pregunta.value.errores.opcionB
        )

        assertNotNull(
            vm.pregunta.value.errores.opcionC
        )
    }

    @Test
    fun crearPreguntaValida() = prueba {

        val vm = crearModelo()

        completarPregunta(vm)

        assertTrue(vm.validarPregunta())

        responder(
            pregunta().copy(id = 2),
            201
        )

        vm.crearPregunta()

        esperar(vm)

        assertEquals(
            2,
            vm.preguntas.value.size
        )

        assertTrue(
            vm.pregunta.value.guardado
        )
    }

    @Test
    fun editarPreguntaCargaFormulario() = prueba {

        val vm = crearModelo()

        val pregunta =
            vm.preguntas.value.first()

        vm.editarPregunta(pregunta)

        assertEquals(
            pregunta.id,
            vm.preguntaEditando.value
        )

        assertEquals(
            pregunta.enunciado,
            vm.pregunta.value.enunciado
        )

        vm.cancelarEdicionPregunta()

        assertNull(
            vm.preguntaEditando.value
        )
    }

    @Test
    fun guardarEdicionNoDuplicaPregunta() = prueba {

        val vm = crearModelo()

        vm.editarPregunta(
            vm.preguntas.value.first()
        )

        vm.onEnunciadoChanged(
            "¿Cuánto es tres más tres?"
        )

        vm.onCorrectaChanged(2)

        responder(
            pregunta().copy(
                enunciado = "¿Cuánto es tres más tres?",
                correcta = 2
            )
        )

        vm.crearPregunta()

        esperar(vm)

        assertNull(vm.errorRemoto.value)

        assertEquals(
            1,
            vm.preguntas.value.size
        )

        assertEquals(
            2,
            vm.preguntas.value.first().correcta
        )

        assertNull(
            vm.preguntaEditando.value
        )
    }

    @Test
    fun eliminarPreguntaActualizaListado() = prueba {

        val vm = crearModelo()

        sinContenido()

        vm.eliminarPregunta(
            vm.preguntas.value.first()
        )

        esperar(vm)

        assertTrue(
            vm.preguntas.value.isEmpty()
        )
    }

    @Test
    fun eliminarPreguntaEnEdicionLimpiaFormulario() = prueba {

        val vm = crearModelo()

        val pregunta =
            vm.preguntas.value.first()

        vm.editarPregunta(pregunta)

        sinContenido()

        vm.eliminarPregunta(pregunta)

        esperar(vm)

        assertNull(
            vm.preguntaEditando.value
        )

        assertTrue(
            vm.preguntas.value.isEmpty()
        )
    }

    @Test
    fun conflictoImpideEliminarPreguntaRespondida() = prueba {

        val vm = crearModelo()

        responder(
            mapOf(
                "mensaje" to "Pregunta con respuestas"
            ),
            409
        )

        vm.eliminarPregunta(
            vm.preguntas.value.first()
        )

        esperar(vm)

        assertEquals(
            1,
            vm.preguntas.value.size
        )

        assertTrue(
            vm.errorRemoto.value.orEmpty()
                .contains("conflicto")
        )
    }

    @Test
    fun generarInformeActualizaResultados() = prueba {

        val vm = crearModelo()

        responder(informe())

        vm.generarInforme(
            vm.filas.value.first()
        )

        esperar(vm)

        assertNotNull(
            vm.filas.value.first().informe
        )

        assertEquals(
            100,
            vm.filas.value.first()
                .informe?.rendimiento
        )
    }

    @Test
    fun validarInformeActualizaEstado() = prueba {

        val vm = crearModelo()

        responder(informe())

        vm.generarInforme(
            vm.filas.value.first()
        )

        esperar(vm)

        val informe = requireNotNull(
            vm.filas.value.first().informe
        )

        responder(
            informe(validado = true)
        )

        vm.validarInforme(informe)

        esperar(vm)

        assertTrue(
            vm.filas.value.first()
                .informe?.validado == true
        )
    }
}