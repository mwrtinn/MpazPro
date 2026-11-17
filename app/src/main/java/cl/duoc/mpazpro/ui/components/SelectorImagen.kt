package cl.duoc.mpazpro.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import cl.duoc.mpazpro.recursos.ImagenStorage
import kotlinx.coroutines.launch
import java.io.File

/**
 * Recursos nativos: **cámara** (TakePicture) y **galería** (GetContent) con ActivityResultLauncher (Guía 13).
 *
 * Seguridad:
 * - El permiso de cámara se pide en tiempo de ejecución antes de abrirla.
 * - La cámara guarda la foto mediante una URI de FileProvider en el almacenamiento interno de la app.
 * - La galería usa el selector del sistema, por lo que no requiere permisos de almacenamiento.
 * - Si el usuario cancela cualquiera de las acciones, la app no falla: simplemente no cambia nada.
 *
 * @param imagenPath Ruta de la imagen actual (`null` si no hay).
 * @param onImagenChange Recibe la ruta de la nueva imagen, o `null` cuando se quita.
 */
@Composable
fun SelectorImagen(
    imagenPath: String?,
    onImagenChange: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var mensaje by remember { mutableStateOf<String?>(null) }
    // rememberSaveable: sobrevive a una rotación mientras la cámara está abierta
    var rutaCamara by rememberSaveable { mutableStateOf<String?>(null) }

    // --- Cámara: la foto queda en el archivo que le entregamos por URI ---
    val camaraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        val ruta = rutaCamara
        rutaCamara = null
        if (exito && ruta != null) {
            onImagenChange(ruta)
            mensaje = null
        } else if (ruta != null) {
            File(ruta).delete() // el usuario canceló: se descarta el archivo vacío
        }
    }

    fun abrirCamara() {
        val archivo = ImagenStorage.crearArchivo(context)
        rutaCamara = archivo.absolutePath
        camaraLauncher.launch(ImagenStorage.uriDe(context, archivo))
    }

    // --- Permiso de cámara en tiempo de ejecución ---
    val permisoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) abrirCamara()
        else mensaje = "Sin permiso de cámara no se puede tomar la foto. Puedes elegir una imagen de la galería."
    }

    // --- Galería: el selector del sistema devuelve una URI que copiamos al almacenamiento interno ---
    val galeriaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch {
                val ruta = ImagenStorage.copiarDesde(context, uri)
                if (ruta != null) {
                    onImagenChange(ruta)
                    mensaje = null
                } else {
                    mensaje = "No se pudo leer la imagen elegida."
                }
            }
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Imagen de apoyo (opcional)", style = MaterialTheme.typography.bodyLarge)
        Text(
            "Usa imágenes propias o autorizadas. No fotografíes a estudiantes.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            ImagenInteligente(ruta = imagenPath, modifier = Modifier.size(88.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedButton(onClick = {
                    val concedido = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED
                    if (concedido) abrirCamara() else permisoLauncher.launch(Manifest.permission.CAMERA)
                }) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tomar foto")
                }
                OutlinedButton(onClick = { galeriaLauncher.launch("image/*") }) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Elegir de la galería")
                }
            }
        }

        if (imagenPath != null) {
            TextButton(onClick = { onImagenChange(null) }) { Text("Quitar imagen") }
        }
        mensaje?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
    }
}
