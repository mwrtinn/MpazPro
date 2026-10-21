package cl.duoc.mpazpro.ui.screen

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
    var fotoBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Recurso Nativo 1: Cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        fotoBitmap = bitmap
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel Docente") },
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Bienvenido, Profesor", style = MaterialTheme.typography.titleMedium)
                    Text("RUT: $rutDocente", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Text("Registro de Asistencia / Novedades", style = MaterialTheme.typography.titleSmall)

            // Campo RUT Alumno
            OutlinedTextField(
                value = state.rutAlumno,
                onValueChange = { viewModel.onRutChanged(it) },
                label = { Text("RUT Alumno") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                isError = state.rutAlumnoError != null,
                supportingText = { state.rutAlumnoError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            // Campo Asignatura
            OutlinedTextField(
                value = state.asignatura,
                onValueChange = { viewModel.onAsignaturaChanged(it) },
                label = { Text("Asignatura") },
                leadingIcon = { Icon(Icons.Default.Book, contentDescription = null) },
                isError = state.asignaturaError != null,
                supportingText = { state.asignaturaError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            // Campo Observación
            OutlinedTextField(
                value = state.observacion,
                onValueChange = { viewModel.onObservacionChanged(it) },
                label = { Text("Detalle / Observación") },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                isError = state.observacionError != null,
                supportingText = { state.observacionError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            // Recurso Nativo 1: Botón Cámara
            Button(
                onClick = { cameraLauncher.launch() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tomar Foto de Evidencia")
            }

            fotoBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Foto capturada",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }

            // Guardar registro
            Button(
                onClick = { viewModel.registrarActividad() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar Registro")
            }

            // Animación de éxito (IE 2.2.2)
            AnimatedVisibility(
                visible = state.isSuccess,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Registro ingresado correctamente en la base de datos.")
                    }
                }
            }

            Divider()

            // Recurso Nativo 2: Teléfono de Urgencia Escuela
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:+56912345678")
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Call, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Llamar a Inspectoría General")
            }
        }
    }
}