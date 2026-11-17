package cl.duoc.mpazpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import cl.duoc.mpazpro.data.local.AppDatabase
import cl.duoc.mpazpro.data.local.PreferenciasDataStore
import cl.duoc.mpazpro.navigation.AppNavigation
import cl.duoc.mpazpro.ui.theme.MpazProTheme
import cl.duoc.mpazpro.viewmodel.ViewModelFactory

/** Punto de entrada: crea la base de datos y el DataStore, arma la fábrica y lanza la navegación. */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)
        val factory = ViewModelFactory(database, PreferenciasDataStore(applicationContext))

        setContent {
            MpazProTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavigation(navController = navController, factory = factory)
                }
            }
        }
    }
}
