package cl.duoc.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mpazpro.data.local.PreferenciasDataStore
import cl.duoc.mpazpro.data.remote.SesionRemota
import cl.duoc.mpazpro.model.LoginErrores
import cl.duoc.mpazpro.model.LoginEvent
import cl.duoc.mpazpro.model.LoginUiState
import cl.duoc.mpazpro.model.Rol
import cl.duoc.mpazpro.repository.AulaRepository
import cl.duoc.mpazpro.utils.Validaciones
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// Gestiona el formulario de acceso y la autenticación remota.
class LoginViewModel(
    private val repository: AulaRepository,
    private val preferencias: PreferenciasDataStore
) : ViewModel() {

    // Estado del formulario.
    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    // Eventos de navegación después del acceso.
    private val _eventos = Channel<LoginEvent>(Channel.BUFFERED)
    val eventos: Flow<LoginEvent> = _eventos.receiveAsFlow()

    // Conserva temporalmente los datos locales de la aplicación.
    private val carga = viewModelScope.launch {
        repository.sembrarSiVacio()
    }

    // Reinicia el formulario y recupera el RUT recordado.
    fun limpiar() {

        SesionRemota.cerrar()

        viewModelScope.launch {

            _state.value = LoginUiState(
                cargandoPreferencias = true
            )

            try {

                val rutGuardado = preferencias.obtenerRut().first()

                _state.update {
                    it.copy(
                        rut = rutGuardado.orEmpty(),
                        recordarRut = !rutGuardado.isNullOrEmpty()
                    )
                }

            } catch (e: IOException) {

                _state.update {
                    it.copy(rut = "", recordarRut = false)
                }

            } finally {

                _state.update {
                    it.copy(cargandoPreferencias = false)
                }
            }
        }
    }

    fun onRutChanged(rut: String) {

        _state.update {
            it.copy(
                rut = rut,
                errores = it.errores.copy(
                    rut = Validaciones.validarRut(rut)
                ),
                errorGeneral = null
            )
        }
    }

    fun onContrasenaChanged(contrasena: String) {

        _state.update {
            it.copy(
                contrasena = contrasena,
                errores = it.errores.copy(
                    contrasena = Validaciones.validarContrasena(
                        contrasena
                    )
                ),
                errorGeneral = null
            )
        }
    }

    fun onRecordarRutChanged(recordar: Boolean) {

        _state.update {
            it.copy(recordarRut = recordar)
        }
    }

    // Valida los campos antes de enviar la solicitud.
    fun validarFormulario(): Boolean {

        val actual = _state.value

        val errores = LoginErrores(
            rut = Validaciones.validarRut(actual.rut),
            contrasena = Validaciones.validarContrasena(
                actual.contrasena
            )
        )

        _state.update {
            it.copy(errores = errores)
        }

        return errores.rut == null &&
                errores.contrasena == null
    }

    // Inicia sesión utilizando el microservicio de usuarios.
    fun ingresar(rol: String) {

        if (_state.value.cargando || !validarFormulario()) return

        val actual = _state.value

        _state.update {
            it.copy(
                cargando = true,
                errorGeneral = null
            )
        }

        viewModelScope.launch {

            SesionRemota.cerrar()

            try {

                carga.join()

                val rut = Validaciones.normalizarRut(actual.rut)

                val usuario = repository.login(
                    rut = rut,
                    password = actual.contrasena,
                    rol = rol
                )

                // Verifica que la sesión recibida sea válida.
                if (usuario.token.isBlank() ||
                    usuario.rol != rol
                ) {
                    _state.update {
                        it.copy(
                            errorGeneral = "Respuesta de autenticación inválida"
                        )
                    }

                    return@launch
                }

                // Conserva únicamente el RUT si el usuario lo solicita.
                try {
                    if (actual.recordarRut) {
                        preferencias.guardarRut(rut)
                    } else {
                        preferencias.borrarRut()
                    }
                } catch (e: IOException) {
                    // Un fallo de preferencias no impide iniciar sesión.
                }

                SesionRemota.iniciar(usuario)

                // Limpia la contraseña del formulario.
                _state.update {
                    it.copy(contrasena = "")
                }

                _eventos.send(
                    LoginEvent.Autenticado(
                        usuario.rut,
                        usuario.rol
                    )
                )

            } catch (e: CancellationException) {

                throw e

            } catch (e: HttpException) {

                val mensaje = when (e.code()) {

                    400 -> "Datos de acceso inválidos"

                    401, 403 ->
                        "RUT o contraseña incorrectos para el perfil ${Rol.titulo(rol)}"

                    else -> "Error del servidor (${e.code()})"
                }

                _state.update {
                    it.copy(errorGeneral = mensaje)
                }

            } catch (e: IOException) {

                _state.update {
                    it.copy(
                        errorGeneral = "No se pudo conectar con el servidor. Verifica que usuarios-service esté ejecutándose."
                    )
                }

            } catch (e: Exception) {

                _state.update {
                    it.copy(
                        errorGeneral = "No fue posible iniciar sesión"
                    )
                }

            } finally {

                _state.update {
                    it.copy(cargando = false)
                }
            }
        }
    }
}