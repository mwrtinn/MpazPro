package cl.duoc.mpazpro.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
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
import androidx.lifecycle.viewmodel.compose.viewModel as composeViewModel
import cl.duoc.mpazpro.utils.GeneradorInforme
import cl.duoc.mpazpro.ui.components.BannerConexion
import cl.duoc.mpazpro.ui.components.TarjetaObjetivo
import cl.duoc.mpazpro.ui.components.TarjetaNaturaleza
import cl.duoc.mpazpro.viewmodel.EstudianteViewModel
import cl.duoc.mpazpro.viewmodel.NaturalezaViewModel

// Pantalla principal del estudiante.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstudianteHomeScreen(
    rut: String,
    viewModel: EstudianteViewModel,
    onAbrirObjetivo: (Int) -> Unit,
    onLogout: () -> Unit
) {

    val naturalezaViewModel: NaturalezaViewModel =
        composeViewModel(
            key = "naturaleza_$rut"
        )

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

            // Actualiza los objetivos del servidor.
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

                if (
                    objetivos.isEmpty() &&
                    !cargando &&
                    errorRemoto == null
                ) {

                    item {

                        Text(
                            "Tu docente aún no asigna objetivos.",
                            style = MaterialTheme
                                .typography.bodyLarge
                        )
                    }
                }

                // Conserva los objetivos y avances académicos.
                items(
                    objetivos,
                    key = { it.id }
                ) { oa ->

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

                // Actividad recreativa mediante una API externa.
                item {

                    Text(
                        "Aprende jugando",
                        style = MaterialTheme
                            .typography.titleLarge,
                        modifier = Modifier.padding(
                            bottom = 8.dp,
                            top = 8.dp
                        )
                    )

                    TarjetaNaturaleza(
                        viewModel = naturalezaViewModel
                    )
                }
            }
        }
    }
}