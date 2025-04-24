package fr.ablanc.fileexchangeandroid.presentation

import android.net.Uri
import android.util.Log.e
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

class BaseViewModel(
    private val repository: SocketRepository,
    private val encryptImageUseCase: EncryptImageUseCase,
    private val decryptImageUseCase: DecryptImageUseCase
) : ViewModel() {

    private val job: Job = socketConnection()

    private val _state = MutableStateFlow(BaseState())
    val state = _state.onStart {}.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), BaseState()
    )

    private fun setServerConnected(state: Boolean) {
        _state.update { it.copy(serverConnected = state) }
    }

    init {
        viewModelScope.launch {
            delay(5000)
            val key = EncryptImageUseCase.generateAESKey()
            socketSend(encryptImageUseCase.invoke(Uri.EMPTY, key))

        }


    }
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
        repository.listen().collect {
           
        }
    }

    private fun socketSend(data : ByteArray)  {
        viewModelScope.launch {
            repository.send(data, "key")
        }

    }

    fun onAction(action: OnScreenAction) {
        when (action) {
            is OnScreenAction.DocumentSelected -> {
                viewModelScope.launch {
                    socketSend(encryptImageUseCase(action.uri, EncryptImageUseCase.generateAESKey()))
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

    var key : String?= ""
    operator fun invoke(): Flow<Ressource<FrameContent>> = flow {
        emit(Ressource.Loading())
        try {
            repository.listen().collect { frame ->
                when (frame) {
                    is Frame.Binary -> {} //emit(Ressource.Success(FrameContent.Binary(decryptImageUseCase.invoke(frame.data, key ?: throw Exception("no key") ))))// fonction savoir type puis decode
                    is Frame.Close -> {}
                    is Frame.Ping -> {}
                    is Frame.Pong -> {}
                    is Frame.Text -> key = frame.readText()
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