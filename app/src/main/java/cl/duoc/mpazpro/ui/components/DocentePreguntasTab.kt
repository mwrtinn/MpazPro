package cl.duoc.mpazpro.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.mpazpro.data.local.entity.ObjetivoEntity
import cl.duoc.mpazpro.data.local.entity.PreguntaEntity
import cl.duoc.mpazpro.model.PreguntaUiState
import cl.duoc.mpazpro.model.TipoPregunta
import cl.duoc.mpazpro.utils.Evaluador
import kotlinx.coroutines.launch

// Formulario y listado CRUD de preguntas.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocentePreguntasTab(
    form: PreguntaUiState,
    objetivos: List<ObjetivoEntity>,
    preguntas: List<PreguntaEntity>,
    onOaChange: (Int) -> Unit,
    onEnunciadoChange: (String) -> Unit,
    onOpcionAChange: (String) -> Unit,
    onOpcionBChange: (String) -> Unit,
    onOpcionCChange: (String) -> Unit,
    onTipoChange: (String) -> Unit,
    onDificultadChange: (Int) -> Unit,
    onCorrectaChange: (Int) -> Unit,
    onGuardar: () -> Unit,
    editandoId: Int?,
    cargando: Boolean,
    onEditar: (PreguntaEntity) -> Unit,
    onEliminar: (PreguntaEntity) -> Unit,
    onCancelarEdicion: () -> Unit
) {

    val lista = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var porEliminar by remember {
        mutableStateOf<PreguntaEntity?>(null)
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
                    "Nueva pregunta"
                else
                    "Editar pregunta",
                style = MaterialTheme.typography.titleMedium
            )

            if (editandoId != null) {

                Text(
                    "Solo se pueden modificar preguntas sin respuestas registradas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
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

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                FilterChip(
                    selected = form.tipo == TipoPregunta.DESAFIO,
                    onClick = {
                        onTipoChange(TipoPregunta.DESAFIO)
                    },
                    enabled = !cargando,
                    label = { Text("Desafío") }
                )

                FilterChip(
                    selected = form.tipo == TipoPregunta.PRUEBA,
                    onClick = {
                        onTipoChange(TipoPregunta.PRUEBA)
                    },
                    enabled = !cargando,
                    label = { Text("Prueba final") }
                )
            }
        }

        item {

            Text(
                "Dificultad:",
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                (1..3).forEach { nivel ->

                    FilterChip(
                        selected = form.dificultad == nivel,
                        onClick = {
                            onDificultadChange(nivel)
                        },
                        enabled = !cargando,
                        label = { Text("Nivel $nivel") }
                    )
                }
            }
        }

        item {

            CampoValidado(
                valor = form.enunciado,
                onCambio = onEnunciadoChange,
                etiqueta = "Pregunta o ejercicio",
                icono = Icons.Default.Quiz,
                error = form.errores.enunciado,
                singleLine = false,
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {

            CampoValidado(
                valor = form.opcionA,
                onCambio = onOpcionAChange,
                etiqueta = "Alternativa A",
                icono = Icons.Default.Edit,
                error = form.errores.opcionA,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {

            CampoValidado(
                valor = form.opcionB,
                onCambio = onOpcionBChange,
                etiqueta = "Alternativa B",
                icono = Icons.Default.Edit,
                error = form.errores.opcionB,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {

            CampoValidado(
                valor = form.opcionC,
                onCambio = onOpcionCChange,
                etiqueta = "Alternativa C",
                icono = Icons.Default.Edit,
                error = form.errores.opcionC,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {

            Text(
                "Respuesta correcta:",
                style = MaterialTheme.typography.bodyLarge
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                (0..2).forEach { i ->

                    RadioButton(
                        selected = form.correcta == i,
                        onClick = { onCorrectaChange(i) },
                        enabled = !cargando
                    )

                    Text(
                        Evaluador.letra(i).toString(),
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            }
        }

        item {

            Button(
                onClick = onGuardar,
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth()
            ) {

                Icon(
                    Icons.Default.Save,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    if (editandoId == null)
                        "Guardar pregunta"
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
                "Pregunta guardada correctamente."
            )
        }

        item {

            Text(
                "Preguntas creadas (${preguntas.size})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        items(preguntas, key = { it.id }) { p ->

            val oa = objetivos.firstOrNull {
                it.id == p.oaId
            }

            val tipo = if (p.tipo == TipoPregunta.PRUEBA)
                "Prueba final"
            else
                "Desafío"

            Card(modifier = Modifier.fillMaxWidth()) {

                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    Text(
                        p.enunciado,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        "$tipo · Nivel ${p.dificultad} · ${oa?.codigo.orEmpty()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text("A) ${p.opcionA}")
                    Text("B) ${p.opcionB}")
                    Text("C) ${p.opcionC}")

                    Text(
                        "Correcta: ${Evaluador.letra(p.correcta)}) " +
                                Evaluador.textoAlternativa(p, p.correcta),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        TextButton(
                            enabled = !cargando,
                            onClick = {

                                onEditar(p)

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
                            onClick = { porEliminar = p },
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

    // Confirma la eliminación antes de solicitarla al servidor.
    porEliminar?.let { pregunta ->

        AlertDialog(
            onDismissRequest = { porEliminar = null },

            title = {
                Text("Eliminar pregunta")
            },

            text = {

                Text(
                    "¿Deseas eliminar \"${pregunta.enunciado}\"? " +
                            "Solo será posible si no tiene respuestas registradas."
                )
            },

            confirmButton = {

                TextButton(
                    enabled = !cargando,
                    onClick = {

                        onEliminar(pregunta)
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