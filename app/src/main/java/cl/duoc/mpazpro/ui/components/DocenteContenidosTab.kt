package cl.duoc.mpazpro.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duoc.mpazpro.data.local.entity.ContenidoEntity
import cl.duoc.mpazpro.data.local.entity.ObjetivoEntity
import cl.duoc.mpazpro.model.ContenidoUiState
import cl.duoc.mpazpro.model.EstadoContenido
import cl.duoc.mpazpro.model.ICONOS_CONTENIDO
import kotlinx.coroutines.launch

// Formulario y listado CRUD de contenidos educativos.
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
    onGuardar: () -> Unit,
    editandoId: Int?,
    cargando: Boolean,
    onEditar: (ContenidoEntity) -> Unit,
    onEliminar: (ContenidoEntity) -> Unit,
    onCancelarEdicion: () -> Unit
) {

    val lista = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var porEliminar by remember {
        mutableStateOf<ContenidoEntity?>(null)
    }

    LazyColumn(
        state = lista,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {

        item {

            Text(
                if (editandoId == null)
                    "Nuevo contenido didáctico"
                else
                    "Editar contenido didáctico",
                style = MaterialTheme.typography.titleMedium
            )

            if (editandoId != null) {

                Text(
                    "Los cambios deberán ser revisados nuevamente por UTP.",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        item {

            SelectorObjetivo(
                objetivos,
                form.oaId,
                onOaChange,
                form.errores.oa,
                Modifier.fillMaxWidth()
            )
        }

        item {

            CampoValidado(
                valor = form.titulo,
                onCambio = onTituloChange,
                etiqueta = "Título",
                icono = Icons.Default.Title,
                error = form.errores.titulo,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {

            CampoValidado(
                valor = form.explicacion,
                onCambio = onExplicacionChange,
                etiqueta = "Explicación (lenguaje simple)",
                icono = Icons.Default.Description,
                error = form.errores.explicacion,
                singleLine = false,
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {

            CampoValidado(
                valor = form.ejemplo,
                onCambio = onEjemploChange,
                etiqueta = "Ejemplo",
                icono = Icons.Default.EditNote,
                error = form.errores.ejemplo,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {

            Text(
                "Ícono de apoyo visual:",
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {

                ICONOS_CONTENIDO.take(6).forEach { icono ->

                    Surface(
                        shape = CircleShape,
                        color = if (icono == form.icono)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant,

                        modifier = Modifier.clickable(
                            enabled = !cargando
                        ) {
                            onIconoChange(icono)
                        }
                    ) {

                        Text(
                            icono,
                            fontSize = 24.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }

        // Conserva la selección de imágenes por cámara o galería.
        item {

            SelectorImagen(
                imagenPath = form.imagenPath,
                onImagenChange = onImagenChange,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {

            Button(
                onClick = onGuardar,
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth()
            ) {

                Icon(Icons.Default.Save, contentDescription = null)

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    if (editandoId == null)
                        "Enviar a revisión de UTP"
                    else
                        "Guardar cambios"
                )
            }

            if (editandoId != null) {

                OutlinedButton(
                    onClick = onCancelarEdicion,
                    enabled = !cargando,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text("Cancelar edición")
                }
            }
        }

        item {

            MensajeExito(
                form.guardado,
                "Contenido guardado y enviado a revisión de UTP."
            )
        }

        item {

            Text(
                "Contenidos creados (${contenidos.size})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        items(contenidos, key = { it.id }) { c ->

            Card(modifier = Modifier.fillMaxWidth()) {

                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(c.icono, fontSize = 24.sp)

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            c.titulo,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Text(
                        c.explicacion,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        "Ejemplo: ${c.ejemplo}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    c.imagenPath?.let {

                        ImagenInteligente(
                            ruta = it,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            circular = false
                        )
                    }

                    ChipEstado(c.estado)

                    if (
                        c.estado == EstadoContenido.RECHAZADO &&
                        c.observacionUtp.isNotBlank()
                    ) {

                        Text(
                            "Observación de UTP: ${c.observacionUtp}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Acciones de edición y eliminación.
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        TextButton(
                            enabled = !cargando,
                            onClick = {

                                onEditar(c)

                                scope.launch {
                                    lista.animateScrollToItem(0)
                                }
                            }
                        ) {

                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text("Editar")
                        }

                        TextButton(
                            enabled = !cargando,
                            onClick = { porEliminar = c },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {

                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text("Eliminar")
                        }
                    }
                }
            }
        }
    }

    // Solicita confirmación antes de eliminar.
    porEliminar?.let { contenido ->

        AlertDialog(
            onDismissRequest = { porEliminar = null },

            title = {
                Text("Eliminar contenido")
            },

            text = {

                Text(
                    "¿Deseas eliminar \"${contenido.titulo}\"? " +
                            "Dejará de estar disponible para los estudiantes."
                )
            },

            confirmButton = {

                TextButton(
                    enabled = !cargando,
                    onClick = {

                        onEliminar(contenido)
                        porEliminar = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {

                    Text("Eliminar")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = { porEliminar = null }
                ) {

                    Text("Cancelar")
                }
            }
        )
    }
}