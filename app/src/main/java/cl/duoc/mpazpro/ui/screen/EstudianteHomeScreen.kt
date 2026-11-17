package cl.duoc.mpazpro.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.mpazpro.utils.GeneradorInforme
import cl.duoc.mpazpro.ui.components.BannerConexion
import cl.duoc.mpazpro.ui.components.TarjetaObjetivo
import cl.duoc.mpazpro.viewmodel.EstudianteViewModel

/** Inicio del estudiante: solo ve los objetivos que su docente asignó. No tiene acceso al informe pedagógico. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstudianteHomeScreen(
    rut: String,
    viewModel: EstudianteViewModel,
    onAbrirObjetivo: (Int) -> Unit,
    onLogout: () -> Unit
) {
    val objetivos by viewModel.objetivos.collectAsState()
    val respuestas by viewModel.respuestas.collectAsState()
    LaunchedEffect(rut) { viewModel.iniciarSesion(rut) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis aprendizajes") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            BannerConexion()
            if (objetivos.isEmpty()) {
                Text(
                    "Tu docente aún no asigna objetivos.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(24.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("¡Hola! Elige una asignatura", style = MaterialTheme.typography.titleMedium)
                                Text("RUT: $rut", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    items(objetivos, key = { it.id }) { oa ->
                        val avance = GeneradorInforme.generar(respuestas.filter { it.oaId == oa.id }).rendimiento
                        TarjetaObjetivo(objetivo = oa, avance = avance, onClick = { onAbrirObjetivo(oa.id) })
                    }
                }
            }
        }
    }
}
