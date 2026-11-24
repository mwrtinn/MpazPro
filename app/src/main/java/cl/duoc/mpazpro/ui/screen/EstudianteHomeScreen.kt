package cl.duoc.mpazpro.ui.screen

import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.*
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

// Pantalla principal de los aprendizajes asignados al estudiante.
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
    val cargando by viewModel.cargando.collectAsState()
    val errorRemoto by viewModel.errorRemoto.collectAsState()

    LaunchedEffect(rut) {
        viewModel.iniciarSesion(rut)
    }

    BackHandler(enabled = cargando) {}

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("Mis aprendizajes")
                },

                actions = {

                    IconButton(
                        onClick = onLogout,
                        enabled = !cargando
                    ) {

                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                }
            )
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            BannerConexion()

            // Permite recuperar los cambios del docente.
            TextButton(
                onClick = {
                    viewModel.iniciarSesion(rut)
                },
                enabled = !cargando
            ) {

                Text("Actualizar datos")
            }

            if (cargando) {

                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            errorRemoto?.let { mensaje ->

                Text(
                    mensaje,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }

            if (objetivos.isEmpty()) {

                if (!cargando && errorRemoto == null) {

                    Text(
                        "Tu docente aún no asigna objetivos.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(24.dp)
                    )
                }

            } else {

                LazyColumn(

                    modifier = Modifier.fillMaxSize(),

                    contentPadding = PaddingValues(16.dp),

                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)

                ) {

                    item {

                        Card(

                            modifier = Modifier.fillMaxWidth(),

                            colors = CardDefaults.cardColors(
                                containerColor =
                                    MaterialTheme.colorScheme
                                        .tertiaryContainer
                            )

                        ) {

                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {

                                Text(
                                    "¡Hola! Elige una asignatura",
                                    style = MaterialTheme
                                        .typography.titleMedium
                                )

                                Text(
                                    "RUT: $rut",
                                    style = MaterialTheme
                                        .typography.bodyMedium
                                )
                            }
                        }
                    }

                    items(
                        objetivos,
                        key = { it.id }
                    ) { oa ->

                        // Calcula el avance con respuestas del servidor.
                        val avance = GeneradorInforme.generar(
                            respuestas.filter {
                                it.oaId == oa.id
                            }
                        ).rendimiento

                        TarjetaObjetivo(
                            objetivo = oa,
                            avance = avance,
                            onClick = {

                                if (!cargando) {
                                    onAbrirObjetivo(oa.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}