package fr.ablanc.fileexchangeandroid.domain

import fr.ablanc.fileexchangeandroid.domain.util.Ressource
import io.ktor.websocket.Frame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class ListenUseCase(
    private val repository: SocketRepository,
    private val decryptImageUseCase: DecryptImageUseCase,
    private val convertIntoFileUseCase: ConvertIntoFileUseCase
) {
    private lateinit var key: SecretKey

    operator fun invoke(): Flow<Ressource<FrameContent>> = flow {
        emit(Ressource.Loading())
        try {
            repository.listen().collect { frame ->
                when (frame) {
                    is Frame.Binary -> {
                        if (!this@ListenUseCase::key.isInitialized) {
                            key = SecretKeySpec(frame.data, "AES")
                        } else {
                            val bitesDecripted = decryptImageUseCase.invoke(
                                frame.data, key ?: throw Exception("no key")
                            )
                            val result = convertIntoFileUseCase.invoke(bitesDecripted)
                            emit(
                                Ressource.Success(result)
                            )
                        }
                    }

                    else -> {}
                }
            }
        } catch (e: Exception) {
            emit(Ressource.Error(message = e.message))
        }
    }
}