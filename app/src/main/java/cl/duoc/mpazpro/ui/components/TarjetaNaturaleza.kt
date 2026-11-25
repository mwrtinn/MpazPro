package cl.duoc.mpazpro.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import cl.duoc.mpazpro.recursos.rememberVoz
import cl.duoc.mpazpro.viewmodel.NaturalezaViewModel

// Minijuego educativo conectado con iNaturalist.
@Composable
fun TarjetaNaturaleza(
    viewModel: NaturalezaViewModel
) {

    val estado by viewModel.estado.collectAsState()
    val voz = rememberVoz()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.secondaryContainer
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    "🌿 Exploradores de Chile",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    Icons.Default.Star,
                    contentDescription = "Estrellas",
                    tint = MaterialTheme.colorScheme.primary
                )

                Text("${estado.estrellas}")
            }

            Text(
                "Descubre las plantas de nuestro país.",
                style = MaterialTheme.typography.bodyMedium
            )

            when {

                // Estado de carga de la API.
                estado.cargando -> {

                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        "Buscando fotografías de la naturaleza..."
                    )
                }

                // Manejo de errores y reintentos.
                estado.error != null -> {

                    Text(
                        estado.error.orEmpty(),
                        color = MaterialTheme.colorScheme.error
                    )

                    Button(
                        onClick = viewModel::iniciarJuego,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null
                        )

                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )

                        Text("Reintentar")
                    }
                }

                // Pantalla inicial.
                estado.preguntas.isEmpty() -> {

                    Text(
                        "🌱 Observa las fotografías."
                    )

                    Text(
                        "🧠 Descubre el nombre de cada planta."
                    )

                    Text(
                        "⭐ Gana estrellas mientras aprendes."
                    )

                    Text(
                        "Recuerda: cuidar las plantas es cuidar nuestro planeta."
                    )

                    Button(
                        onClick = viewModel::iniciarJuego,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {

                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null
                        )

                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )

                        Text("Comenzar aventura")
                    }
                }

                // Resumen de la partida.
                estado.terminado -> {

                    Text(
                        "¡Aventura completada!",
                        style = MaterialTheme
                            .typography.headlineSmall
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {

                        repeat(estado.preguntas.size) { i ->

                            Icon(
                                Icons.Default.Star,
                                contentDescription =
                                    if (i < estado.estrellas)
                                        "Estrella obtenida"
                                    else
                                        "Estrella pendiente",
                                modifier = Modifier.size(36.dp),
                                tint = if (
                                    i < estado.estrellas
                                ) {

                                    MaterialTheme.colorScheme.primary

                                } else {

                                    MaterialTheme.colorScheme
                                        .onSurface.copy(alpha = 0.25f)
                                }
                            )
                        }
                    }

                    Text(
                        "${estado.puntos} de ${
                            estado.preguntas.size * 10
                        } puntos",
                        style = MaterialTheme
                            .typography.headlineMedium
                    )

                    Text(
                        "Reconociste ${estado.estrellas} de ${
                            estado.preguntas.size
                        } plantas."
                    )

                    Text(
                        if (
                            estado.estrellas ==
                            estado.preguntas.size
                        ) {

                            "¡Eres un gran explorador de la naturaleza!"

                        } else {

                            "¡Muy bien! Sigue practicando para descubrir más plantas."
                        }
                    )

                    Button(
                        onClick = viewModel::volverAJugar,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Text("Volver a jugar")
                    }
                }

                // Presentación de preguntas.
                else -> {

                    val pregunta = estado.preguntaActual

                    if (pregunta != null) {

                        var imagenLista by remember(
                            pregunta.imagen,
                            estado.indice
                        ) {
                            mutableStateOf(false)
                        }

                        var errorImagen by remember(
                            pregunta.imagen,
                            estado.indice
                        ) {
                            mutableStateOf(false)
                        }

                        var reintento by remember(
                            pregunta.imagen,
                            estado.indice
                        ) {
                            mutableIntStateOf(0)
                        }

                        Text(
                            "Pregunta ${estado.indice + 1} de ${
                                estado.preguntas.size
                            }"
                        )

                        LinearProgressIndicator(
                            progress = {
                                (
                                        estado.indice +
                                                if (estado.respondida)
                                                    1
                                                else
                                                    0
                                        ) / estado.preguntas.size.toFloat()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Carga la fotografía obtenida de la API.
                        key(
                            pregunta.imagen,
                            estado.indice,
                            reintento
                        ) {

                            AsyncImage(
                                model = pregunta.imagen,
                                contentDescription =
                                    "Fotografía de la planta que debes identificar",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                contentScale = ContentScale.Fit,

                                onLoading = {
                                    imagenLista = false
                                    errorImagen = false
                                },

                                onSuccess = {
                                    imagenLista = true
                                    errorImagen = false
                                },

                                onError = {
                                    imagenLista = false
                                    errorImagen = true
                                }
                            )
                        }

                        if (!imagenLista && !errorImagen) {

                            Text("Cargando fotografía...")
                        }

                        if (errorImagen) {

                            Text(
                                "No se pudo cargar la fotografía.",
                                color = MaterialTheme
                                    .colorScheme.error
                            )

                            TextButton(
                                onClick = {

                                    errorImagen = false
                                    imagenLista = false
                                    reintento++
                                }
                            ) {

                                Text("Reintentar fotografía")
                            }
                        }

                        // Reconoce al autor y licencia de la imagen.
                        Text(
                            pregunta.autor,
                            style = MaterialTheme
                                .typography.bodySmall
                        )

                        Text(
                            "Licencia: ${pregunta.licencia.uppercase()}",
                            style = MaterialTheme
                                .typography.bodySmall
                        )

                        pregunta.fotoId?.let { id ->

                            Text(
                                "Fuente: iNaturalist.org/photos/$id",
                                style = MaterialTheme
                                    .typography.bodySmall
                            )
                        }

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Text(
                                "¿Qué planta aparece en la fotografía?",
                                style = MaterialTheme
                                    .typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = {

                                    voz.hablar(
                                        "¿Qué planta aparece en la fotografía? " +
                                                pregunta.alternativas
                                                    .joinToString(". ")
                                    )
                                }
                            ) {

                                Icon(
                                    Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription =
                                        "Escuchar pregunta"
                                )
                            }
                        }

                        // Alternativas de la pregunta.
                        pregunta.alternativas.forEach { alternativa ->

                            OutlinedButton(
                                onClick = {

                                    viewModel.responder(alternativa)
                                },

                                enabled =
                                    imagenLista &&
                                            !estado.respondida,

                                modifier = Modifier.fillMaxWidth()
                            ) {

                                Text(
                                    alternativa,
                                    modifier = Modifier.weight(1f)
                                )

                                if (
                                    estado.respondida &&
                                    alternativa ==
                                    pregunta.planta.nombre
                                ) {

                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription =
                                            "Respuesta correcta"
                                    )
                                }
                            }
                        }

                        // Retroalimentación educativa.
                        if (estado.respondida) {

                            val correcta =
                                estado.seleccion ==
                                        pregunta.planta.nombre

                            HorizontalDivider()

                            Text(
                                if (correcta)
                                    "¡Excelente! Ganaste 10 puntos ⭐"
                                else
                                    "¡Sigue aprendiendo!",
                                style = MaterialTheme
                                    .typography.titleMedium,
                                color = MaterialTheme
                                    .colorScheme.primary
                            )

                            Text(
                                "Respuesta: ${
                                    pregunta.planta.nombre
                                }"
                            )

                            Text(
                                pregunta.planta.cientifico,
                                style = MaterialTheme
                                    .typography.bodySmall
                            )

                            Text(
                                pregunta.planta.datoCurioso
                            )

                            TextButton(
                                onClick = {

                                    voz.hablar(
                                        pregunta.planta.datoCurioso
                                    )
                                }
                            ) {

                                Icon(
                                    Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null
                                )

                                Spacer(
                                    modifier = Modifier.width(8.dp)
                                )

                                Text("Escuchar dato curioso")
                            }

                            Button(
                                onClick = viewModel::siguiente,
                                modifier = Modifier.fillMaxWidth()
                            ) {

                                Text(
                                    if (
                                        estado.indice ==
                                        estado.preguntas.lastIndex
                                    )
                                        "Ver resultados"
                                    else
                                        "Siguiente pregunta"
                                )
                            }
                        }

                        TextButton(
                            onClick = viewModel::volverAJugar
                        ) {

                            Text("Reiniciar aventura")
                        }
                    }
                }
            }

            HorizontalDivider()

            Text(
                "Fotografías y datos de especies: iNaturalist.",
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                "Actividad recreativa de Ciencias Naturales.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}