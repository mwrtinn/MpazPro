package cl.duoc.mpazpro.recursos

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext

/**
 * Recurso nativo: estado de la red (ConnectivityManager). El caso indica uso con conexión a internet,
 * así que la app avisa cuando se pierde. Solo requiere el permiso normal ACCESS_NETWORK_STATE.
 */
fun estaConectado(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val red = cm.activeNetwork ?: return false
    val capacidades = cm.getNetworkCapabilities(red) ?: return false
    return capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

/** Estado reactivo: true mientras haya conexión. Se desuscribe solo al salir de la pantalla. */
@Composable
fun rememberConectado(): State<Boolean> {
    val context = LocalContext.current
    return produceState(initialValue = estaConectado(context), context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                value = true
            }

            override fun onLost(network: Network) {
                value = estaConectado(context)
            }
        }
        cm.registerDefaultNetworkCallback(callback)
        awaitDispose { cm.unregisterNetworkCallback(callback) }
    }
}
