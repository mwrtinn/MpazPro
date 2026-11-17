package cl.duoc.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mpazpro.data.local.PreferenciasDataStore
import cl.duoc.mpazpro.model.LoginErrores
import cl.duoc.mpazpro.model.LoginEvent
import cl.duoc.mpazpro.model.LoginUiState
import cl.duoc.mpazpro.model.Rol
import cl.duoc.mpazpro.repository.AulaRepository
import cl.duoc.mpazpro.utils.Seguridad
import cl.duoc.mpazpro.utils.Validaciones
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel de la pantalla de acceso. Los tres perfiles (estudiante, docente y UTP) ingresan con RUT.
 *
 * Responsabilidades:
 * 1. Mantener el estado del formulario ([LoginUiState]) y validarlo con [Validaciones].
 * 2. Autenticar contra Room (la contraseña se compara como hash SHA-256).
 * 3. Recordar el RUT en DataStore cuando el usuario lo pide.
 */
class LoginViewModel(
    private val repository: AulaRepository,
    private val preferencias: PreferenciasDataStore
) : ViewModel() {

    // Estado mutable interno y público de solo lectura (flujo de datos unidireccional)
    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    // Eventos de una sola vez (acceso correcto)
    private val _eventos = Channel<LoginEvent>(Channel.BUFFERED)
    val eventos: Flow<LoginEvent> = _eventos.receiveAsFlow()

    // Los datos de prueba se cargan una sola vez; el ingreso espera a que terminen
    private val carga = viewModelScope.launch { repository.sembrarSiVacio() }

    /** Se llama al entrar a la pantalla: reinicia el formulario y recupera el RUT recordado. */
    fun limpiar() {
        viewModelScope.launch {
            _state.value = LoginUiState(cargandoPreferencias = true)
            val rutGuardado = preferencias.obtenerRut().first()
            _state.update {
                it.copy(
                    rut = rutGuardado.orEmpty(),
                    recordarRut = !rutGuardado.isNullOrEmpty(),
                    cargandoPreferencias = false
                )
            }
        }
    }

    fun onRutChanged(rut: String) {
        _state.update {
            it.copy(rut = rut, errores = it.errores.copy(rut = Validaciones.validarRut(rut)), errorGeneral = null)
        }
    }

    fun onContrasenaChanged(contrasena: String) {
        _state.update {
            it.copy(
                contrasena = contrasena,
                errores = it.errores.copy(contrasena = Validaciones.validarContrasena(contrasena)),
                errorGeneral = null
            )
        }
    }

    fun onRecordarRutChanged(recordar: Boolean) {
        _state.update { it.copy(recordarRut = recordar) }
    }

    /** Valida todo el formulario: devuelve `true` si es válido y actualiza los errores si corresponde. */
    fun validarFormulario(): Boolean {
        val actual = _state.value
        val errores = LoginErrores(
            rut = Validaciones.validarRut(actual.rut),
            contrasena = Validaciones.validarContrasena(actual.contrasena)
        )
        _state.update { it.copy(errores = errores) }
        return errores.rut == null && errores.contrasena == null
    }

    /** Intenta ingresar con el [rol] elegido en la pantalla de inicio. */
    fun ingresar(rol: String) {
        if (_state.value.cargando || !validarFormulario()) return

        val actual = _state.value
        viewModelScope.launch {
            _state.update { it.copy(cargando = true, errorGeneral = null) }
            carga.join()

            val rut = Validaciones.normalizarRut(actual.rut)
            val usuario = repository.login(rut, Seguridad.hash(actual.contrasena), rol)

            if (usuario != null) {
                if (actual.recordarRut) preferencias.guardarRut(rut) else preferencias.borrarRut()
                _eventos.send(LoginEvent.Autenticado(usuario.rut, usuario.rol))
            } else {
                _state.update {
                    it.copy(errorGeneral = "RUT o contraseña incorrectos para el perfil ${Rol.titulo(rol)}")
                }
            }
            _state.update { it.copy(cargando = false) }
        }
    }
}
