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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.mpazpro.data.local.entity.ObjetivoEntity
import cl.duoc.mpazpro.data.local.entity.PreguntaEntity
import cl.duoc.mpazpro.model.PreguntaUiState
import cl.duoc.mpazpro.model.TipoPregunta
import cl.duoc.mpazpro.utils.Evaluador

/**
 * Pestaña "Preguntas" del docente: formulario para crear preguntas de práctica (desafío) o de la
 * prueba final, y lista de las preguntas creadas.
 */
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
    onGuardar: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item { Text("Nueva pregunta", style = MaterialTheme.typography.titleMedium) }
        item { SelectorObjetivo(objetivos, form.oaId, onOaChange, form.errores.oa, Modifier.fillMaxWidth()) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = form.tipo == TipoPregunta.DESAFIO, onClick = { onTipoChange(TipoPregunta.DESAFIO) }, label = { Text("Desafío") })
                FilterChip(selected = form.tipo == TipoPregunta.PRUEBA, onClick = { onTipoChange(TipoPregunta.PRUEBA) }, label = { Text("Prueba final") })
            }
        }
        item {
            Text("Dificultad:", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..3).forEach { nivel ->
                    FilterChip(selected = form.dificultad == nivel, onClick = { onDificultadChange(nivel) }, label = { Text("Nivel $nivel") })
                }
            }
        }
        item {
            CampoValidado(
                valor = form.enunciado, onCambio = onEnunciadoChange,
                etiqueta = "Pregunta o ejercicio", icono = Icons.Default.Quiz, error = form.errores.enunciado,
                singleLine = false, minLines = 2, modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            CampoValidado(
                valor = form.opcionA, onCambio = onOpcionAChange, etiqueta = "Alternativa A",
                icono = Icons.Default.Edit, error = form.errores.opcionA, modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            CampoValidado(
                valor = form.opcionB, onCambio = onOpcionBChange, etiqueta = "Alternativa B",
                icono = Icons.Default.Edit, error = form.errores.opcionB, modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            CampoValidado(
                valor = form.opcionC, onCambio = onOpcionCChange, etiqueta = "Alternativa C",
                icono = Icons.Default.Edit, error = form.errores.opcionC, modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Text("Respuesta correcta:", style = MaterialTheme.typography.bodyLarge)
            Row(verticalAlignment = Alignment.CenterVertically) {
                (0..2).forEach { i ->
                    RadioButton(selected = form.correcta == i, onClick = { onCorrectaChange(i) })
                    Text(Evaluador.letra(i).toString(), modifier = Modifier.padding(end = 16.dp))
                }
            }
        }
        item {
            Button(onClick = onGuardar, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar pregunta")
            }
        }
        item { MensajeExito(form.guardado, "Pregunta guardada correctamente.") }

        item {
            Text(
                "Preguntas creadas (${preguntas.size})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        items(preguntas, key = { it.id }) { p ->
            val oa = objetivos.firstOrNull { it.id == p.oaId }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(p.enunciado, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "${if (p.tipo == TipoPregunta.PRUEBA) "Prueba final" else "Desafío"} · Nivel ${p.dificultad} · ${oa?.codigo.orEmpty()}",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary
                    )
                    Text("Correcta: ${Evaluador.letra(p.correcta)}) ${Evaluador.textoAlternativa(p, p.correcta)}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
