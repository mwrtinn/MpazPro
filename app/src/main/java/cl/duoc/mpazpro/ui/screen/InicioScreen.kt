package cl.duoc.mpazpro.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cl.duoc.mpazpro.R
import cl.duoc.mpazpro.navigation.Screen
import cl.duoc.mpazpro.viewmodel.MainViewModel

val AzulPrincipal = Color(0xFF1E3A8A)
val FondoSuperficie = Color(0xFFF8FAFC)
val GrisTexto = Color(0xFF475569)

@Composable
fun InicioScreen(
    mainViewModel: MainViewModel
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = FondoSuperficie
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(
                    initialOffsetY = { -50 },
                    animationSpec = tween(1000)
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_escuela),
                        contentDescription = "Logo Escuela Marcela Paz",
                        modifier = Modifier
                            .size(140.dp)
                            .padding(bottom = 24.dp)
                    )

                    Text(
                        text = "MpazPro",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = AzulPrincipal
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Escuela Marcela Paz",
                        style = MaterialTheme.typography.titleMedium,
                        color = GrisTexto
                    )
                }
            }

            Spacer(modifier = Modifier.height(56.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(1000, delayMillis = 500)) + slideInVertically(
                    initialOffsetY = { 50 },
                    animationSpec = tween(1000, delayMillis = 500)
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Seleccione su perfil para ingresar:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GrisTexto,
                        modifier = Modifier.padding(bottom = 24.dp),
                        textAlign = TextAlign.Center
                    )

                    Button(
                        onClick = { mainViewModel.navegarA(Screen.LoginEstudiante) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AzulPrincipal,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = "Estudiante",
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Ingresar como Estudiante",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { mainViewModel.navegarA(Screen.Login) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, AzulPrincipal),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AzulPrincipal
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Docente",
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Ingresar como Docente",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

