package cl.duoc.mpazpro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// Controla las operaciones remotas y evita envíos simultáneos.
abstract class OperacionRemotaViewModel : ViewModel() {

    private val _cargando = MutableStateFlow(false)
    val cargando = _cargando.asStateFlow()

    private val _errorRemoto = MutableStateFlow<String?>(null)
    val errorRemoto = _errorRemoto.asStateFlow()

    protected fun ejecutar(operacion: suspend () -> Unit) {

        if (_cargando.value) return

        _cargando.value = true
        _errorRemoto.value = null

        viewModelScope.launch {

            try {

                operacion()

            } catch (e: CancellationException) {

                throw e

            } catch (e: HttpException) {

                _errorRemoto.value = when (e.code()) {

                    400 -> "Datos inválidos. Revisa los campos."

                    401 -> "Sesión no válida o vencida. Vuelve a iniciar sesión."

                    403 -> "No tienes permisos para realizar esta operación."

                    404 -> "El registro ya no está disponible. Actualiza los datos."

                    409 -> "La operación entra en conflicto con los datos actuales. Actualiza antes de reintentar."

                    else -> "Error del servidor (${e.code()})."
                }

            } catch (e: IOException) {

                _errorRemoto.value =
                    "No se pudo conectar con el servidor. Verifica los microservicios y actualiza los datos antes de reintentar."

            } catch (e: Exception) {

                _errorRemoto.value =
                    "No se pudo completar la operación. Actualiza los datos antes de reintentar."

            } finally {

                _cargando.value = false
            }
        }
    }
}