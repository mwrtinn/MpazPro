package cl.duoc.mpazpro.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.mpazpro.model.EstadoContenido
import cl.duoc.mpazpro.model.ContenidoRevision
import cl.duoc.mpazpro.model.RevisionState

/** Contenido a revisar: UTP verifica su relación con el objetivo y lo aprueba o rechaza. */
@Composable
fun TarjetaRevision(revision: ContenidoRevision, onAprobar: () -> Unit, onRechazar: () -> Unit) {
    val c = revision.contenido
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(c.icono, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(c.titulo, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            }
            revision.objetivo?.let {
                Text("${it.codigo} · ${it.asignatura} · ${it.unidad}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
            c.imagenPath?.let {
                ImagenInteligente(ruta = it, modifier = Modifier.fillMaxWidth().height(140.dp), circular = false)
            }
            Text(c.explicacion, style = MaterialTheme.typography.bodyMedium)
            Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = MaterialTheme.shapes.small) {
                Text("Ejemplo: ${c.ejemplo}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(8.dp))
            }
            ChipEstado(c.estado)
            if (c.estado == EstadoContenido.RECHAZADO && c.observacionUtp.isNotBlank()) {
                Text("Observación: ${c.observacionUtp}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAprobar, enabled = c.estado != EstadoContenido.APROBADO) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Aprobar")
                }
                OutlinedButton(onClick = onRechazar, enabled = c.estado != EstadoContenido.RECHAZADO) {
                    Icon(Icons.Default.Cancel, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rechazar")
                }
            }
        }
    }
}

/** Rechazar exige una observación. */
@Composable
fun DialogoRechazo(
    revision: RevisionState,
    onObservacionChange: (String) -> Unit,
    onConfirmar: () -> Unit,
    onCerrar: () -> Unit
) {
    if (revision.contenido == null) return
    DialogoCampo(
        titulo = "Rechazar contenido",
        etiqueta = "Observación para el docente",
        icono = Icons.Default.EditNote,
        valor = revision.observacion,
        error = revision.error,
        textoConfirmar = "Rechazar",
        onCambio = onObservacionChange,
        onConfirmar = onConfirmar,
        onCerrar = onCerrar
    )
}
