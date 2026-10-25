package cl.duoc.mpazpro.ui.screen

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.duoc.mpazpro.viewmodel.DocenteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocenteScreen(
    rutDocente: String,
    viewModel: DocenteViewModel,
    onLogout: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var fotoEvidencia by remember { mutableStateOf<Bitmap?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { fotoEvidencia = it }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel Docente - Marcela Paz") },
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Gestión de Desafíos e Informes Pedagógicos", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = state.rutAlumno,
                onValueChange = viewModel::onRutChanged,
                label = { Text("RUT Alumno") },
                isError = state.rutAlumnoError != null,
                supportingText = { state.rutAlumnoError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.titulo,
                onValueChange = viewModel::onTituloChanged,
                label = { Text("Título del Desafío") },
                isError = state.tituloError != null,
                supportingText = { state.tituloError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.descripcion,
                onValueChange = viewModel::onDescripcionChanged,
                label = { Text("Descripción / Instrucciones") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.limiteIntentos,
                onValueChange = viewModel::onLimiteIntentosChanged,
                label = { Text("Límite de Intentos Permitidos") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.informePedagogico,
                onValueChange = viewModel::onInformeChanged,
                label = { Text("Informe Pedagógico / Retroalimentación") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(onClick = { cameraLauncher.launch() }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Capturar Material / Evidencia")
            }

            fotoEvidencia?.let {
                Image(bitmap = it.asImageBitmap(), contentDescription = null, modifier = Modifier.height(150.dp))
            }

            Button(onClick = { viewModel.crearDesafio() }, modifier = Modifier.fillMaxWidth()) {
                Text("Guardar Desafío e Informe")
            }

            AnimatedVisibility(visible = state.isSuccess) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Text("Desafío e informe guardados con éxito.", modifier = Modifier.padding(12.dp))
                }
            }
        }
    }
}