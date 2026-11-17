package cl.duoc.mpazpro.model

/**
 * Perfiles de la aplicación. Se guardan como texto en Room.
 * Los tres perfiles (estudiante, docente y UTP) ingresan siempre con su RUT.
 */
object Rol {
    const val ESTUDIANTE = "ESTUDIANTE"
    const val DOCENTE = "DOCENTE"
    const val UTP = "UTP"

    fun titulo(rol: String) = when (rol) {
        ESTUDIANTE -> "Estudiante"
        DOCENTE -> "Docente"
        else -> "Jefatura UTP"
    }
}

/** Estado de aprobación de un contenido; lo decide la jefatura de UTP. */
object EstadoContenido {
    const val PENDIENTE = "PENDIENTE"
    const val APROBADO = "APROBADO"
    const val RECHAZADO = "RECHAZADO"
}

/**
 * DESAFIO = práctica progresiva con retroalimentación inmediata.
 * PRUEBA = evaluación final con intentos limitados.
 */
object TipoPregunta {
    const val DESAFIO = "DESAFIO"
    const val PRUEBA = "PRUEBA"
}

/** Íconos (emojis) disponibles como apoyo visual en los contenidos. */
val ICONOS_CONTENIDO = listOf("📘", "🔢", "📖", "🌱", "🧭", "🔬", "🗺️", "✏️")
