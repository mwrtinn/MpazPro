package cl.duoc.mpazpro.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.mpazpro.navigation.Screen
import cl.duoc.mpazpro.viewmodel.MainViewModel

@Composable
fun InicioScreen(
    mainViewModel: MainViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "MpazPro",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Escuela Marcela Paz",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Seleccione su perfil para ingresar:",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = {
                mainViewModel.navegarA(Screen.Evaluacion)
            },
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(text = "Ingresar como Estudiante")
        }

        Button(
            onClick = {
                mainViewModel.navegarA(Screen.PerfilEstudiante)
            },
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text(text = "Ingresar como Docente / UTP")
        }
    }
}

