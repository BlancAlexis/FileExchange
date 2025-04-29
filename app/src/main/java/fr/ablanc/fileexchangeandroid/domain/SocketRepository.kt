package fr.ablanc.fileexchangeandroid.domain

import fr.ablanc.fileexchangeandroid.data.SocketDataSource
import fr.ablanc.fileexchangeandroid.domain.util.Resources
import io.ktor.websocket.Frame
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retryWhen

interface SocketRepository {
    fun connect(): Flow<Resources<Unit>>
    suspend fun disconnect(): Result<Unit>
    fun listen(): Flow<Frame>
    suspend fun send(data: ByteArray): Resources<Unit>
}

const val CONNECTION_RETRY = 5

class SocketRepositoryImpl(
    private val socketDataSource: SocketDataSource, private val cryptoManager: CryptoManager
) : SocketRepository {
    override fun connect(): Flow<Resources<Unit>> = flow {
        emit(Resources.Loading())
        socketDataSource.connect(cryptoManager.key.encoded)
        emit(Resources.Success(Unit))
    }.retryWhen { cause, attempt ->
        if (cause is Exception && attempt < CONNECTION_RETRY) {
            emit(Resources.Error(exception = cause, message = cause.message))
            delay(5000)
            true
        } else {
            false
        }
    }


    override suspend fun disconnect(): Result<Unit> {
        return try {
            socketDataSource.disconnect()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure<Unit>(exception = e)
        }
    }

    override fun listen(): Flow<Frame> {
        return socketDataSource.listen()
    }


    override suspend fun send(data: ByteArray): Resources<Unit> {
        return try {
            socketDataSource.sendData(data)
            Resources.Success(Unit)
        } catch (e: Exception) {
            Resources.Error(exception = e, message = e.message)
        }
    }
}

