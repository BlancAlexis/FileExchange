package fr.ablanc.fileexchangeandroid.domain

import android.graphics.Bitmap
import fr.ablanc.fileexchangeandroid.data.SocketDataSource
import fr.ablanc.fileexchangeandroid.domain.util.Ressource
import io.ktor.websocket.Frame
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retry

interface SocketRepository {
    suspend fun connect(): Flow<Ressource<Unit>>
    suspend fun disconnect(): Result<Unit>
    suspend fun listen(): Flow<Frame>
    suspend fun send(data: ByteArray): Ressource<Unit>
}

class SocketRepositoryImpl(
    private val socketDataSource: SocketDataSource, private val cryptoManager: CryptoManager
) : SocketRepository {
    override suspend fun connect(): Flow<Ressource<Unit>> = flow {
        try {
            emit(Ressource.Loading())
            socketDataSource.connect(cryptoManager.key.encoded)
            emit(Ressource.Success(Unit))
        } catch (e: Exception) {
            emit(Ressource.Error(exception = e, message = e.message))
        }
    }.retry(
        retries = 3, predicate = { (it is Exception).also { if (it) delay(5000) } }
    )

    override suspend fun disconnect(): Result<Unit> {
        return try {
            socketDataSource.disconnect()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure<Unit>(exception = e)
        }
    }

    override suspend fun listen(): Flow<Frame> {
        return socketDataSource.listen()
    }


    override suspend fun send(data: ByteArray): Ressource<Unit> {
        return try {
            socketDataSource.send(data)
            Ressource.Success(Unit)
        } catch (e: Exception) {
            Ressource.Error(exception = e, message = e.message)
        }
    }
}

sealed class FrameContent {
    data class Image(val value: Bitmap) : FrameContent()
    data class PDF(val value: ByteArray) : FrameContent()
}