package fr.ablanc.fileexchangeandroid.domain

import android.graphics.Bitmap
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

class SocketRepositoryImpl(
    private val socketDataSource: SocketDataSource, private val cryptoManager: CryptoManager
) : SocketRepository {
    override fun connect(): Flow<Resources<Unit>> = flow {
        emit(Resources.Loading())
        socketDataSource.connect(cryptoManager.key.encoded)
        emit(Resources.Success(Unit))
    }.retryWhen { cause, attempt ->
        if (cause is Exception && attempt < 1000) {
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
            socketDataSource.send(data)
            Resources.Success(Unit)
        } catch (e: Exception) {
            Resources.Error(exception = e, message = e.message)
        }
    }
}

sealed class FrameContent {
    data class Image(val value: Bitmap) : FrameContent()
    data class PDF(val value: ByteArray) : FrameContent()
}