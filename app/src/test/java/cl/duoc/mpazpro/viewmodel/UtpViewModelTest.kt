package cl.duoc.mpazpro.viewmodel

import org.junit.Assert.*
import org.junit.Test

// Pruebas del flujo de revisión pedagógica.
class UtpViewModelTest : BasePruebaRemota() {

    private suspend fun crearModelo(): UtpViewModel {

        responder(listOf(objetivo()))
        responder(listOf(contenido()))

        val vm = registrar(
            UtpViewModel(repository)
        )

        esperar(vm)

        assertNull(vm.errorRemoto.value)

        return vm
    }

    @Test
    fun cargaContenidosPendientes() = prueba {

        val vm = crearModelo()

        val contenidos = vm.contenidos.value

        assertEquals(1, contenidos.size)

        assertEquals(
            "PENDIENTE",
            contenidos.first().contenido.estado
        )
    }

    @Test
    fun aprobarContenidoActualizaEstado() = prueba {

        val vm = crearModelo()

        responder(contenido("APROBADO"))

        vm.aprobar(
            vm.contenidos.value.first().contenido
        )

        esperar(vm)

        assertEquals(
            "APROBADO",
            vm.contenidos.value.first()
                .contenido.estado
        )

        assertNull(vm.errorRemoto.value)
    }

    @Test
    fun abrirYCerrarRechazo() = prueba {

        val vm = crearModelo()

        val contenido =
            vm.contenidos.value.first().contenido

        vm.abrirRechazo(contenido)

        assertEquals(
            contenido,
            vm.revision.value.contenido
        )

        vm.cerrarRechazo()

        assertNull(
            vm.revision.value.contenido
        )
    }

    @Test
    fun rechazoSinObservacionEsInvalido() = prueba {

        val vm = crearModelo()

        vm.abrirRechazo(
            vm.contenidos.value.first().contenido
        )

        vm.onObservacionChanged("")

        vm.confirmarRechazo()

        assertNotNull(vm.revision.value.error)

        assertEquals(
            "PENDIENTE",
            vm.contenidos.value.first()
                .contenido.estado
        )
    }

    @Test
    fun rechazoConObservacionActualizaContenido() = prueba {

        val vm = crearModelo()

        val observacion =
            "Mejorar la explicación para los estudiantes."

        vm.abrirRechazo(
            vm.contenidos.value.first().contenido
        )

        vm.onObservacionChanged(observacion)

        responder(
            contenido("RECHAZADO").copy(
                observacionUtp = observacion
            )
        )

        vm.confirmarRechazo()

        esperar(vm)

        val actualizado =
            vm.contenidos.value.first().contenido

        assertEquals(
            "RECHAZADO",
            actualizado.estado
        )

        assertEquals(
            observacion,
            actualizado.observacionUtp
        )

        assertNull(vm.revision.value.contenido)
        assertNull(vm.errorRemoto.value)
    }

    @Test
    fun recargarActualizaContenidos() = prueba {

        val vm = crearModelo()

        responder(listOf(objetivo()))

        responder(
            listOf(
                contenido("APROBADO"),
                contenido("PENDIENTE").copy(id = 2)
            )
        )

        vm.recargar()

        esperar(vm)

        assertEquals(
            2,
            vm.contenidos.value.size
        )
    }

    @Test
    fun errorDePermisosConservaContenido() = prueba {

        val vm = crearModelo()

        responder(
            mapOf("mensaje" to "Acceso denegado"),
            403
        )

        vm.aprobar(
            vm.contenidos.value.first().contenido
        )

        esperar(vm)

        assertNotNull(vm.errorRemoto.value)

        assertTrue(
            vm.errorRemoto.value.orEmpty()
                .contains("permisos")
        )

        assertEquals(
            "PENDIENTE",
            vm.contenidos.value.first()
                .contenido.estado
        )
    }

    @Test
    fun manejaErroresHttp() = prueba {

        val vm = crearModelo()

        val errores = mapOf(
            400 to "Datos inválidos",
            401 to "Sesión",
            403 to "permisos",
            404 to "registro",
            409 to "conflicto",
            500 to "servidor"
        )

        errores.forEach { (codigo, mensaje) ->

            responder(
                mapOf("mensaje" to "Error simulado"),
                codigo
            )

            vm.recargar()

            esperar(vm)

            assertTrue(
                vm.errorRemoto.value.orEmpty()
                    .contains(mensaje)
            )

            assertFalse(vm.cargando.value)
        }
    }
}