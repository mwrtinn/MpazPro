package cl.duoc.mpazpro.model

import cl.duoc.mpazpro.data.local.entity.ContenidoEntity
import cl.duoc.mpazpro.data.local.entity.InformeEntity
import cl.duoc.mpazpro.data.local.entity.ObjetivoEntity
import cl.duoc.mpazpro.data.local.entity.PreguntaEntity
import cl.duoc.mpazpro.data.local.entity.RespuestaEntity
import cl.duoc.mpazpro.data.local.entity.UsuarioEntity

/** Respuesta de un estudiante a una pregunta, con su corrección. */
data class ResultadoPregunta(val pregunta: PreguntaEntity, val elegida: Int, val correcta: Boolean)

/**
 * Estado de una sesión de práctica (DESAFIO) o de la prueba final (PRUEBA).
 *
 * @property bloqueadoMensaje Si no es `null`, la sesión no puede iniciar (sin intentos, sin preguntas, etc.).
 * @property intentosPermitidos Máximo de intentos de la prueba final definido por el docente.
 */
data class QuizUiState(
    val cargando: Boolean = true,
    val tipo: String = TipoPregunta.DESAFIO,
    val titulo: String = "",
    val preguntas: List<PreguntaEntity> = emptyList(),
    val indice: Int = 0,
    val seleccion: Int = -1,
    val respondida: Boolean = false,
    val resultados: List<ResultadoPregunta> = emptyList(),
    val terminado: Boolean = false,
    val intento: Int = 1,
    val intentosPermitidos: Int = 0,
    val bloqueadoMensaje: String? = null
) {
    val preguntaActual: PreguntaEntity? get() = preguntas.getOrNull(indice)
    val correctas: Int get() = resultados.count { it.correcta }
}

/** Un estudiante frente a un objetivo asignado, con sus respuestas e informe (vista del docente). */
data class FilaResultado(
    val estudiante: UsuarioEntity,
    val objetivo: ObjetivoEntity,
    val respuestas: List<RespuestaEntity>,
    val informe: InformeEntity?
)

// ---------------------------------------------------------------------------------------------
// Revisión de UTP
// ---------------------------------------------------------------------------------------------

/** Contenido junto al objetivo al que pertenece, para que UTP verifique la relación. */
data class ContenidoRevision(val contenido: ContenidoEntity, val objetivo: ObjetivoEntity?)

/** Diálogo de rechazo: exige una observación para el docente. */
data class RevisionState(
    val contenido: ContenidoEntity? = null,
    val observacion: String = "",
    val error: String? = null
)
