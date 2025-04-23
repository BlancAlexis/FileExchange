package fr.ablanc.fileexchangeandroid.data.di

import fr.ablanc.fileexchangeandroid.data.WebSocketClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import org.koin.dsl.module

fun injectFeatures() = loadFeatures


private val loadFeatures by lazy {
    listOf(
        appModule
    )
}

val appModule = module {
    single {
        HttpClient(CIO) {
            install(Logging)
            install(WebSockets)
        }
    }
    single { WebSocketClient(get()) }
}

