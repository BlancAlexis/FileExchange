package fr.ablanc.fileexchangeandroid.domain

import fr.ablanc.fileexchangeandroid.domain.util.Resources
import io.ktor.websocket.Frame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class ListenDataUseCase(
    private val repository: SocketRepository,
    private val decryptResourceUseCase: DecryptResourceUseCase,
    private val convertIntoFileUseCase: ConvertIntoFileUseCase
) {
    private lateinit var key: SecretKey

    operator fun invoke(): Flow<Resources<FrameContent>> = flow {
        try {
            repository.listen().collect { frame ->
                when (frame) {
                    is Frame.Binary -> {
                        val prefix = String(frame.data.sliceArray(0 until 4))
                        when (prefix) {
                            "KEY:" -> {
                                key = SecretKeySpec(
                                    frame.data.sliceArray(4 until frame.data.size), "AES"
                                )
                            }

                            "DATA" -> {
                                emit(Resources.Loading())
                                val bitesDecrypted = decryptResourceUseCase.invoke(
                                    frame.data.sliceArray(4 until frame.data.size), key
                                )
                                val result = convertIntoFileUseCase.invoke(bitesDecrypted)
                                emit(Resources.Success(result))
                            }

                            else -> {
                                emit(Resources.Error(message = "Unknown prefix"))
                            }
                        }
                    }

                    else -> {
                        emit(Resources.Error(message = "Unknown Frame"))
                    }
                }
            }
        } catch (e: Exception) {
            emit(Resources.Error(message = e.message))
        }
    }
}