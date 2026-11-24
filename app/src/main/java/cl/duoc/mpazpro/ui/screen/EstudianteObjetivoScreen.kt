package cl.duoc.mpazpro.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.mpazpro.utils.Evaluador
import cl.duoc.mpazpro.model.TipoPregunta
import cl.duoc.mpazpro.recursos.rememberVoz
import cl.duoc.mpazpro.ui.components.BannerConexion
import cl.duoc.mpazpro.ui.components.TarjetaContenido
import cl.duoc.mpazpro.viewmodel.EstudianteViewModel
import kotlinx.coroutines.launch

// Muestra los contenidos autorizados y las evaluaciones del objetivo.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstudianteObjetivoScreen(
    oaId: Int,
    viewModel: EstudianteViewModel,
    onIniciarQuiz: (String) -> Unit,
    onVolver: () -> Unit
) {

    val objetivo by viewModel.objetivo.collectAsState()
    val contenidos by viewModel.contenidos.collectAsState()
    val respuestas by viewModel.respuestas.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    val errorRemoto by viewModel.errorRemoto.collectAsState()
    val pruebaPendiente by
    viewModel.pruebaPendiente.collectAsState()

    LaunchedEffect(oaId) {
        viewModel.seleccionarObjetivo(oaId)
    }

    BackHandler(enabled = cargando) {}

    val voz = rememberVoz()
    val scope = rememberCoroutineScope()

    val snackbar = remember {
        SnackbarHostState()
    }

    // Conserva la lectura por voz del material educativo.
    val escuchar: (String) -> Unit = { texto ->

        if (!voz.hablar(texto)) {

            scope.launch {

                snackbar.showSnackbar(
                    "La voz no está disponible en este dispositivo"
                )
            }
        }
    }

    val oa = objetivo?.takeIf {
        it.id == oaId
    }

    val intentosUsados = respuestas.filter {

        it.oaId == oaId &&
                it.tipo == TipoPregunta.PRUEBA

    }.maxOfOrNull {
        it.intento
    } ?: 0

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text(oa?.asignatura ?: "Objetivo")
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

            TextButton(
                onClick = {
                    viewModel.seleccionarObjetivo(oaId)
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

            if (oa == null) return@Column

            LazyColumn(

                modifier = Modifier.fillMaxSize(),

                contentPadding = PaddingValues(16.dp),

                verticalArrangement =
                    Arrangement.spacedBy(12.dp)

            ) {

                item {

                    Text(
                        "${oa.codigo} · ${oa.unidad}",
                        style = MaterialTheme
                            .typography.titleMedium,
                        color = MaterialTheme
                            .colorScheme.primary
                    )

                    Text(
                        oa.descripcion,
                        style = MaterialTheme
                            .typography.bodyLarge,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                item {

                    Text(
                        "Aprende",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                if (contenidos.isEmpty() && !cargando) {

                    item {

                        Text(
                            "Aún no hay contenidos autorizados por UTP para este objetivo.",
                            style = MaterialTheme
                                .typography.bodyMedium
                        )
                    }
                }

                items(
                    contenidos,
                    key = { it.id }
                ) { c ->

                    TarjetaContenido(
                        contenido = c,

                        onEscuchar = {

                            escuchar(
                                "${c.titulo}. ${c.explicacion}. Ejemplo: ${c.ejemplo}"
                            )
                        }
                    )
                }

                item {

                    Text(
                        "Practica y demuestra",
                        style = MaterialTheme
                            .typography.titleLarge
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Button(

                        onClick = {
                            onIniciarQuiz(TipoPregunta.DESAFIO)
                        },

                        enabled = !cargando,

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)

                    ) {

                        Icon(
                            Icons.Default.FitnessCenter,
                            contentDescription = null
                        )

                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )

                        Text("Desafíos de práctica")
                    }

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    val restantes =
                        Evaluador.intentosRestantes(
                            oa.intentosPrueba,
                            intentosUsados
                        )

                    // Permite terminar un intento ya iniciado.
                    OutlinedButton(

                        onClick = {
                            onIniciarQuiz(TipoPregunta.PRUEBA)
                        },

                        enabled = !cargando &&
                                (
                                        restantes > 0 ||
                                                pruebaPendiente
                                        ),

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)

                    ) {

                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null
                        )

                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )

                        Text(
                            if (pruebaPendiente)
                                "Continuar prueba pendiente"
                            else
                                "Prueba final ($restantes de ${oa.intentosPrueba} intentos disponibles)"
                        )
                    }
                }
            }
        }
    }
}