package cl.duoc.mpazpro.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Campo de texto con retroalimentación visual estándar para toda la app:
 * ícono inicial, ícono de error (rojo) o de campo válido (verde) y mensaje bajo el campo.
 * No contiene reglas de validación: recibe el [error] ya calculado por el ViewModel.
 */
@Composable
fun CampoValidado(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    error: String?,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    teclado: KeyboardType = KeyboardType.Text,
    ayuda: String? = null
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        leadingIcon = { Icon(icono, contentDescription = null) },
        trailingIcon = {
            if (error != null) {
                Icon(Icons.Default.Error, contentDescription = "Campo con error", tint = MaterialTheme.colorScheme.error)
            } else if (valor.isNotBlank()) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Campo válido", tint = MaterialTheme.colorScheme.tertiary)
            }
        },
        isError = error != null,
        supportingText = if (error != null || ayuda != null) {
            { Text(error ?: ayuda.orEmpty()) }
        } else null,
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    )
}

/** Campo de contraseña con botón para mostrar u ocultar el texto. */
@Composable
fun CampoContrasena(
    valor: String,
    onCambio: (String) -> Unit,
    error: String?,
    modifier: Modifier = Modifier,
    etiqueta: String = "Contraseña",
    teclado: KeyboardType = KeyboardType.Password
) {
    var visible by rememberSaveable { mutableStateOf(false) }

    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (visible) "Ocultar contraseña" else "Mostrar contraseña"
                )
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        isError = error != null,
        supportingText = if (error != null) {
            { Text(error) }
        } else null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    )
}
