package fr.ablanc.fileexchangeandroid.presentation

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.ablanc.fileexchangeandroid.domain.DecryptImageUseCase
import fr.ablanc.fileexchangeandroid.domain.EncryptImageUseCase
import fr.ablanc.fileexchangeandroid.domain.FrameContent
import fr.ablanc.fileexchangeandroid.domain.SocketRepository
import fr.ablanc.fileexchangeandroid.domain.util.Ressource
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class BaseViewModel(
    private val repository: SocketRepository,
    private val encryptImageUseCase: EncryptImageUseCase,
    private val decryptImageUseCase: DecryptImageUseCase,
    private val listenUseCase: ListenUseCase
) : ViewModel() {

    private val job: Job = socketConnection()

    private val _state = MutableStateFlow(BaseState())
    val state = _state.onStart {}.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), BaseState()
    )

    private fun setServerConnected(state: Boolean) {
        _state.update { it.copy(serverConnected = state) }
    }

 /*   init {
        viewModelScope.launch {
            delay(5000)
            val key = EncryptImageUseCase.generateAESKey()
            socketSend(encryptImageUseCase.invoke(Uri.EMPTY, key), key)

        }
 }
*/

    private suspend fun socketConnect() {
        if (this.job.isActive == true) {
            socketDisconnection()
        } else {
            job.start()
        }
    }

    private fun socketConnection(): Job {
        return viewModelScope.launch {
            repository.connect().collect {
                when (it) {
                    is Ressource.Error<*> -> setServerConnected(false)
                    is Ressource.Loading<*> -> println("co : loading")
                    is Ressource.Success<*> -> {
                        println("co : success")
                        setServerConnected(true)
                        socketListen()

                    }
                }
            }
        }

    }

    private suspend fun socketDisconnection() {
        repository.disconnect().onSuccess {
                job.cancel()
            }.onFailure {

            }
    }

    private suspend fun socketListen() {
        listenUseCase().collect {
            when (it) {
                is Ressource.Error<*> -> {
                    println("listen : error emit ${it.message}")
                }
                is Ressource.Loading<*> -> {}
                is Ressource.Success<*> -> {
                    println("listen : success emit")
                    when (it.data) {
                        is FrameContent.Image -> _state.update { state ->
                            state.copy(
                                image = it.data.value
                            )
                        }
                    }
                }
            }
        }
    }

    private fun socketSend(data: ByteArray, key1: SecretKey)  {
        viewModelScope.launch {
            repository.send(data, key1)
        }

    }

    fun onAction(action: OnScreenAction) {
        when (action) {
            is OnScreenAction.DocumentSelected -> {
                viewModelScope.launch {
                    val key = EncryptImageUseCase.generateAESKey()
                    socketSend(
                        encryptImageUseCase(action.uri, key), key
                    )
                }
                println("bien recu")
                viewModelScope.launch {
                    println("av encrypt")
                    val key = EncryptImageUseCase.generateAESKey()
                    val a = encryptImageUseCase.invoke(
                        uri = action.uri, key = key
                    )
                    println("ap encrypt")
                    println("av decrypt")
                    println("ap decrypt")
                }
            }

        }
    }
}
class ListenUseCase(
    private val repository: SocketRepository,
    private val decryptImageUseCase: DecryptImageUseCase
) {

    lateinit var key : SecretKey
    operator fun invoke(): Flow<Ressource<FrameContent>> = flow {
        emit(Ressource.Loading())
        try {
            repository.listen().collect { frame ->
println("listen usecase")
                when (frame) {
                    is Frame.Binary -> {
                        if(!this@ListenUseCase::key.isInitialized){
                           key = SecretKeySpec(frame.data, "AES")
                        }
                        else{
                            val bitesDecripted =  decryptImageUseCase.invoke(frame.data, key ?: throw Exception("no key") )
                            val byteArray = BitmapFactory.decodeByteArray(bitesDecripted, 0, bitesDecripted.size)
                            emit(Ressource.Success(FrameContent.Image(BitmapFactory.decodeByteArray(bitesDecripted, 0, bitesDecripted.size)))) }// fonction savoir type puis decode
                        }

                    is Frame.Close -> {}
                    is Frame.Ping -> {}
                    is Frame.Pong -> {}
                    is Frame.Text -> {}
                }
            }
        } catch (e: Exception) {
            emit(Ressource.Error(message = e.message))
        }
    }
}
sealed interface OnScreenAction {
    data class DocumentSelected(val uri: Uri) : OnScreenAction
}

class convert
