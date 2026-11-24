package cl.duoc.mpazpro.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.mpazpro.model.EstadoContenido
import cl.duoc.mpazpro.ui.components.BannerConexion
import cl.duoc.mpazpro.ui.components.DialogoRechazo
import cl.duoc.mpazpro.ui.components.TarjetaRevision
import cl.duoc.mpazpro.viewmodel.UtpViewModel

// Panel UTP para revisar los contenidos del servidor.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UtpScreen(
    viewModel: UtpViewModel,
    onLogout: () -> Unit
) {

    val cargando by viewModel.cargando.collectAsState()
    val errorRemoto by viewModel.errorRemoto.collectAsState()
    val contenidos by viewModel.contenidos.collectAsState()
    val revision by viewModel.revision.collectAsState()

    val pendientes = contenidos.count {

        it.contenido.estado == EstadoContenido.PENDIENTE
    }

    // Los contenidos pendientes aparecen primero.
    val ordenados = contenidos.sortedBy {

        if (it.contenido.estado == EstadoContenido.PENDIENTE)
            0
        else
            1
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("Revisión de contenidos (UTP)")
                },

                actions = {

                    IconButton(onClick = onLogout) {

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

            // Actualiza los contenidos registrados por el docente.
            TextButton(
                onClick = viewModel::recargar,
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
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 4.dp
                    )
                )
            }

            LazyColumn(

                modifier = Modifier.fillMaxSize(),

                contentPadding = PaddingValues(16.dp),

                verticalArrangement =
                    Arrangement.spacedBy(12.dp)

            ) {

                item {

                    Text(
                        "Contenidos pendientes de revisión: $pendientes",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        "Verifica que sean pertinentes para 3.º Básico y estén vinculados a su objetivo de aprendizaje.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                items(
                    ordenados,
                    key = { it.contenido.id }
                ) { rev ->

                    TarjetaRevision(

                        revision = rev,

                        onAprobar = {
                            viewModel.aprobar(rev.contenido)
                        },

                        onRechazar = {
                            viewModel.abrirRechazo(rev.contenido)
                        }
                    )
                }
            }
        }
    }

    // Solicita una observación al rechazar un contenido.
    DialogoRechazo(
        revision = revision,
        onObservacionChange =
            viewModel::onObservacionChanged,
        onConfirmar =
            viewModel::confirmarRechazo,
        onCerrar =
            viewModel::cerrarRechazo
    )
}