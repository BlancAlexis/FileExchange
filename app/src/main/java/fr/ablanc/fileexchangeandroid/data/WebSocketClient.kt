package fr.ablanc.fileexchangeandroid.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.pingInterval
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow

class WebSocketClient {

    val ADDRESS_SERVER = "ws://10.0.2.2:8181"

    private var session: WebSocketSession? = null

    val client = HttpClient(CIO) {
        install(WebSockets)
        install(Logging)
    }

    suspend fun connect() = flow {
        try {
            session = client.webSocketSession(ADDRESS_SERVER)
            emit(true)
        } catch (e: Exception) {
            emit(false)
            throw e
        }
    }

    suspend fun disconnect() {
        session?.close()
        client.close()
        session = null
    }

    suspend fun listen() = flow<Frame> {
        session?.incoming?.receiveAsFlow()?.collect {
            emit(it)
        }
    }

    suspend fun send(message: String) {
        try {
            session?.outgoing?.send(Frame.Text(message))
        } catch (e: Exception) { throw e }

    }

}