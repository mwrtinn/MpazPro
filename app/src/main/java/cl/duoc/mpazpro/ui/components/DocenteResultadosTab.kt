package cl.duoc.mpazpro.ui.components

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.mpazpro.data.local.entity.InformeEntity
import cl.duoc.mpazpro.utils.GeneradorInforme
import cl.duoc.mpazpro.model.FilaResultado

/** Seguimiento por estudiante y objetivo + informe pedagógico (solo visible para el docente). */
@Composable
fun DocenteResultadosTab(
    filas: List<FilaResultado>,
    onGenerar: (FilaResultado) -> Unit,
    onValidar: (InformeEntity) -> Unit,
    onCompartir: (FilaResultado) -> Unit
) {
    if (filas.isEmpty()) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text("Asigna al menos un objetivo para ver el avance de tus estudiantes.", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(filas, key = { "${it.estudiante.rut}-${it.objetivo.id}" }) { fila ->
            val avance = GeneradorInforme.generar(fila.respuestas).rendimiento
            val progreso by animateFloatAsState(targetValue = avance / 100f, label = "avanceDocente")
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(fila.estudiante.nombre, style = MaterialTheme.typography.titleMedium)
                    Text("${fila.objetivo.codigo} · ${fila.objetivo.asignatura}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    Text("Respuestas registradas: ${fila.respuestas.size} · Rendimiento: $avance%")
                    LinearProgressIndicator(progress = { progreso }, modifier = Modifier.fillMaxWidth())

                    val informe = fila.informe
                    if (informe != null) {
                        Text("Fortalezas: ${informe.fortalezas}", style = MaterialTheme.typography.bodyMedium)
                        Text("A reforzar: ${informe.refuerzo}", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Informe del ${informe.fecha} · " + if (informe.validado) "VALIDADO" else "pendiente de tu validación",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (informe.validado) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                        OutlinedButton(onClick = { onGenerar(fila) }, enabled = fila.respuestas.isNotEmpty()) {
                            Icon(Icons.Default.Autorenew, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (informe == null) "Generar" else "Actualizar")
                        }
                        if (informe != null && !informe.validado) {
                            Button(onClick = { onValidar(informe) }) {
                                Icon(Icons.Default.Verified, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Validar")
                            }
                        }
                        if (informe != null) {
                            OutlinedButton(onClick = { onCompartir(fila) }) {
                                Icon(Icons.Default.Share, contentDescription = "Compartir informe")
                            }
                        }
                    }
                }
            }
        }
    }
}
