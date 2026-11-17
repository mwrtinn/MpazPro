package cl.duoc.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mpazpro.data.local.entity.ContenidoEntity
import cl.duoc.mpazpro.model.QuizUiState
import cl.duoc.mpazpro.model.ResultadoPregunta
import cl.duoc.mpazpro.data.local.entity.ObjetivoEntity
import cl.duoc.mpazpro.data.local.entity.PreguntaEntity
import cl.duoc.mpazpro.data.local.entity.RespuestaEntity
import cl.duoc.mpazpro.repository.AulaRepository
import cl.duoc.mpazpro.utils.Evaluador
import cl.duoc.mpazpro.model.TipoPregunta
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
/**
 * ViewModel del perfil estudiante. Las tres pantallas del estudiante (inicio, objetivo y quiz)
 * comparten esta misma instancia, así el estado no se pierde al navegar entre ellas.
 */
class EstudianteViewModel(private val repository: AulaRepository) : ViewModel() {

    private val rutActual = MutableStateFlow("")
    private val oaActual = MutableStateFlow(0)

    /** Solo los objetivos que el docente asignó. */
    val objetivos: StateFlow<List<ObjetivoEntity>> = repository.objetivosAsignados()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val respuestas: StateFlow<List<RespuestaEntity>> = rutActual
        .flatMapLatest { repository.respuestasDeAlumno(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val objetivo: StateFlow<ObjetivoEntity?> = oaActual
        .flatMapLatest { repository.observarObjetivo(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Solo contenidos aprobados por UTP. */
    val contenidos: StateFlow<List<ContenidoEntity>> = oaActual
        .flatMapLatest { repository.contenidosAprobados(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _quiz = MutableStateFlow(QuizUiState())
    val quiz: StateFlow<QuizUiState> = _quiz.asStateFlow()

    fun iniciarSesion(rut: String) {
        rutActual.value = rut
    }

    fun seleccionarObjetivo(id: Int) {
        oaActual.value = id
    }

    fun iniciarQuiz(oaId: Int, tipo: String) {
        viewModelScope.launch {
            _quiz.value = QuizUiState(cargando = true, tipo = tipo)
            val oa = repository.observarObjetivo(oaId).first()
            val lista = repository.preguntasDe(oaId, tipo)
            val usados = repository.ultimoIntento(rutActual.value, oaId, tipo)
            val titulo = if (tipo == TipoPregunta.PRUEBA) "Prueba final" else "Desafíos"

            _quiz.value = when {
                oa == null || !oa.asignado ->
                    QuizUiState(cargando = false, tipo = tipo, titulo = titulo, bloqueadoMensaje = "Este objetivo no está disponible.")
                lista.isEmpty() ->
                    QuizUiState(cargando = false, tipo = tipo, titulo = titulo, bloqueadoMensaje = "Tu docente aún no agrega preguntas.")
                tipo == TipoPregunta.PRUEBA && !Evaluador.puedeRendirPrueba(oa.intentosPrueba, usados) ->
                    QuizUiState(
                        cargando = false, tipo = tipo, titulo = titulo,
                        bloqueadoMensaje = "Ya usaste los ${oa.intentosPrueba} intentos de la prueba final."
                    )
                else -> QuizUiState(
                    cargando = false, tipo = tipo, titulo = titulo, preguntas = lista,
                    intento = usados + 1, intentosPermitidos = oa.intentosPrueba
                )
            }
        }
    }

    fun seleccionar(alternativa: Int) {
        _quiz.update { if (it.respondida) it else it.copy(seleccion = alternativa) }
    }

    /** Corrige y guarda la respuesta. En la práctica muestra el resultado; en la prueba avanza directo. */
    fun responder() {
        val s = _quiz.value
        val pregunta = s.preguntaActual ?: return
        if (s.respondida || s.terminado || s.seleccion < 0) return

        val correcta = Evaluador.esCorrecta(pregunta, s.seleccion)
        viewModelScope.launch {
            repository.guardarRespuesta(
                RespuestaEntity(
                    rutAlumno = rutActual.value, oaId = pregunta.oaId, preguntaId = pregunta.id,
                    tipo = pregunta.tipo, dificultad = pregunta.dificultad, intento = s.intento,
                    elegida = s.seleccion, esCorrecta = correcta, fecha = System.currentTimeMillis()
                )
            )
        }
        val conResultado = s.copy(resultados = s.resultados + ResultadoPregunta(pregunta, s.seleccion, correcta))
        if (s.tipo == TipoPregunta.PRUEBA) avanzar(conResultado) else _quiz.value = conResultado.copy(respondida = true)
    }

    fun siguiente() {
        val s = _quiz.value
        if (s.respondida) avanzar(s)
    }

    private fun avanzar(s: QuizUiState) {
        _quiz.value = if (s.indice >= s.preguntas.lastIndex) {
            s.copy(terminado = true, respondida = false, seleccion = -1)
        } else {
            s.copy(indice = s.indice + 1, seleccion = -1, respondida = false)
        }
    }
}
