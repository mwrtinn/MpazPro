package cl.duoc.mpazpro.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.duoc.mpazpro.utils.textoInforme
import cl.duoc.mpazpro.recursos.compartirTexto
import cl.duoc.mpazpro.ui.components.BannerConexion
import cl.duoc.mpazpro.ui.components.DocenteContenidosTab
import cl.duoc.mpazpro.ui.components.DocenteObjetivosTab
import cl.duoc.mpazpro.ui.components.DocentePreguntasTab
import cl.duoc.mpazpro.ui.components.DocenteResultadosTab
import cl.duoc.mpazpro.viewmodel.DocenteViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Panel docente con gestión remota de contenidos y evaluaciones.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocenteScreen(
    viewModel: DocenteViewModel,
    onLogout: () -> Unit
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var pestana by rememberSaveable {
        mutableIntStateOf(0)
    }

    val cargando by viewModel.cargando.collectAsState()
    val errorRemoto by viewModel.errorRemoto.collectAsState()
    val objetivos by viewModel.objetivos.collectAsState()
    val contenidos by viewModel.contenidos.collectAsState()
    val preguntas by viewModel.preguntas.collectAsState()
    val filas by viewModel.filas.collectAsState()
    val config by viewModel.config.collectAsState()
    val contenidoForm by viewModel.contenido.collectAsState()
    val preguntaForm by viewModel.pregunta.collectAsState()

    val contenidoEditando by
    viewModel.contenidoEditando.collectAsState()

    val preguntaEditando by
    viewModel.preguntaEditando.collectAsState()

    // Oculta los mensajes de guardado.
    LaunchedEffect(
        contenidoForm.guardado,
        preguntaForm.guardado
    ) {

        if (contenidoForm.guardado || preguntaForm.guardado) {

            delay(2500)
            viewModel.limpiarMensajes()
        }
    }

    // Muestra errores del servidor aunque el usuario esté en el listado.
    LaunchedEffect(errorRemoto) {

        errorRemoto?.let {
            snackbar.showSnackbar(it)
        }
    }

    val titulos = listOf(
        "Objetivos",
        "Contenidos",
        "Preguntas",
        "Resultados"
    )

    val iconos = listOf(
        Icons.Default.Checklist,
        Icons.Default.Book,
        Icons.Default.Quiz,
        Icons.Default.Assessment
    )

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("Panel Docente")
                },

                actions = {

                    IconButton(
                        onClick = onLogout,
                        enabled = !cargando
                    ) {

                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión"
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

            ScrollableTabRow(
                selectedTabIndex = pestana,
                edgePadding = 8.dp
            ) {

                titulos.forEachIndexed { i, titulo ->

                    Tab(
                        selected = pestana == i,
                        onClick = { pestana = i },
                        enabled = !cargando,
                        text = { Text(titulo) },
                        icon = {

                            Icon(
                                iconos[i],
                                contentDescription = null
                            )
                        }
                    )
                }
            }

            if (pestana == 1) {

                Text(
                    "La imagen seleccionada es una vista previa local; aún no se comparte con otros dispositivos.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 4.dp
                    )
                )
            }

            // Mantiene las cuatro pestañas originales.
            AnimatedContent(
                targetState = pestana,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                modifier = Modifier.weight(1f),
                label = "pestanaDocente"
            ) { actual ->

                when (actual) {

                    0 -> DocenteObjetivosTab(
                        objetivos = objetivos,
                        config = config,
                        onAsignar = viewModel::alternarAsignado,
                        onAbrirConfig = viewModel::abrirConfig,
                        onIntentosChange =
                            viewModel::onIntentosChanged,
                        onGuardarConfig =
                            viewModel::guardarConfig,
                        onCerrarConfig =
                            viewModel::cerrarConfig
                    )

                    1 -> DocenteContenidosTab(
                        form = contenidoForm,
                        objetivos = objetivos,
                        contenidos = contenidos,
                        onOaChange =
                            viewModel::onContenidoOaChanged,
                        onTituloChange =
                            viewModel::onTituloChanged,
                        onExplicacionChange =
                            viewModel::onExplicacionChanged,
                        onEjemploChange =
                            viewModel::onEjemploChanged,
                        onIconoChange =
                            viewModel::onIconoChanged,
                        onImagenChange =
                            viewModel::onImagenChanged,
                        onGuardar =
                            viewModel::crearContenido,

                        editandoId = contenidoEditando,
                        cargando = cargando,
                        onEditar =
                            viewModel::editarContenido,
                        onEliminar =
                            viewModel::eliminarContenido,
                        onCancelarEdicion =
                            viewModel::cancelarEdicionContenido
                    )

                    2 -> DocentePreguntasTab(
                        form = preguntaForm,
                        objetivos = objetivos,
                        preguntas = preguntas,
                        onOaChange =
                            viewModel::onPreguntaOaChanged,
                        onEnunciadoChange =
                            viewModel::onEnunciadoChanged,
                        onOpcionAChange =
                            viewModel::onOpcionAChanged,
                        onOpcionBChange =
                            viewModel::onOpcionBChanged,
                        onOpcionCChange =
                            viewModel::onOpcionCChanged,
                        onTipoChange =
                            viewModel::onTipoChanged,
                        onDificultadChange =
                            viewModel::onDificultadChanged,
                        onCorrectaChange =
                            viewModel::onCorrectaChanged,
                        onGuardar =
                            viewModel::crearPregunta,

                        editandoId = preguntaEditando,
                        cargando = cargando,
                        onEditar =
                            viewModel::editarPregunta,
                        onEliminar =
                            viewModel::eliminarPregunta,
                        onCancelarEdicion =
                            viewModel::cancelarEdicionPregunta
                    )

                    else -> DocenteResultadosTab(
                        filas = filas,
                        onGenerar = viewModel::generarInforme,
                        onValidar = viewModel::validarInforme,

                        onCompartir = { fila ->

                            val informe = fila.informe

                            if (informe != null) {

                                val ok = compartirTexto(
                                    context,
                                    "Informe pedagógico - ${fila.estudiante.nombre}",
                                    textoInforme(
                                        fila.estudiante.nombre,
                                        fila.estudiante.rut,
                                        fila.objetivo,
                                        informe
                                    )
                                )

                                if (!ok) {

                                    scope.launch {

                                        snackbar.showSnackbar(
                                            "No hay aplicaciones para compartir"
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}