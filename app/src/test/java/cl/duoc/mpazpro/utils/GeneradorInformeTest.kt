package cl.duoc.mpazpro.utils

import cl.duoc.mpazpro.data.local.entity.RespuestaEntity
import cl.duoc.mpazpro.model.TipoPregunta
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneradorInformeTest {

    private fun r(tipo: String, nivel: Int, intento: Int, ok: Boolean) = RespuestaEntity(
        rutAlumno = "111111111", oaId = 1, preguntaId = 1, tipo = tipo, dificultad = nivel,
        intento = intento, elegida = 0, esCorrecta = ok, fecha = 0L
    )

    @Test
    fun sinRespuestasNoInventaResultados() {
        val resumen = GeneradorInforme.generar(emptyList())
        assertEquals(0, resumen.rendimiento)
        assertEquals(0, resumen.totalRespuestas)
    }

    @Test
    fun usaElUltimoIntentoDeLaPruebaFinal() {
        val respuestas = listOf(
            r(TipoPregunta.PRUEBA, 1, 1, false), r(TipoPregunta.PRUEBA, 2, 1, false),
            r(TipoPregunta.PRUEBA, 1, 2, true), r(TipoPregunta.PRUEBA, 2, 2, true)
        )
        assertEquals(100, GeneradorInforme.generar(respuestas).rendimiento)
    }

    @Test
    fun sinPruebaUsaLasActividadesDePractica() {
        val respuestas = listOf(r(TipoPregunta.DESAFIO, 1, 1, true), r(TipoPregunta.DESAFIO, 2, 1, false))
        assertEquals(50, GeneradorInforme.generar(respuestas).rendimiento)
    }

    @Test
    fun separaFortalezasYAspectosAReforzarPorNivel() {
        val respuestas = listOf(
            r(TipoPregunta.DESAFIO, 1, 1, true),
            r(TipoPregunta.DESAFIO, 2, 1, false)
        )
        val resumen = GeneradorInforme.generar(respuestas)
        assertTrue(resumen.fortalezas.contains("nivel 1"))
        assertTrue(resumen.refuerzo.contains("nivel 2"))
    }

    @Test
    fun claveDeInformeEsUnicaPorAlumnoYObjetivo() {
        assertEquals("111111111-3", claveInforme("111111111", 3))
    }
}
