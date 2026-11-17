package cl.duoc.mpazpro.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SeguridadTest {
    @Test
    fun hashEsDeterministaYNoGuardaTextoPlano() {
        assertEquals(Seguridad.hash("1234"), Seguridad.hash("1234"))
        assertNotEquals("1234", Seguridad.hash("1234"))
        assertEquals(64, Seguridad.hash("1234").length)
    }

    @Test
    fun contrasenasDistintasGeneranHashDistinto() {
        assertNotEquals(Seguridad.hash("1234"), Seguridad.hash("4321"))
    }
}
