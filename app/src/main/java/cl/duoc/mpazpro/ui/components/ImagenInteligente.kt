package cl.duoc.mpazpro.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cl.duoc.mpazpro.recursos.ImagenStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Componente reutilizable que muestra una imagen guardada en el dispositivo (Guía 13).
 *
 * - Carga la imagen en segundo plano (no bloquea la interfaz) y reducida para ahorrar memoria.
 * - Si no hay imagen (o no se puede leer), muestra un ícono por defecto.
 *
 * @param ruta Ruta del archivo de la imagen, o `null` si no hay.
 * @param modifier Define el tamaño (por ejemplo `Modifier.size(120.dp)` o `Modifier.fillMaxWidth().height(160.dp)`).
 * @param circular `true` para recortar en círculo; `false` para esquinas redondeadas.
 */
@Composable
fun ImagenInteligente(
    ruta: String?,
    modifier: Modifier = Modifier.size(120.dp),
    circular: Boolean = true
) {
    val imagen by produceState<ImageBitmap?>(initialValue = null, ruta) {
        value = if (ruta == null) null else withContext(Dispatchers.IO) {
            ImagenStorage.cargarBitmap(ruta)?.asImageBitmap()
        }
    }

    Box(
        modifier = modifier
            .clip(if (circular) CircleShape else RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        val cargada = imagen
        if (cargada != null) {
            Image(
                bitmap = cargada,
                contentDescription = "Imagen de apoyo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = "Sin imagen",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
