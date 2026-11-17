package cl.duoc.mpazpro.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** Pruebas unitarias de las reglas de validación (no requieren Android). */
class ValidacionesTest {

    @Test
    fun rutFicticiosDelSeedSonValidos() {
        assertNull(Validaciones.validarRut("11.111.111-1"))
        assertNull(Validaciones.validarRut("22222222-2"))
        assertNull(Validaciones.validarRut("12.345.678-5"))
        assertNull(Validaciones.validarRut("134567899"))
    }

    @Test
    fun rutConDigitoVerificadorIncorrectoEsInvalido() {
        assertNotNull(Validaciones.validarRut("11.111.111-2"))
    }

    @Test
    fun rutVacioOMuyCortoEsInvalido() {
        assertEquals("El RUT es obligatorio", Validaciones.validarRut(""))
        assertNotNull(Validaciones.validarRut("123"))
    }

    @Test
    fun rutConLetrasEnElCuerpoEsInvalido() {
        assertNotNull(Validaciones.validarRut("11A11111-1"))
    }

    @Test
    fun normalizarRutQuitaPuntosYGuion() {
        assertEquals("111111111", Validaciones.normalizarRut("11.111.111-1"))
        assertEquals("12345678K", Validaciones.normalizarRut("12.345.678-k"))
    }

    @Test
    fun calcularDvCubreLosTresCasosEspeciales() {
        assertEquals('1', Validaciones.calcularDv("11111111"))
        assertEquals('K', Validaciones.calcularDv("10000013"))
        assertEquals('0', Validaciones.calcularDv("10000004"))
        assertNull(Validaciones.validarRut("10.000.013-K"))
        assertNull(Validaciones.validarRut("10000004-0"))
    }

    @Test
    fun contrasenaValidaLargoMinimo() {
        assertNull(Validaciones.validarContrasena("1234"))
        assertNotNull(Validaciones.validarContrasena("123"))
        assertNotNull(Validaciones.validarContrasena(""))
    }

    @Test
    fun intentosDeLaPruebaDebenEstarEntre1y3() {
        assertNull(Validaciones.validarIntentos("2"))
        assertNotNull(Validaciones.validarIntentos("0"))
        assertNotNull(Validaciones.validarIntentos("4"))
        assertNotNull(Validaciones.validarIntentos("dos"))
    }

    @Test
    fun preguntaYAlternativasValidanLargo() {
        assertNull(Validaciones.validarEnunciado("¿Cuánto es 40 + 25?"))
        assertNotNull(Validaciones.validarEnunciado("corta"))
        assertNull(Validaciones.validarAlternativa("65"))
        assertNotNull(Validaciones.validarAlternativa("  "))
    }

    @Test
    fun alternativasRepetidasSeRechazan() {
        assertNotNull(Validaciones.alternativaRepetida("55", listOf("55", "65")))
        assertNotNull(Validaciones.alternativaRepetida(" Raíz ", listOf("raíz")))
        assertNull(Validaciones.alternativaRepetida("75", listOf("55", "65")))
        assertNull(Validaciones.alternativaRepetida("", listOf("55")))
    }

    @Test
    fun contenidoValidaLargoMinimoDeLaExplicacion() {
        assertNotNull(Validaciones.validarExplicacion("muy corta"))
        assertNull(Validaciones.validarExplicacion("Contar de 5 en 5 es sumar 5 cada vez."))
        assertNotNull(Validaciones.validarTitulo(""))
        assertNull(Validaciones.validarEjemplo("10, 20, 30"))
    }

    @Test
    fun rechazarContenidoExigeObservacion() {
        assertNotNull(Validaciones.validarObservacion(""))
        assertNull(Validaciones.validarObservacion("Falta un ejemplo."))
    }
}
