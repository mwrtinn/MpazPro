package cl.duoc.mpazpro.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
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
import cl.duoc.mpazpro.model.ResultadoPregunta
import cl.duoc.mpazpro.recursos.rememberVoz
import cl.duoc.mpazpro.ui.components.BannerConexion
import cl.duoc.mpazpro.ui.components.OpcionesPregunta
import cl.duoc.mpazpro.ui.components.ResumenResultados
import cl.duoc.mpazpro.ui.components.TarjetaFeedback
import cl.duoc.mpazpro.viewmodel.EstudianteViewModel
import kotlinx.coroutines.launch

// Evaluaciones corregidas y almacenadas en el microservicio.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    oaId: Int,
    tipo: String,
    viewModel: EstudianteViewModel,
    onVolver: () -> Unit
) {

    val quiz by viewModel.quiz.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    val errorRemoto by viewModel.errorRemoto.collectAsState()

    LaunchedEffect(oaId, tipo) {

        viewModel.iniciarQuiz(oaId, tipo)
    }

    // Evita abandonar la pantalla mientras se registra una respuesta.
    BackHandler(enabled = cargando) {}

    val voz = rememberVoz()
    val scope = rememberCoroutineScope()

    val snackbar = remember {
        SnackbarHostState()
    }

    val haptic = LocalHapticFeedback.current

    LaunchedEffect(quiz.respondida) {

        if (quiz.respondida) {

            haptic.performHapticFeedback(
                HapticFeedbackType.LongPress
            )
        }
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {

                    Text(
                        if (tipo == TipoPregunta.PRUEBA)
                            "Prueba final"
                        else
                            "Desafíos"
                    )
                },

                navigationIcon = {

                    IconButton(
                        onClick = onVolver,
                        enabled = !cargando
                    ) {

                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },

        snackbarHost = {
            SnackbarHost(snackbar)
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            BannerConexion()

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

                if (!cargando && quiz.preguntaActual != null) {

                    Text(
                        "Si la respuesta ya fue registrada, vuelve al objetivo y continúa la evaluación.",
                        modifier = Modifier.padding(
                            horizontal = 16.dp
                        )
                    )
                }
            }

            when {

                quiz.cargando -> {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {

                        CircularProgressIndicator()
                    }
                }

                quiz.bloqueadoMensaje != null -> {

                    Column(

                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),

                        verticalArrangement =
                            Arrangement.Center

                    ) {

                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Text(
                                quiz.bloqueadoMensaje.orEmpty(),
                                style = MaterialTheme
                                    .typography.bodyLarge,
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        Button(
                            onClick = onVolver,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        ) {

                            Text("Volver")
                        }
                    }
                }

                quiz.terminado -> {

                    Column(

                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(
                                rememberScrollState()
                            ),

                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)

                    ) {

                        ResumenRemoto(quiz.resultados)

                        Button(

                            onClick = onVolver,

                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)

                        ) {

                            Text("Volver al objetivo")
                        }
                    }
                }

                quiz.preguntaActual == null -> {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            "No se pudo cargar la evaluación."
                        )

                        Button(

                            onClick = {
                                viewModel.iniciarQuiz(
                                    oaId,
                                    tipo
                                )
                            },

                            enabled = !cargando

                        ) {

                            Text("Reintentar")
                        }

                        TextButton(
                            onClick = onVolver,
                            enabled = !cargando
                        ) {

                            Text("Volver")
                        }
                    }
                }

                else -> {

                    val pregunta =
                        quiz.preguntaActual

                    if (pregunta != null) {

                        val progreso by animateFloatAsState(

                            targetValue = (
                                    quiz.indice +
                                            if (quiz.respondida) 1 else 0
                                    ) / quiz.preguntas.size.toFloat(),

                            label = "progresoQuiz"
                        )

                        Column(

                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .verticalScroll(
                                    rememberScrollState()
                                ),

                            verticalArrangement =
                                Arrangement.spacedBy(12.dp)

                        ) {

                            val encabezado =
                                "Pregunta ${quiz.indice + 1} de ${quiz.preguntas.size} · Nivel ${pregunta.dificultad}" +
                                        if (tipo == TipoPregunta.PRUEBA)
                                            " · Intento ${quiz.intento} de ${quiz.intentosPermitidos}"
                                        else
                                            ""

                            Text(
                                encabezado,
                                style = MaterialTheme
                                    .typography.bodyMedium,
                                color = MaterialTheme
                                    .colorScheme.primary
                            )

                            LinearProgressIndicator(
                                progress = { progreso },
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Mantiene la lectura por voz.
                            Row(
                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {

                                Text(
                                    pregunta.enunciado,
                                    style = MaterialTheme
                                        .typography.titleLarge,
                                    modifier = Modifier.weight(1f)
                                )

                                IconButton(

                                    onClick = {

                                        val opciones =
                                            (0..2).joinToString(". ") {

                                                "${Evaluador.letra(it)}: ${
                                                    Evaluador.textoAlternativa(
                                                        pregunta,
                                                        it
                                                    )
                                                }"
                                            }

                                        if (!voz.hablar(
                                                "${pregunta.enunciado}. $opciones"
                                            )
                                        ) {

                                            scope.launch {

                                                snackbar.showSnackbar(
                                                    "La voz no está disponible en este dispositivo"
                                                )
                                            }
                                        }
                                    }

                                ) {

                                    Icon(
                                        Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription =
                                            "Escuchar pregunta"
                                    )
                                }
                            }

                            OpcionesPregunta(

                                pregunta = pregunta,

                                seleccion = quiz.seleccion,

                                bloqueado =
                                    quiz.respondida || cargando,

                                mostrarResultado =
                                    quiz.respondida &&
                                            tipo == TipoPregunta.DESAFIO,

                                onSeleccion =
                                    viewModel::seleccionar
                            )

                            // Utiliza la corrección recibida del servidor.
                            AnimatedVisibility(

                                visible =
                                    quiz.respondida &&
                                            tipo == TipoPregunta.DESAFIO,

                                enter = fadeIn() +
                                        slideInVertically { it / 2 }

                            ) {

                                val resultado =
                                    quiz.resultados.lastOrNull()

                                if (resultado != null) {

                                    val textoCorrecto =
                                        if (pregunta.correcta in 0..2) {

                                            "${Evaluador.letra(pregunta.correcta)}) ${
                                                Evaluador.textoAlternativa(
                                                    pregunta,
                                                    pregunta.correcta
                                                )
                                            }"

                                        } else {
                                            "No disponible"
                                        }

                                    TarjetaFeedback(
                                        correcta =
                                            resultado.correcta,
                                        textoCorrecto =
                                            textoCorrecto
                                    )
                                }
                            }

                            if (quiz.respondida) {

                                val esUltima =
                                    quiz.indice >=
                                            quiz.preguntas.lastIndex

                                Button(

                                    onClick =
                                        viewModel::siguiente,

                                    enabled = !cargando,

                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)

                                ) {

                                    Text(
                                        if (esUltima)
                                            "Terminar"
                                        else
                                            "Siguiente"
                                    )
                                }

                            } else {

                                Button(

                                    onClick =
                                        viewModel::responder,

                                    enabled =
                                        quiz.seleccion >= 0 &&
                                                !cargando,

                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)

                                ) {

                                    Text(
                                        when {

                                            cargando ->
                                                "Guardando respuesta..."

                                            tipo == TipoPregunta.PRUEBA ->
                                                "Responder y continuar"

                                            else ->
                                                "Responder"
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Muestra los resultados sin inventar alternativas no recuperadas.
@Composable
private fun ResumenRemoto(
    resultados: List<ResultadoPregunta>
) {

    if (resultados.all {
            it.pregunta.correcta in 0..2
        }
    ) {

        ResumenResultados(resultados)

    } else {

        val buenas = resultados.count {
            it.correcta
        }

        Text(
            "Obtuviste $buenas de ${resultados.size} correctas",
            style = MaterialTheme.typography.titleLarge
        )

        resultados.forEachIndexed { i, r ->

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement =
                        Arrangement.spacedBy(6.dp)
                ) {

                    Text(
                        "${i + 1}. ${r.pregunta.enunciado}"
                    )

                    Text(
                        if (r.correcta)
                            "Respuesta correcta"
                        else
                            "Respuesta incorrecta",

                        color = if (r.correcta)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )

                    Text(
                        "Tu respuesta: ${
                            Evaluador.textoAlternativa(
                                r.pregunta,
                                r.elegida
                            )
                        }"
                    )

                    if (!r.correcta &&
                        r.pregunta.correcta in 0..2
                    ) {

                        Text(
                            "Correcta: ${
                                Evaluador.textoAlternativa(
                                    r.pregunta,
                                    r.pregunta.correcta
                                )
                            }"
                        )
                    }
                }
            }
        }
    }
}