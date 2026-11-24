package cl.duoc.mpazpro.data.remote

import org.junit.Assert.*
import org.junit.Test

// Verifica la conversión entre datos remotos y entidades locales.
class MapeadoresRemotosTest {

    private fun objetivoPrueba() = ObjetivoDto(
        id = 1,
        asignatura = "Matemática",
        unidad = "Unidad 1",
        codigo = "MA03 OA 01",
        eje = "Números",
        descripcion = "Resolver ejercicios matemáticos",
        referencia = "Currículum Nacional",
        asignado = true,
        intentosPrueba = 3
    )

    private fun contenidoPrueba() = ContenidoDto(
        id = 2,
        oaId = 1,
        titulo = "Aprendiendo a sumar",
        explicacion = "Sumamos cantidades",
        ejemplo = "2 + 3 = 5",
        icono = "📚",
        estado = "APROBADO",
        observacionUtp = "",
        imagenPath = null
    )

    private fun preguntaPrueba() = PreguntaDto(
        id = 3,
        oaId = 1,
        tipo = "DESAFIO",
        dificultad = 1,
        enunciado = "¿Cuánto es 2 más 3?",
        opcionA = "4",
        opcionB = "5",
        opcionC = "6",
        correcta = 1
    )

    @Test
    fun usuarioRemotoNoAlmacenaContrasena() {

        val dto = UsuarioDto(
            rut = "111111111",
            nombre = "Estudiante de prueba",
            rol = "ESTUDIANTE",
            curso = "3 Básico"
        )

        val entidad = dto.aEntidad()

        assertEquals(dto.rut, entidad.rut)
        assertEquals(dto.nombre, entidad.nombre)
        assertEquals(dto.rol, entidad.rol)
        assertEquals(dto.curso, entidad.curso)
        assertEquals("", entidad.contrasena)
    }

    @Test
    fun objetivoConservaTodosSusDatos() {

        val dto = objetivoPrueba()

        val entidad = dto.aEntidad()

        assertEquals(1, entidad.id)
        assertTrue(entidad.asignado)
        assertEquals(3, entidad.intentosPrueba)
        assertEquals(dto, entidad.aDto())
    }

    @Test
    fun objetivoNuevoNoEnviaIdentificadorLocal() {

        val entidad = objetivoPrueba()
            .aEntidad()
            .copy(id = 0)

        assertNull(entidad.aDto().id)
    }

    @Test
    fun contenidoConservaEstadoDeRevision() {

        val dto = contenidoPrueba().copy(
            estado = "RECHAZADO",
            observacionUtp = "Mejorar explicación"
        )

        val entidad = dto.aEntidad()

        assertEquals("RECHAZADO", entidad.estado)
        assertEquals(
            "Mejorar explicación",
            entidad.observacionUtp
        )

        assertEquals(dto, entidad.aDto())
    }

    @Test
    fun contenidoNuevoNoEnviaIdentificador() {

        val entidad = contenidoPrueba()
            .aEntidad()
            .copy(id = 0)

        assertNull(entidad.aDto().id)
    }

    @Test
    fun contenidoNoEnviaRutaPrivadaDelCelular() {

        val entidad = contenidoPrueba()
            .aEntidad()
            .copy(
                imagenPath =
                    "/storage/emulated/0/foto.jpg"
            )

        assertNull(entidad.aDto().imagenPath)
    }

    @Test
    fun preguntaConservaAlternativaCorrecta() {

        val dto = preguntaPrueba()

        val entidad = dto.aEntidad()

        assertEquals(1, entidad.correcta)
        assertEquals("5", entidad.opcionB)
        assertEquals(dto, entidad.aDto())
    }

    @Test
    fun preguntaEstudianteNoInventaRespuestaCorrecta() {

        val dto = preguntaPrueba().copy(
            correcta = null
        )

        val entidad = dto.aEntidad()

        assertEquals(-1, entidad.correcta)
    }

    @Test
    fun preguntaNuevaNoEnviaIdentificador() {

        val entidad = preguntaPrueba()
            .aEntidad()
            .copy(id = 0)

        assertNull(entidad.aDto().id)
    }

    @Test
    fun respuestaConservaCorreccionDelServidor() {

        val dto = RespuestaDto(
            id = 1,
            rutAlumno = "111111111",
            oaId = 1,
            preguntaId = 3,
            tipo = "DESAFIO",
            dificultad = 1,
            intento = 1,
            elegida = 1,
            esCorrecta = true,
            fecha = 1700000000000L,
            correcta = 1
        )

        val entidad = dto.aEntidad()

        assertEquals(dto.id, entidad.id)
        assertEquals(dto.rutAlumno, entidad.rutAlumno)
        assertEquals(dto.oaId, entidad.oaId)
        assertEquals(dto.preguntaId, entidad.preguntaId)
        assertEquals(dto.tipo, entidad.tipo)
        assertEquals(dto.dificultad, entidad.dificultad)
        assertEquals(dto.intento, entidad.intento)
        assertEquals(dto.elegida, entidad.elegida)
        assertEquals(dto.fecha, entidad.fecha)
        assertTrue(entidad.esCorrecta)
    }

    @Test
    fun informeConservaRendimientoYValidacion() {

        val dto = InformeDto(
            clave = "111111111-1",
            rutAlumno = "111111111",
            oaId = 1,
            rendimiento = 80,
            fortalezas = "Resuelve sumas",
            refuerzo = "Practicar multiplicaciones",
            fecha = "2026-11-24",
            validado = true
        )

        val entidad = dto.aEntidad()

        assertEquals(dto.clave, entidad.clave)
        assertEquals(dto.rutAlumno, entidad.rutAlumno)
        assertEquals(dto.oaId, entidad.oaId)
        assertEquals(dto.rendimiento, entidad.rendimiento)
        assertEquals(dto.fortalezas, entidad.fortalezas)
        assertEquals(dto.refuerzo, entidad.refuerzo)
        assertEquals(dto.fecha, entidad.fecha)
        assertTrue(entidad.validado)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rechazaObjetivoRemotoSinIdentificador() {

        objetivoPrueba().copy(id = null).aEntidad()
    }

    @Test(expected = IllegalArgumentException::class)
    fun rechazaContenidoRemotoSinIdentificador() {

        contenidoPrueba().copy(id = null).aEntidad()
    }

    @Test(expected = IllegalArgumentException::class)
    fun rechazaPreguntaRemotaSinIdentificador() {

        preguntaPrueba().copy(id = null).aEntidad()
    }
}