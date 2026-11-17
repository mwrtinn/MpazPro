package cl.duoc.mpazpro.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import cl.duoc.mpazpro.utils.Evaluador
import cl.duoc.mpazpro.model.TipoPregunta
import cl.duoc.mpazpro.recursos.rememberVoz
import cl.duoc.mpazpro.ui.components.BannerConexion
import cl.duoc.mpazpro.ui.components.OpcionesPregunta
import cl.duoc.mpazpro.ui.components.ResumenResultados
import cl.duoc.mpazpro.ui.components.TarjetaFeedback
import cl.duoc.mpazpro.viewmodel.EstudianteViewModel
import kotlinx.coroutines.launch

/**
 * Desafíos (práctica con retroalimentación inmediata) o prueba final (revisión al terminar,
 * dentro de los intentos que definió el docente).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    oaId: Int,
    tipo: String,
    viewModel: EstudianteViewModel,
    onVolver: () -> Unit
) {
    val quiz by viewModel.quiz.collectAsState()
    LaunchedEffect(oaId, tipo) { viewModel.iniciarQuiz(oaId, tipo) }

    val voz = rememberVoz()
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(quiz.respondida) {
        if (quiz.respondida) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (tipo == TipoPregunta.PRUEBA) "Prueba final" else "Desafíos") },
                navigationIcon = {
                    IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            BannerConexion()
            when {
                quiz.cargando -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                quiz.bloqueadoMensaje != null -> Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(quiz.bloqueadoMensaje.orEmpty(), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(16.dp))
                    }
                    Button(onClick = onVolver, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) { Text("Volver") }
                }

                quiz.terminado -> Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ResumenResultados(quiz.resultados)
                    Button(onClick = onVolver, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Volver al objetivo") }
                }

                else -> {
                    val pregunta = quiz.preguntaActual
                    if (pregunta != null) {
                        val progreso by animateFloatAsState(
                            targetValue = (quiz.indice + if (quiz.respondida) 1 else 0) / quiz.preguntas.size.toFloat(),
                            label = "progresoQuiz"
                        )
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val encabezado = "Pregunta ${quiz.indice + 1} de ${quiz.preguntas.size} · Nivel ${pregunta.dificultad}" +
                                if (tipo == TipoPregunta.PRUEBA) " · Intento ${quiz.intento} de ${quiz.intentosPermitidos}" else ""
                            Text(encabezado, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            LinearProgressIndicator(progress = { progreso }, modifier = Modifier.fillMaxWidth())

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(pregunta.enunciado, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                                IconButton(onClick = {
                                    val opciones = (0..2).joinToString(". ") { "${Evaluador.letra(it)}: ${Evaluador.textoAlternativa(pregunta, it)}" }
                                    if (!voz.hablar("${pregunta.enunciado}. $opciones")) {
                                        scope.launch { snackbar.showSnackbar("La voz no está disponible en este dispositivo") }
                                    }
                                }) { Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Escuchar pregunta") }
                            }

                            OpcionesPregunta(
                                pregunta = pregunta,
                                seleccion = quiz.seleccion,
                                bloqueado = quiz.respondida,
                                mostrarResultado = quiz.respondida && tipo == TipoPregunta.DESAFIO,
                                onSeleccion = viewModel::seleccionar
                            )

                            AnimatedVisibility(
                                visible = quiz.respondida && tipo == TipoPregunta.DESAFIO,
                                enter = fadeIn() + slideInVertically { it / 2 }
                            ) {
                                val ok = Evaluador.esCorrecta(pregunta, quiz.seleccion)
                                TarjetaFeedback(
                                    correcta = ok,
                                    textoCorrecto = "${Evaluador.letra(pregunta.correcta)}) ${Evaluador.textoAlternativa(pregunta, pregunta.correcta)}"
                                )
                            }

                            if (quiz.respondida) {
                                val esUltima = quiz.indice >= quiz.preguntas.lastIndex
                                Button(onClick = viewModel::siguiente, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                                    Text(if (esUltima) "Terminar" else "Siguiente")
                                }
                            } else {
                                Button(
                                    onClick = viewModel::responder,
                                    enabled = quiz.seleccion >= 0,
                                    modifier = Modifier.fillMaxWidth().height(52.dp)
                                ) { Text(if (tipo == TipoPregunta.PRUEBA) "Responder y continuar" else "Responder") }
                            }
                        }
                    }
                }
            }
        }
    }
}
