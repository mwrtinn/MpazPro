package cl.duoc.mpazpro.ui.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.runtime.Composable
import cl.duoc.mpazpro.model.LoginUiState
import cl.duoc.mpazpro.model.Rol
import cl.duoc.mpazpro.ui.components.LoginContenido

/**
 * Acceso por RUT y contraseña para los tres perfiles (estudiante, docente y UTP).
 * La validación y la autenticación viven en el LoginViewModel; esta pantalla solo muestra el estado.
 */
@Composable
fun LoginScreen(
    rol: String,
    state: LoginUiState,
    onRutChange: (String) -> Unit,
    onContrasenaChange: (String) -> Unit,
    onRecordarChange: (Boolean) -> Unit,
    onIngresar: () -> Unit,
    onVolver: () -> Unit
) {
    LoginContenido(
        titulo = "Acceso ${Rol.titulo(rol)}",
        iconoIdentificador = Icons.Default.Badge,
        state = state,
        onRutChange = onRutChange,
        onContrasenaChange = onContrasenaChange,
        onRecordarChange = onRecordarChange,
        onIngresar = onIngresar,
        onVolver = onVolver
    )
}
