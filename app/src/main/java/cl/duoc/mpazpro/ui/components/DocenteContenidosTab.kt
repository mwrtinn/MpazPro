package cl.duoc.mpazpro.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.mpazpro.data.local.entity.ContenidoEntity
import cl.duoc.mpazpro.data.local.entity.ObjetivoEntity
import cl.duoc.mpazpro.model.ContenidoUiState
import cl.duoc.mpazpro.model.EstadoContenido
import cl.duoc.mpazpro.model.ICONOS_CONTENIDO

/**
 * Pestaña "Contenidos" del docente: formulario para crear un contenido didáctico (con imagen de apoyo
 * opcional) y lista de los contenidos creados con su estado de revisión de UTP.
 * Solo dibuja el estado y avisa los eventos; la validación vive en el DocenteViewModel.
 */
@Composable
fun DocenteContenidosTab(
    form: ContenidoUiState,
    objetivos: List<ObjetivoEntity>,
    contenidos: List<ContenidoEntity>,
    onOaChange: (Int) -> Unit,
    onTituloChange: (String) -> Unit,
    onExplicacionChange: (String) -> Unit,
    onEjemploChange: (String) -> Unit,
    onIconoChange: (String) -> Unit,
    onImagenChange: (String?) -> Unit,
    onGuardar: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item { Text("Nuevo contenido didáctico", style = MaterialTheme.typography.titleMedium) }
        item { SelectorObjetivo(objetivos, form.oaId, onOaChange, form.errores.oa, Modifier.fillMaxWidth()) }
        item {
            CampoValidado(
                valor = form.titulo, onCambio = onTituloChange,
                etiqueta = "Título", icono = Icons.Default.Title, error = form.errores.titulo,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            CampoValidado(
                valor = form.explicacion, onCambio = onExplicacionChange,
                etiqueta = "Explicación (lenguaje simple)", icono = Icons.Default.Description,
                error = form.errores.explicacion, singleLine = false, minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            CampoValidado(
                valor = form.ejemplo, onCambio = onEjemploChange,
                etiqueta = "Ejemplo", icono = Icons.Default.EditNote, error = form.errores.ejemplo,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Text("Ícono de apoyo visual:", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                ICONOS_CONTENIDO.take(6).forEach { icono ->
                    Surface(
                        shape = CircleShape,
                        color = if (icono == form.icono) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { onIconoChange(icono) }
                    ) { Text(icono, fontSize = 24.sp, modifier = Modifier.padding(8.dp)) }
                }
            }
        }
        // Recursos nativos: cámara y galería
        item { SelectorImagen(imagenPath = form.imagenPath, onImagenChange = onImagenChange, modifier = Modifier.fillMaxWidth()) }
        item {
            Button(onClick = onGuardar, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enviar a revisión de UTP")
            }
        }
        item { MensajeExito(form.guardado, "Contenido enviado a UTP para su revisión.") }

        item {
            Text(
                "Contenidos creados (${contenidos.size})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        items(contenidos, key = { it.id }) { c ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(c.icono, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(c.titulo, style = MaterialTheme.typography.titleMedium)
                    }
                    c.imagenPath?.let { ImagenInteligente(ruta = it, modifier = Modifier.fillMaxWidth().height(120.dp), circular = false) }
                    ChipEstado(c.estado)
                    if (c.estado == EstadoContenido.RECHAZADO && c.observacionUtp.isNotBlank()) {
                        Text(
                            "Observación de UTP: ${c.observacionUtp}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
