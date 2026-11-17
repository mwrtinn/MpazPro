package cl.duoc.mpazpro.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AzulClaro,
    secondary = NaranjoClaro,
    tertiary = VerdeClaro
)

private val LightColorScheme = lightColorScheme(
    primary = AzulEscuela,
    secondary = NaranjoEscuela,
    tertiary = VerdeEscuela,
    background = FondoClaro
)

@Composable
fun MpazProTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Se usa la paleta de la escuela (no el color dinámico del teléfono) para que la
    // identidad visual sea la misma en todos los dispositivos.
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
