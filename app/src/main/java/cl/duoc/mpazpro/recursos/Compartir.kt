package cl.duoc.mpazpro.recursos

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent

/**
 * Recurso nativo: menú "Compartir" del sistema. El docente lo usa para descargar o enviar el informe
 * (guardarlo en Drive, enviarlo por correo, etc.). No requiere permisos. Devuelve false si no hay apps.
 */
fun compartirTexto(context: Context, asunto: String, texto: String): Boolean {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, asunto)
        putExtra(Intent.EXTRA_TEXT, texto)
    }
    return try {
        context.startActivity(Intent.createChooser(intent, "Compartir informe"))
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}
