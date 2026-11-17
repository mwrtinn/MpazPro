package cl.duoc.mpazpro.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.mpazpro.utils.Evaluador
import cl.duoc.mpazpro.model.TipoPregunta
import cl.duoc.mpazpro.recursos.rememberVoz
import cl.duoc.mpazpro.ui.components.BannerConexion
import cl.duoc.mpazpro.ui.components.TarjetaContenido
import cl.duoc.mpazpro.viewmodel.EstudianteViewModel
import kotlinx.coroutines.launch

/** Detalle de un objetivo: contenidos aprobados por UTP, desafíos de práctica y prueba final. */
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
    LaunchedEffect(oaId) { viewModel.seleccionarObjetivo(oaId) }

    val voz = rememberVoz()
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val escuchar: (String) -> Unit = { texto ->
        if (!voz.hablar(texto)) scope.launch { snackbar.showSnackbar("La voz no está disponible en este dispositivo") }
    }

    val oa = objetivo
    val intentosUsados = respuestas.filter { it.oaId == oaId && it.tipo == TipoPregunta.PRUEBA }.maxOfOrNull { it.intento } ?: 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(oa?.asignatura ?: "Objetivo") },
                navigationIcon = {
                    IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            BannerConexion()
            if (oa == null) return@Column
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("${oa.codigo} · ${oa.unidad}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Text(oa.descripcion, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 4.dp))
                }
                item { Text("Aprende", style = MaterialTheme.typography.titleLarge) }
                if (contenidos.isEmpty()) {
                    item { Text("Aún no hay contenidos autorizados por UTP para este objetivo.", style = MaterialTheme.typography.bodyMedium) }
                }
                items(contenidos, key = { it.id }) { c ->
                    TarjetaContenido(contenido = c, onEscuchar = { escuchar("${c.titulo}. ${c.explicacion}. Ejemplo: ${c.ejemplo}") })
                }
                item {
                    Text("Practica y demuestra", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { onIniciarQuiz(TipoPregunta.DESAFIO) }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                        Icon(Icons.Default.FitnessCenter, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Desafíos de práctica")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val restantes = Evaluador.intentosRestantes(oa.intentosPrueba, intentosUsados)
                    OutlinedButton(
                        onClick = { onIniciarQuiz(TipoPregunta.PRUEBA) },
                        enabled = restantes > 0,
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Prueba final (intentos disponibles: $restantes de ${oa.intentosPrueba})")
                    }
                }
            }
        }
    }
}
