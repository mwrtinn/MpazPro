package cl.duoc.mpazpro.utils

import cl.duoc.mpazpro.data.local.entity.PreguntaEntity

/** Reglas de negocio de la corrección y de los intentos (sin Android, fáciles de probar). */
object Evaluador {

    fun esCorrecta(p: PreguntaEntity, alternativa: Int): Boolean = alternativa == p.correcta

    fun textoAlternativa(p: PreguntaEntity, indice: Int): String = when (indice) {
        0 -> p.opcionA
        1 -> p.opcionB
        else -> p.opcionC
    }

    fun letra(indice: Int): Char = 'A' + indice

    fun porcentaje(correctas: Int, total: Int): Int = if (total == 0) 0 else correctas * 100 / total

    fun puedeRendirPrueba(intentosPermitidos: Int, intentosUsados: Int): Boolean = intentosUsados < intentosPermitidos

    fun intentosRestantes(intentosPermitidos: Int, intentosUsados: Int): Int =
        (intentosPermitidos - intentosUsados).coerceAtLeast(0)
}
