package cl.duoc.mpazpro.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.mpazpro.data.local.entity.ObjetivoEntity
import cl.duoc.mpazpro.model.EstadoContenido
import cl.duoc.mpazpro.recursos.rememberConectado

/** Aviso animado cuando el dispositivo pierde la conexión a internet (recurso nativo: red). */
@Composable
fun BannerConexion() {
    val conectado by rememberConectado()
    AnimatedVisibility(
        visible = !conectado,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Surface(color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WifiOff, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sin conexión a internet", color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }
}

/** Etiqueta de color con el estado de aprobación de un contenido. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChipEstado(estado: String) {
    val texto = when (estado) {
        EstadoContenido.APROBADO -> "Aprobado por UTP"
        EstadoContenido.RECHAZADO -> "Rechazado por UTP"
        else -> "Pendiente de UTP"
    }
    AssistChip(onClick = {}, label = { Text(texto) }, enabled = false)
}

/** Lista desplegable de objetivos de aprendizaje, con mensaje de error si no se elige ninguno. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorObjetivo(
    objetivos: List<ObjetivoEntity>,
    seleccionadoId: Int?,
    onSeleccion: (Int) -> Unit,
    error: String?,
    modifier: Modifier = Modifier
) {
    var expandido by remember { mutableStateOf(false) }
    val actual = objetivos.firstOrNull { it.id == seleccionadoId }

    ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = it }, modifier = modifier) {
        OutlinedTextField(
            value = actual?.let { "${it.codigo} · ${it.asignatura}" }.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Objetivo de aprendizaje") },
            leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
            isError = error != null,
            supportingText = if (error != null) {
                { Text(error) }
            } else null,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            objetivos.forEach { oa ->
                DropdownMenuItem(
                    text = { Text("${oa.codigo} · ${oa.asignatura}") },
                    onClick = {
                        onSeleccion(oa.id)
                        expandido = false
                    }
                )
            }
        }
    }
}

/** Mensaje de confirmación animado (guardado correcto). */
@Composable
fun MensajeExito(visible: Boolean, texto: String) {
    AnimatedVisibility(visible = visible, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
        Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = MaterialTheme.shapes.medium) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(texto)
            }
        }
    }
}

/** Diálogo con un único campo numérico/texto validado; lo usan los intentos y el rechazo de UTP. */
@Composable
fun DialogoCampo(
    titulo: String,
    etiqueta: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    valor: String,
    error: String?,
    teclado: androidx.compose.ui.text.input.KeyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
    textoConfirmar: String,
    onCambio: (String) -> Unit,
    onConfirmar: () -> Unit,
    onCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(titulo) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CampoValidado(
                    valor = valor, onCambio = onCambio, etiqueta = etiqueta, icono = icono,
                    error = error, teclado = teclado, singleLine = teclado != androidx.compose.ui.text.input.KeyboardType.Text,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = { TextButton(onClick = onConfirmar) { Text(textoConfirmar) } },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar") } }
    )
}
