package cl.duoc.mpazpro.utils

import cl.duoc.mpazpro.data.local.entity.RespuestaEntity
import cl.duoc.mpazpro.model.TipoPregunta

data class ResumenInforme(
    val rendimiento: Int,
    val fortalezas: String,
    val refuerzo: String,
    val totalRespuestas: Int
)

/**
 * Arma el informe pedagógico a partir de las respuestas registradas.
 * Solo describe resultados: no emite diagnósticos ni decisiones académicas. El docente debe validarlo.
 */
object GeneradorInforme {

    fun generar(respuestas: List<RespuestaEntity>): ResumenInforme {
        if (respuestas.isEmpty()) {
            return ResumenInforme(0, "Sin actividad registrada.", "Sin actividad registrada.", 0)
        }

        // Rendimiento: último intento de la prueba final; si aún no la rinde, las actividades de práctica.
        val pruebas = respuestas.filter { it.tipo == TipoPregunta.PRUEBA }
        val base = if (pruebas.isNotEmpty()) {
            val ultimo = pruebas.maxOf { it.intento }
            pruebas.filter { it.intento == ultimo }
        } else {
            respuestas
        }
        val rendimiento = Evaluador.porcentaje(base.count { it.esCorrecta }, base.size)

        val fortalezas = mutableListOf<String>()
        val refuerzo = mutableListOf<String>()
        val porNivel = respuestas.groupBy { it.dificultad }.toSortedMap()
        for ((nivel, lista) in porNivel) {
            val p = Evaluador.porcentaje(lista.count { it.esCorrecta }, lista.size)
            val etiqueta = "preguntas de nivel $nivel ($p% correctas)"
            if (p >= 70) fortalezas.add(etiqueta) else if (p < 50) refuerzo.add(etiqueta)
        }

        return ResumenInforme(
            rendimiento = rendimiento,
            fortalezas = if (fortalezas.isEmpty()) "Aún sin fortalezas claras." else fortalezas.joinToString("; "),
            refuerzo = if (refuerzo.isEmpty()) "Sin contenidos que requieran refuerzo por ahora." else refuerzo.joinToString("; "),
            totalRespuestas = respuestas.size
        )
    }
}

/** Clave única del informe de un estudiante en un objetivo. */
fun claveInforme(rut: String, oaId: Int) = "$rut-$oaId"

/** Fecha y hora legibles para el informe. */
fun fechaActual(): String =
    java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.forLanguageTag("es-CL")).format(java.util.Date())

/** Texto plano del informe, para compartirlo o descargarlo (uso docente). */
fun textoInforme(
    nombreAlumno: String,
    rut: String,
    objetivo: cl.duoc.mpazpro.data.local.entity.ObjetivoEntity,
    informe: cl.duoc.mpazpro.data.local.entity.InformeEntity
): String = buildString {
    appendLine("INFORME PEDAGÓGICO (uso docente)")
    appendLine("Escuela Escritora Marcela Paz - 3° Básico")
    appendLine("Estudiante: $nombreAlumno ($rut)")
    appendLine("Objetivo: ${objetivo.codigo} - ${objetivo.asignatura}")
    appendLine("Rendimiento: ${informe.rendimiento}%")
    appendLine("Fortalezas: ${informe.fortalezas}")
    appendLine("A reforzar: ${informe.refuerzo}")
    appendLine("Fecha: ${informe.fecha}")
    appendLine("Estado: ${if (informe.validado) "VALIDADO por docente" else "PENDIENTE de validación docente"}")
    append("Este informe describe resultados; no es un diagnóstico ni una decisión académica.")
}
