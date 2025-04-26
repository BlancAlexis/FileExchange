package fr.ablanc.fileexchangeandroid.data

import fr.ablanc.fileexchangeandroid.domain.CryptoManager
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow

interface SocketDataSource {
    suspend fun connect(encoded: ByteArray)
    suspend fun disconnect()
    fun listen(): Flow<Frame>
    suspend fun send(data: ByteArray)
}

class SocketDataSourceImpl(
    private val client: HttpClient,
    private val serverAddress: String,
    private val cryptoManager: CryptoManager
) : SocketDataSource {

    private var session: WebSocketSession? = null

    override suspend fun connect(key: ByteArray) {
        try {
            session = client.webSocketSession(serverAddress)
            session?.outgoing?.send(Frame.Binary(true, key))
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun disconnect() {
        session?.close()
        client.close()
        session = null
    }

    override fun listen() = flow<Frame> {
        session?.incoming?.receiveAsFlow()?.collect {
            emit(it)
        }
    }

    suspend fun sendKey(): ChannelResult<Unit>? {
        return session?.outgoing?.trySend(
            Frame.Binary(
                true, withPrefix("KEY:", cryptoManager.key.encoded)
            )
        )
    }

    override suspend fun send(dataPayload: ByteArray) {
        sendKey()?.onSuccess {
            session?.outgoing?.trySend(
                Frame.Binary(true, withPrefix("DATA", dataPayload))
            )?.onSuccess {
                println("Frame sent")
            }?.onFailure {
                println(it?.message)
                println("Fail sent")
            }
        }?.onFailure {
            println(it?.message)
            println("Fail sent")
        }
    }

    private fun withPrefix(prefix: String, data: ByteArray): ByteArray {
        return prefix.toByteArray(Charsets.UTF_8) + data
    }

}