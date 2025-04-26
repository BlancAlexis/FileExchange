package fr.ablanc.fileexchangeandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.ablanc.fileexchangeandroid.presentation.component.HomeScreenRoot
import fr.ablanc.fileexchangeandroid.presentation.theme.FileExchangeAndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FileExchangeAndroidTheme {
                enableEdgeToEdge()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize(),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        NavHost(
                            navController = rememberNavController(), startDestination = "home"
                        ) {
                            composable("home") {
                                HomeScreenRoot()
                            }
                        }
                    }
                }
            }
        }
    }
}



