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
import cl.duoc.mpazpro.navigation.AppNavigation
import cl.duoc.mpazpro.ui.theme.MpazProTheme
import cl.duoc.mpazpro.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MpazProTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    AppNavigation(
                        navController = navController,
                        mainViewModel = mainViewModel
                    )
                }
            }
        }
    }
}