package cl.duoc.mpazpro.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** DataStore de Preferences de la aplicación (se crea una sola vez por Context). */
val Context.dataStore by preferencesDataStore(name = "preferencias_usuario")

/**
 * Guarda preferencias simples del usuario con DataStore Preferences.
 * Aquí se recuerda el último RUT ingresado (nunca la contraseña) cuando el usuario lo pide.
 */
class PreferenciasDataStore(private val context: Context) {

    // Clave usada para guardar el RUT recordado
    private val rutRecordado = stringPreferencesKey("rut_recordado")

    /** Guarda el RUT (normalizado) para sugerirlo en el siguiente ingreso. */
    suspend fun guardarRut(rut: String) {
        context.dataStore.edit { preferencias -> preferencias[rutRecordado] = rut }
    }

    /** Elimina el RUT recordado. */
    suspend fun borrarRut() {
        context.dataStore.edit { preferencias -> preferencias.remove(rutRecordado) }
    }

    /** Devuelve el RUT recordado como Flow reactivo (`null` si no hay ninguno). */
    fun obtenerRut(): Flow<String?> = context.dataStore.data.map { preferencias -> preferencias[rutRecordado] }
}
