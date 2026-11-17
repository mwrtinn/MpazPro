package cl.duoc.mpazpro.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.mpazpro.data.local.entity.ContenidoEntity
import cl.duoc.mpazpro.data.local.entity.ObjetivoEntity

private fun iconoAsignatura(asignatura: String) = when {
    asignatura.startsWith("Matem") -> "🔢"
    asignatura.startsWith("Lenguaje") -> "📖"
    asignatura.startsWith("Ciencias") -> "🌱"
    else -> "🧭"
}

/** Tarjeta de un objetivo asignado, con su avance animado. */
@Composable
fun TarjetaObjetivo(objetivo: ObjetivoEntity, avance: Int, onClick: () -> Unit) {
    val progreso by animateFloatAsState(targetValue = avance / 100f, label = "avanceObjetivo")
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(iconoAsignatura(objetivo.asignatura), fontSize = 36.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(objetivo.asignatura, style = MaterialTheme.typography.titleMedium)
                    Text(objetivo.unidad, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text("${objetivo.codigo} · ${objetivo.eje}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            Text(objetivo.descripcion, style = MaterialTheme.typography.bodyMedium)
            LinearProgressIndicator(progress = { progreso }, modifier = Modifier.fillMaxWidth())
            Text("Avance: $avance%", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** Recurso didáctico con ícono, explicación, ejemplo y botón de audio de apoyo. */
@Composable
fun TarjetaContenido(contenido: ContenidoEntity, onEscuchar: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(contenido.icono, fontSize = 36.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(contenido.titulo, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = onEscuchar) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Escuchar explicación")
                }
            }
            // Imagen de apoyo (tomada con la cámara o elegida en la galería por el docente)
            contenido.imagenPath?.let {
                ImagenInteligente(ruta = it, modifier = Modifier.fillMaxWidth().height(180.dp), circular = false)
            }
            Text(contenido.explicacion, style = MaterialTheme.typography.bodyLarge)
            Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = MaterialTheme.shapes.medium) {
                Text(
                    text = "Ejemplo: ${contenido.ejemplo}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}
