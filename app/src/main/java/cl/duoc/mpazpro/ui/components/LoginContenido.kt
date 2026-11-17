package cl.duoc.mpazpro.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cl.duoc.mpazpro.model.LoginUiState

/**
 * Formulario de acceso compartido por los tres perfiles. Solo dibuja el estado que recibe:
 * no valida ni consulta la base de datos (eso lo hace el LoginViewModel).
 *
 * El contenido se limita a 480 dp de ancho para que no se estire en pantallas grandes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginContenido(
    titulo: String,
    iconoIdentificador: ImageVector,
    state: LoginUiState,
    onRutChange: (String) -> Unit,
    onContrasenaChange: (String) -> Unit,
    onRecordarChange: (Boolean) -> Unit,
    onIngresar: () -> Unit,
    onVolver: () -> Unit
) {
    // Estado derivado: el botón solo se habilita cuando hay datos y no hay una consulta en curso
    val puedeIngresar by remember(state.rut, state.contrasena, state.cargando) {
        derivedStateOf { state.rut.isNotBlank() && state.contrasena.isNotBlank() && !state.cargando }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            // Simulación de carga inicial: se lee el RUT recordado desde DataStore
            if (state.cargandoPreferencias) {
                CircularProgressIndicator()
                return@Box
            }

            Column(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CampoValidado(
                    valor = state.rut,
                    onCambio = onRutChange,
                    etiqueta = "RUT",
                    icono = iconoIdentificador,
                    error = state.errores.rut,
                    ayuda = "Ejemplo: 11.111.111-1",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                CampoContrasena(
                    valor = state.contrasena,
                    onCambio = onContrasenaChange,
                    error = state.errores.contrasena,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = state.recordarRut, onCheckedChange = onRecordarChange)
                    Text("Recordar mi RUT en este dispositivo")
                }

                // Mensaje de error general (credenciales incorrectas) con animación de entrada y salida
                AnimatedVisibility(
                    visible = state.errorGeneral != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = state.errorGeneral.orEmpty(),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onIngresar,
                    enabled = puedeIngresar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.cargando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Ingresar", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}
