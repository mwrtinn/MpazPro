package cl.duoc.mpazpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import cl.duoc.mpazpro.data.AppDatabase
import cl.duoc.mpazpro.navigation.AppNavigation
import cl.duoc.mpazpro.ui.theme.MpazProTheme
import cl.duoc.mpazpro.viewmodel.MainViewModel
import cl.duoc.mpazpro.viewmodel.ViewModelFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtenemos la base de datos y los DAOs para pasárselos al MainViewModel
        val database = AppDatabase.getDatabase(this)

        val mainViewModel: MainViewModel by viewModels {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MainViewModel(database.docenteDao(), database.estudianteDao()) as T
                }
            }
        }

        setContent {
            MpazProTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val factory = ViewModelFactory(database.desafioDao())

                    AppNavigation(
                        navController = navController,
                        mainViewModel = mainViewModel,
                        factory = factory
                    )
                }
            }
        }
    }
}