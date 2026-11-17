package cl.duoc.mpazpro.recursos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Manejo de las imágenes de apoyo de los contenidos (recursos nativos: cámara y galería).
 *
 * - Todas las imágenes se guardan en el almacenamiento **interno** de la app (`filesDir/imagenes`),
 *   que es privado: ninguna otra app puede leerlo.
 * - La cámara recibe una URI de [FileProvider] (no una ruta directa), que es la forma segura
 *   de compartir un archivo con otra aplicación.
 */
object ImagenStorage {

    private fun directorio(context: Context): File = File(context.filesDir, "imagenes").apply { mkdirs() }

    /** Crea un archivo vacío donde la cámara dejará la foto. */
    fun crearArchivo(context: Context): File =
        File(directorio(context), "img_${System.currentTimeMillis()}.jpg").apply { createNewFile() }

    /** URI segura (FileProvider) para pasarle el archivo a la cámara. */
    fun uriDe(context: Context, archivo: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)

    /** Copia una imagen elegida en la galería al almacenamiento interno. Devuelve su ruta o `null` si falla. */
    suspend fun copiarDesde(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val destino = crearArchivo(context)
            val copiado = context.contentResolver.openInputStream(uri)?.use { entrada ->
                destino.outputStream().use { salida -> entrada.copyTo(salida) }
                true
            } ?: false
            if (copiado) destino.absolutePath else null
        }.getOrNull()
    }

    /** Carga la imagen reducida (lado máximo [maxLado] px) para no gastar memoria con fotos grandes. */
    fun cargarBitmap(ruta: String, maxLado: Int = 800): Bitmap? {
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(ruta, limites)
        if (limites.outWidth <= 0 || limites.outHeight <= 0) return null

        var muestreo = 1
        while (limites.outWidth / muestreo > maxLado * 2 || limites.outHeight / muestreo > maxLado * 2) muestreo *= 2
        val opciones = BitmapFactory.Options().apply { inSampleSize = muestreo }
        return BitmapFactory.decodeFile(ruta, opciones)
    }
}
