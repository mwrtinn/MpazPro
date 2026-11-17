package cl.duoc.mpazpro.utils

import cl.duoc.mpazpro.data.local.entity.PreguntaEntity
import cl.duoc.mpazpro.model.TipoPregunta
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EvaluadorTest {

    private val pregunta = PreguntaEntity(
        id = 1, oaId = 1, tipo = TipoPregunta.DESAFIO, dificultad = 1, enunciado = "P",
        opcionA = "a", opcionB = "b", opcionC = "c", correcta = 1
    )

    @Test
    fun corrigeRespuestas() {
        assertTrue(Evaluador.esCorrecta(pregunta, 1))
        assertFalse(Evaluador.esCorrecta(pregunta, 0))
    }

    @Test
    fun letrasYTextosDeAlternativas() {
        assertEquals('A', Evaluador.letra(0))
        assertEquals('C', Evaluador.letra(2))
        assertEquals("b", Evaluador.textoAlternativa(pregunta, 1))
    }

    @Test
    fun porcentajeManejaTotalCero() {
        assertEquals(0, Evaluador.porcentaje(0, 0))
        assertEquals(50, Evaluador.porcentaje(1, 2))
        assertEquals(100, Evaluador.porcentaje(3, 3))
    }

    @Test
    fun intentosDeLaPruebaFinalSeRespetan() {
        assertTrue(Evaluador.puedeRendirPrueba(intentosPermitidos = 2, intentosUsados = 1))
        assertFalse(Evaluador.puedeRendirPrueba(intentosPermitidos = 2, intentosUsados = 2))
        assertEquals(1, Evaluador.intentosRestantes(2, 1))
        assertEquals(0, Evaluador.intentosRestantes(2, 5))
    }
}
