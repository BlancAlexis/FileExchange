package fr.ablanc.fileexchangeandroid.domain

import fr.ablanc.fileexchangeandroid.data.SocketDataSource
import fr.ablanc.fileexchangeandroid.domain.util.Ressource
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retry

interface SocketRepository {
    suspend fun connect(): Flow<Ressource<Unit>>
    suspend fun disconnect(): Result<Unit>
    suspend fun listen(): Flow<Ressource<String>> // TODO voir pour un wrapper
    suspend fun send(message: String) : Ressource<Unit>
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

    override suspend fun listen(): Flow<Ressource<String>> = flow {
        socketDataSource.listen().catch { emit(Ressource.Error(it as Exception?)) }
            .collect { value ->
                if (value is Frame.Text) {
                    emit(Ressource.Success(value.readText()))
                } else {
                    emit(Ressource.Error(message = "Unknown"))
                }
            }
    }

    override suspend fun send(message: String): Ressource<Unit> {
        return try {
            socketDataSource.send(message)
            Ressource.Success(Unit)
        } catch (e: Exception) {
            Ressource.Error(exception = e, message = e.message)
        }
    }
}