package fr.ablanc.fileexchangeandroid.data

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow

interface SocketDataSource {
    suspend fun connect()
    suspend fun disconnect()
    suspend fun listen(): Flow<Frame>
    suspend fun send(message: String)
}

class SocketDataSourceImpl(
    private val client: HttpClient,
    private val serverAddress: String
) : SocketDataSource {

    private var session: WebSocketSession? = null

    override suspend fun connect() {
        try {
            session = client.webSocketSession(serverAddress)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun disconnect() {
        session?.close()
        client.close()
        session = null
    }

    override suspend fun listen() = flow<Frame> {
        session?.incoming?.receiveAsFlow()?.collect {
            emit(it)
        }
    }

    override suspend fun send(message: String) {
        try {
            session?.outgoing?.send(Frame.Text(message))
        } catch (e: Exception) {
            throw e
        }

    }

}