package cl.duoc.mpazpro.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import cl.duoc.mpazpro.data.local.entity.PreguntaEntity
import cl.duoc.mpazpro.utils.Evaluador
import cl.duoc.mpazpro.model.ResultadoPregunta

/** Las tres alternativas. Con [mostrarResultado] pinta la correcta en verde y la elegida errónea en rojo. */
@Composable
fun OpcionesPregunta(
    pregunta: PreguntaEntity,
    seleccion: Int,
    bloqueado: Boolean,
    mostrarResultado: Boolean,
    onSeleccion: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        (0..2).forEach { i ->
            val esCorrectaOpcion = mostrarResultado && i == pregunta.correcta
            val esErrada = mostrarResultado && i == seleccion && i != pregunta.correcta
            val color by animateColorAsState(
                targetValue = when {
                    esCorrectaOpcion -> MaterialTheme.colorScheme.tertiaryContainer
                    esErrada -> MaterialTheme.colorScheme.errorContainer
                    seleccion == i -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                label = "colorOpcion"
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = color),
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = seleccion == i,
                        enabled = !bloqueado,
                        role = Role.RadioButton,
                        onClick = { onSeleccion(i) }
                    )
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = seleccion == i, onClick = null, enabled = !bloqueado)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "${Evaluador.letra(i)}) ${Evaluador.textoAlternativa(pregunta, i)}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    if (esCorrectaOpcion) Icon(Icons.Default.CheckCircle, contentDescription = "Correcta", tint = MaterialTheme.colorScheme.tertiary)
                    else if (esErrada) Icon(Icons.Default.Cancel, contentDescription = "Incorrecta", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

/** Mensaje inmediato de la práctica: ¡Correcto! o Incorrecto + la respuesta correcta. */
@Composable
fun TarjetaFeedback(correcta: Boolean, textoCorrecto: String) {
    val colores = CardDefaults.cardColors(
        containerColor = if (correcta) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.errorContainer
    )
    Card(colors = colores, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (correcta) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (correcta) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(if (correcta) "¡Muy bien, es correcto!" else "Respuesta incorrecta", style = MaterialTheme.typography.titleMedium)
                if (!correcta) Text("La respuesta correcta es: $textoCorrecto", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/** Revisión final: qué preguntas salieron bien y cuáles no (el estudiante ve solo sus respuestas). */
@Composable
fun ResumenResultados(resultados: List<ResultadoPregunta>) {
    val buenas = resultados.count { it.correcta }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Obtuviste $buenas de ${resultados.size} correctas", style = MaterialTheme.typography.titleLarge)
        resultados.forEachIndexed { i, r ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = if (r.correcta) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = if (r.correcta) "Correcta" else "Incorrecta",
                        tint = if (r.correcta) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("${i + 1}. ${r.pregunta.enunciado}", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Tu respuesta: ${Evaluador.letra(r.elegida)}) ${Evaluador.textoAlternativa(r.pregunta, r.elegida)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (!r.correcta) {
                            Text(
                                "Correcta: ${Evaluador.letra(r.pregunta.correcta)}) ${Evaluador.textoAlternativa(r.pregunta, r.pregunta.correcta)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }
        }
    }
}
