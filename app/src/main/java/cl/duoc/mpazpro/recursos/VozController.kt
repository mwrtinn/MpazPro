package cl.duoc.mpazpro.recursos

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Recurso nativo: síntesis de voz (TextToSpeech) como audio de apoyo a la lectura.
 * No requiere permisos ni micrófono. Se debe llamar a [liberar] al salir de la pantalla.
 */
class VozController(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var listo = false

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) return
        listo = idiomaDisponible(Locale.forLanguageTag("es-CL")) || idiomaDisponible(Locale.forLanguageTag("es"))
    }

    private fun idiomaDisponible(locale: Locale): Boolean {
        val r = tts?.setLanguage(locale) ?: return false
        return r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED
    }

    /** Devuelve false si el motor de voz aún no está listo o no tiene español instalado. */
    fun hablar(texto: String): Boolean {
        if (!listo) return false
        tts?.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "mpazpro_tts")
        return true
    }

    fun liberar() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        listo = false
    }
}

/** Crea el controlador de voz ligado al ciclo de vida de la pantalla (se libera solo al salir). */
@androidx.compose.runtime.Composable
fun rememberVoz(): VozController {
    val context = androidx.compose.ui.platform.LocalContext.current
    val voz = androidx.compose.runtime.remember { VozController(context) }
    androidx.compose.runtime.DisposableEffect(voz) { onDispose { voz.liberar() } }
    return voz
}
