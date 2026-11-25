package cl.duoc.mpazpro.data.remote

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

// Verifica el manejo de la sesión remota.
class SesionRemotaTest {

    @Before
    fun preparar() {
        SesionRemota.cerrar()
    }

    @After
    fun finalizar() {
        SesionRemota.cerrar()
    }

    private fun usuarioPrueba(
        rut: String = "123456785",
        rol: String = "DOCENTE",
        token: String = "token-ficticio"
    ): LoginResponse {

        return LoginResponse(
            rut = rut,
            nombre = "Usuario de prueba",
            rol = rol,
            curso = "3 Básico",
            token = token
        )
    }

    @Test
    fun sesionInicialSinUsuario() {

        assertNull(SesionRemota.usuario)
        assertTrue(SesionRemota.token.isNullOrBlank())
    }

    @Test
    fun iniciarSesionGuardaUsuario() {

        val usuario = usuarioPrueba()

        SesionRemota.iniciar(usuario)

        assertEquals(usuario, SesionRemota.usuario)
    }

    @Test
    fun iniciarSesionGuardaToken() {

        SesionRemota.iniciar(usuarioPrueba())

        assertEquals(
            "token-ficticio",
            SesionRemota.token
        )
    }

    @Test
    fun iniciarSesionGuardaRutYRol() {

        SesionRemota.iniciar(usuarioPrueba())

        assertEquals(
            "123456785",
            SesionRemota.rut
        )

        assertEquals(
            "DOCENTE",
            SesionRemota.rol
        )
    }

    @Test
    fun cerrarSesionEliminaUsuarioYToken() {

        SesionRemota.iniciar(usuarioPrueba())
        SesionRemota.cerrar()

        assertNull(SesionRemota.usuario)
        assertTrue(SesionRemota.token.isNullOrBlank())
    }

    @Test
    fun nuevaSesionReemplazaLaAnterior() {

        SesionRemota.iniciar(usuarioPrueba())

        SesionRemota.iniciar(
            usuarioPrueba(
                rut = "111111111",
                rol = "ESTUDIANTE",
                token = "otro-token-ficticio"
            )
        )

        assertEquals(
            "111111111",
            SesionRemota.rut
        )

        assertEquals(
            "ESTUDIANTE",
            SesionRemota.rol
        )

        assertEquals(
            "otro-token-ficticio",
            SesionRemota.token
        )
    }
}