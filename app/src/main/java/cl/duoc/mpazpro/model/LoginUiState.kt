package cl.duoc.mpazpro.model

/**
 * Mensajes de error de validación de la pantalla de acceso.
 * Un valor `null` indica que el campo es válido o aún no se ha evaluado con error.
 *
 * @property rut Error del campo RUT (formato y dígito verificador).
 * @property contrasena Error del campo contraseña.
 */
data class LoginErrores(
    val rut: String? = null,
    val contrasena: String? = null
)

/**
 * Estado completo de la pantalla de acceso (docente, estudiante y UTP comparten el formulario).
 *
 * @property rut RUT escrito por el usuario.
 * @property contrasena Contraseña escrita por el usuario.
 * @property recordarRut Si está marcado, el RUT se guarda en DataStore para el próximo ingreso.
 * @property errores Errores de validación por campo.
 * @property errorGeneral Mensaje de credenciales incorrectas.
 * @property cargando `true` mientras se consulta la base de datos.
 * @property cargandoPreferencias `true` mientras se lee el RUT recordado desde DataStore.
 */
data class LoginUiState(
    val rut: String = "",
    val contrasena: String = "",
    val recordarRut: Boolean = false,
    val errores: LoginErrores = LoginErrores(),
    val errorGeneral: String? = null,
    val cargando: Boolean = false,
    val cargandoPreferencias: Boolean = true
)

/** Evento de una sola vez: la navegación reacciona cuando el acceso es correcto. */
sealed class LoginEvent {
    data class Autenticado(val rut: String, val rol: String) : LoginEvent()
}
