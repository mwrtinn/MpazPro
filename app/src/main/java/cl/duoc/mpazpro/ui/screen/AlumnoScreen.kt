package cl.duoc.mpazpro.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.duoc.mpazpro.viewmodel.AlumnoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumnoScreen(
    rutAlumno: String,
    viewModel: AlumnoViewModel,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val desafios by viewModel.desafios.collectAsState()

    LaunchedEffect(rutAlumno) {
        viewModel.cargarDesafiosAlumno(rutAlumno)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Portal Alumno - Marcela Paz") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar Sesión")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Desafíos Interactivos", style = MaterialTheme.typography.titleMedium)
                    Text("Estudiante RUT: $rutAlumno", style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (desafios.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No tienes desafíos interactivos asignados.")
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(desafios, key = { it.id }) { desafio ->
                        Card(
                            modifier = Modifier.fillMaxWidth().animateContentSize(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(desafio.titulo, style = MaterialTheme.typography.titleSmall)
                                Text(desafio.descripcion, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Intentos: ${desafio.intentosRealizados} / ${desafio.limiteIntentos}")
                                Text("Puntaje: ${desafio.puntajeObtenido} / ${desafio.puntajeMaximo}")
                                if (desafio.informePedagogico.isNotBlank()) {
                                    Text("Informe Docente: ${desafio.informePedagogico}", style = MaterialTheme.typography.bodySmall)
                                }
                                Button(
                                    onClick = { viewModel.realizarIntento(desafio, (50..100).random()) },
                                    enabled = desafio.intentosRealizados < desafio.limiteIntentos,
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text("Resolver Desafío")
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:contacto@marcelapaz.cl")
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Icon(Icons.Default.Email, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Contactar Soporte")
            }
        }
    }
}