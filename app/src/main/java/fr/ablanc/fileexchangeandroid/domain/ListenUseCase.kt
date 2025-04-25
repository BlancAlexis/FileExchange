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
        try {
            repository.listen().collect { frame ->
                when (frame) {
                    is Frame.Binary -> {
                        val prefix = String(frame.data.sliceArray(0 until 4))
                        when(prefix){
                            "KEY:" -> {
                                key = SecretKeySpec(frame.data.sliceArray(4 until frame.data.size), "AES")
                            }
                            "DATA" -> {
                                emit(Ressource.Loading())
                                val bitesDecrypted = decryptImageUseCase.invoke(frame.data.sliceArray(4 until frame.data.size), key)
                                val result = convertIntoFileUseCase.invoke(bitesDecrypted)
                                emit(Ressource.Success(result))
                            }
                            else -> {

                            }
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