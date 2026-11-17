package cl.duoc.mpazpro.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.mpazpro.R
import cl.duoc.mpazpro.model.Rol
import cl.duoc.mpazpro.ui.theme.MpazProTheme

/**
 * Pantalla inicial: se elige el perfil y luego se ingresa siempre con RUT.
 * Incluye una animación de entrada (fade + deslizamiento) al abrir la app.
 */
@Composable
fun InicioScreen(onSeleccionarRol: (String) -> Unit) {
    val entrada = remember { MutableTransitionState(false).apply { targetState = true } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(visibleState = entrada, enter = fadeIn() + slideInVertically { it / 4 }) {
            Column(
                modifier = Modifier.widthIn(max = 480.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Encabezado(tamanoLogo = 120)
                Spacer(modifier = Modifier.height(32.dp))
                BotonesDePerfil(onSeleccionarRol)
            }
        }
    }
}

/** Logo, nombre de la app y de la escuela. */
@Composable
private fun Encabezado(tamanoLogo: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Logo MpazPro",
            modifier = Modifier.size(tamanoLogo.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("MpazPro", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Text(
            "Escuela Marcela Paz · 3.º Básico",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Los tres botones de perfil. */
@Composable
private fun BotonesDePerfil(onSeleccionarRol: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Elige tu perfil para ingresar con tu RUT:",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(onClick = { onSeleccionarRol(Rol.ESTUDIANTE) }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Icon(Icons.Default.Face, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Soy estudiante", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = { onSeleccionarRol(Rol.DOCENTE) }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Icon(Icons.Default.Person, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Soy docente", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = { onSeleccionarRol(Rol.UTP) }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Icon(Icons.Default.VerifiedUser, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Soy jefatura UTP", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Preview(name = "Inicio", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun PreviewInicio() {
    MpazProTheme { InicioScreen(onSeleccionarRol = {}) }
}
