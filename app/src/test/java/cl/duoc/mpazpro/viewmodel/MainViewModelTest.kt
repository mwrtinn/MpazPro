package cl.duoc.mpazpro.viewmodel

import cl.duoc.mpazpro.data.remote.LoginResponse
import cl.duoc.mpazpro.data.remote.SesionRemota
import cl.duoc.mpazpro.navigation.NavigationEvent
import cl.duoc.mpazpro.navigation.Screen
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Assert.*
import org.junit.Test

// Pruebas de navegación y cierre de sesión.
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private lateinit var vm: MainViewModel

    @Before
    fun preparar() {

        Dispatchers.setMain(
            StandardTestDispatcher()
        )

        SesionRemota.cerrar()

        vm = MainViewModel()
    }

    @After
    fun finalizar() {

        vm.viewModelScope.cancel()

        SesionRemota.cerrar()

        Dispatchers.resetMain()
    }

    @Test
    fun navegarEmiteDestinoCorrecto() = runTest {

        val evento = async(
            start = CoroutineStart.UNDISPATCHED
        ) {

            vm.navigationEvents.first()
        }

        val destino = Screen.Login("DOCENTE")

        vm.navigateTo(destino)

        advanceUntilIdle()

        assertEquals(
            NavigationEvent.NavigateTo(destino),
            evento.await()
        )
    }

    @Test
    fun navegarAtrasEmiteEventoCorrecto() = runTest {

        val evento = async(
            start = CoroutineStart.UNDISPATCHED
        ) {

            vm.navigationEvents.first()
        }

        vm.navigateBack()

        advanceUntilIdle()

        assertEquals(
            NavigationEvent.PopBackStack,
            evento.await()
        )
    }

    @Test
    fun navegarArribaEmiteEventoCorrecto() = runTest {

        val evento = async(
            start = CoroutineStart.UNDISPATCHED
        ) {

            vm.navigationEvents.first()
        }

        vm.navigateUp()

        advanceUntilIdle()

        assertEquals(
            NavigationEvent.NavigateUp,
            evento.await()
        )
    }

    @Test
    fun cerrarSesionEliminaTokenYRegresaAlInicio() = runTest {

        SesionRemota.iniciar(
            LoginResponse(
                rut = "123456785",
                nombre = "Docente de prueba",
                rol = "DOCENTE",
                curso = "3 Básico",
                token = "token-ficticio"
            )
        )

        val evento = async(
            start = CoroutineStart.UNDISPATCHED
        ) {

            vm.navigationEvents.first()
        }

        vm.cerrarSesion()

        advanceUntilIdle()

        assertNull(SesionRemota.usuario)
        assertTrue(SesionRemota.token.isNullOrBlank())

        assertEquals(
            NavigationEvent.NavigateTo(
                route = Screen.Inicio,
                popUpToRoute = Screen.Inicio,
                inclusive = true
            ),
            evento.await()
        )
    }
}