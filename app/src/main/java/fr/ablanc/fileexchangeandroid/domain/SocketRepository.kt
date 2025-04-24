package fr.ablanc.fileexchangeandroid.domain

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
    suspend fun listen(): Flow<Frame>  // TODO voir pour un wrapper
    suspend fun send(data: ByteArray, key: String) : Ressource<Unit>
}

class SocketRepositoryImpl(
    private val socketDataSource: SocketDataSource
) : SocketRepository {
    override suspend fun connect(): Flow<Ressource<Unit>> = flow {
        try {
            emit(Ressource.Loading())
            socketDataSource.connect()
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


    override suspend fun send(data: ByteArray, key: String): Ressource<Unit> {
        return try {
            socketDataSource.send(data,key)
            Ressource.Success(Unit)
        } catch (e: Exception) {
            Ressource.Error(exception = e, message = e.message)
        }
    }
}
sealed class FrameContent {
    data class Image(val value: String) : FrameContent()
    data class PDF(val value: ByteArray) : FrameContent()
}