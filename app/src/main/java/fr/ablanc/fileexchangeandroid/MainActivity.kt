package fr.ablanc.fileexchangeandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key.Companion.W
import androidx.compose.ui.tooling.preview.Preview
import fr.ablanc.fileexchangeandroid.data.WebSocketClient
import fr.ablanc.fileexchangeandroid.presentation.theme.FileExchangeAndroidTheme
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlobalScope.launch {
            val webSocketClient = WebSocketClient(
                HttpClient(CIO) {
                    install(WebSockets)
                }
            )
            webSocketClient.connect().collect {
                println("co $it")
            }
            webSocketClient.listen().collect {
                println("listen $it")
            }
            webSocketClient.send("Hello")
            webSocketClient.disconnect()
        }
        setContent {
            FileExchangeAndroidTheme {
                Greeting(name = "Android")
            }
            enableEdgeToEdge()
            setContent {
                FileExchangeAndroidTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Greeting(
                            name = "Android",
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FileExchangeAndroidTheme {
        Greeting("Android")
    }
}