package cl.duoc.mpazpro.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.duoc.mpazpro.data.local.entity.ObjetivoEntity
import cl.duoc.mpazpro.model.ConfigIntentosState

/** El docente elige qué objetivos quedan disponibles y cuántos intentos tiene la prueba final. */
@Composable
fun DocenteObjetivosTab(
    objetivos: List<ObjetivoEntity>,
    config: ConfigIntentosState,
    onAsignar: (ObjetivoEntity, Boolean) -> Unit,
    onAbrirConfig: (ObjetivoEntity) -> Unit,
    onIntentosChange: (String) -> Unit,
    onGuardarConfig: () -> Unit,
    onCerrarConfig: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Selecciona los objetivos que verán tus estudiantes (máximo una unidad por asignatura).", style = MaterialTheme.typography.bodyMedium)
        }
        items(objetivos, key = { it.id }) { oa ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(oa.asignatura, style = MaterialTheme.typography.titleMedium)
                    Text("${oa.codigo} · ${oa.unidad}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    Text(oa.descripcion, style = MaterialTheme.typography.bodyMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(checked = oa.asignado, onCheckedChange = { onAsignar(oa, it) })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (oa.asignado) "Disponible para estudiantes" else "No asignado")
                    }
                    OutlinedButton(onClick = { onAbrirConfig(oa) }) {
                        Icon(Icons.Default.Repeat, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Intentos de la prueba final: ${oa.intentosPrueba}")
                    }
                }
            }
        }
    }

    if (config.objetivo != null) {
        DialogoCampo(
            titulo = "Intentos de la prueba final",
            etiqueta = "Máximo de intentos (1 a 3)",
            icono = Icons.Default.Repeat,
            valor = config.valor,
            error = config.error,
            teclado = KeyboardType.Number,
            textoConfirmar = "Guardar",
            onCambio = onIntentosChange,
            onConfirmar = onGuardarConfig,
            onCerrar = onCerrarConfig
        )
    }
}
