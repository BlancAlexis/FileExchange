package fr.ablanc.fileexchangeandroid.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.ablanc.fileexchangeandroid.domain.EncryptImageUseCase
import fr.ablanc.fileexchangeandroid.domain.FrameContent
import fr.ablanc.fileexchangeandroid.domain.ListenUseCase
import fr.ablanc.fileexchangeandroid.domain.SocketRepository
import fr.ablanc.fileexchangeandroid.domain.util.Ressource
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BaseViewModel(
    private val repository: SocketRepository,
    private val encryptImageUseCase: EncryptImageUseCase,
    private val listenUseCase: ListenUseCase,
) : ViewModel() {

    private val job: Job = socketConnection()

    private val _state = MutableStateFlow(BaseState())
    val state = _state.onStart {}.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), BaseState()
    )

    private fun setServerConnected(state: Boolean) {
        _state.update { it.copy(serverConnected = state) }
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

    private fun socketSend(data: ByteArray) {
        viewModelScope.launch {
            repository.send(data)
        }

    }

    fun onAction(action: OnScreenAction) {
        when (action) {
            is OnScreenAction.DocumentSelected -> {
                viewModelScope.launch {
                    socketSend(
                        encryptImageUseCase(action.uri)
                    )
                }
            }

        }
    }
}

sealed interface OnScreenAction {
    data class DocumentSelected(val uri: Uri) : OnScreenAction
}