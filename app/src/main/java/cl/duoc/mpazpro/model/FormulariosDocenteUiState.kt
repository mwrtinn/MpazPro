package cl.duoc.mpazpro.model

import cl.duoc.mpazpro.data.local.entity.ObjetivoEntity

// ---------------------------------------------------------------------------------------------
// Formulario "Nuevo contenido didáctico" (docente)
// ---------------------------------------------------------------------------------------------

/** Errores de validación del formulario de contenido. `null` = campo válido. */
data class ContenidoErrores(
    val oa: String? = null,
    val titulo: String? = null,
    val explicacion: String? = null,
    val ejemplo: String? = null
)

/**
 * Estado del formulario de contenido.
 *
 * @property oaId Objetivo de aprendizaje al que pertenece el contenido.
 * @property icono Emoji usado como apoyo visual.
 * @property imagenPath Ruta de la imagen de apoyo (tomada con la cámara o elegida desde la galería).
 * @property guardado `true` cuando se acaba de guardar, para mostrar el mensaje de éxito animado.
 */
data class ContenidoUiState(
    val oaId: Int? = null,
    val titulo: String = "",
    val explicacion: String = "",
    val ejemplo: String = "",
    val icono: String = ICONOS_CONTENIDO.first(),
    val imagenPath: String? = null,
    val errores: ContenidoErrores = ContenidoErrores(),
    val guardado: Boolean = false
)

// ---------------------------------------------------------------------------------------------
// Formulario "Nueva pregunta" (docente)
// ---------------------------------------------------------------------------------------------

/** Errores de validación del formulario de pregunta. `null` = campo válido. */
data class PreguntaErrores(
    val oa: String? = null,
    val enunciado: String? = null,
    val opcionA: String? = null,
    val opcionB: String? = null,
    val opcionC: String? = null
)

/**
 * Estado del formulario de pregunta.
 *
 * @property tipo [TipoPregunta.DESAFIO] o [TipoPregunta.PRUEBA].
 * @property dificultad Nivel de 1 a 3 (los desafíos se presentan de menor a mayor dificultad).
 * @property correcta Índice de la alternativa correcta (0 = A, 1 = B, 2 = C).
 */
data class PreguntaUiState(
    val oaId: Int? = null,
    val tipo: String = TipoPregunta.DESAFIO,
    val dificultad: Int = 1,
    val enunciado: String = "",
    val opcionA: String = "",
    val opcionB: String = "",
    val opcionC: String = "",
    val correcta: Int = 0,
    val errores: PreguntaErrores = PreguntaErrores(),
    val guardado: Boolean = false
)

// ---------------------------------------------------------------------------------------------
// Diálogos de un solo campo
// ---------------------------------------------------------------------------------------------

/** Diálogo para definir los intentos de la prueba final de un objetivo. */
data class ConfigIntentosState(
    val objetivo: ObjetivoEntity? = null,
    val valor: String = "",
    val error: String? = null
)
