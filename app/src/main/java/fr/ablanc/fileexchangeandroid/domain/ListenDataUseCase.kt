package fr.ablanc.fileexchangeandroid.domain

import fr.ablanc.fileexchangeandroid.domain.util.Resources
import fr.ablanc.fileexchangeandroid.domain.util.Resources.Error
import fr.ablanc.fileexchangeandroid.domain.util.Resources.Loading
import fr.ablanc.fileexchangeandroid.domain.util.Resources.Success
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
                        val prefix = WebDataType.fromType(String(frame.data.sliceArray(0 until 4)))
                        when (prefix) {
                            WebDataType.KEY -> {
                                key = SecretKeySpec(
                                    frame.data.sliceArray(4 until frame.data.size), "AES"
                                )
                            }

                            WebDataType.DATA -> {
                                emit(Loading())
                                val bitesDecrypted = decryptResourceUseCase.invoke(
                                    frame.data.sliceArray(4 until frame.data.size), key
                                )
                                val result = convertIntoFileUseCase.invoke(bitesDecrypted)
                                emit(Success(result))
                            }

                            null -> {
                                emit(Error(message = "Unknown prefix"))
                            }
                        }
                    }

                    else -> {
                        emit(Error(message = "Unknown Frame"))
                    }
                }
            }
        } catch (e: Exception) {
            emit(Error(message = e.message))
        }
    }
}