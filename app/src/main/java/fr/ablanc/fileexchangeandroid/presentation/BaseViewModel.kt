package fr.ablanc.fileexchangeandroid.presentation

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.ablanc.fileexchangeandroid.domain.CacheManager
import fr.ablanc.fileexchangeandroid.domain.EncryptUseCase
import fr.ablanc.fileexchangeandroid.domain.FrameContent
import fr.ablanc.fileexchangeandroid.domain.ListenDataUseCase
import fr.ablanc.fileexchangeandroid.domain.SocketRepository
import fr.ablanc.fileexchangeandroid.domain.Type
import fr.ablanc.fileexchangeandroid.domain.UIFile
import fr.ablanc.fileexchangeandroid.domain.util.Resources
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class BaseViewModel(
    private val socketRepository: SocketRepository,
    private val encryptUseCase: EncryptUseCase,
    private val listenDataUseCase: ListenDataUseCase,
    private val cacheManager: CacheManager
) : ViewModel() {

    private var socketJob: Job? = null

    private val _state = MutableStateFlow(BaseState())
    val state = _state.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), BaseState()
    )


    internal fun socketConnect() {
        socketJob?.cancel()
        socketJob = viewModelScope.launch {
            socketRepository.connect().collect { resource ->
                when (resource) {
                    is Resources.Error<*> -> setServerConnected(false)
                    is Resources.Loading<*> -> println("co : loading")
                    is Resources.Success<*> -> {
                        setServerConnected(true)
                        socketListen()
                    }
                }
            }
        }
    }

    internal fun socketDisconnection() {
        socketJob?.cancel()
        socketJob = null
        setServerConnected(false)
    }

    private suspend fun socketListen() {
        listenDataUseCase().collect {
            when (it) {
                is Resources.Error<*> -> {
                    println("listen : error emit ${it.message}")
                }

                is Resources.Loading<*> -> _state.update { state ->
                    state.copy(
                        isLoadingResource = true
                    )
                }

                is Resources.Success<*> -> {
                    println("listen : success emit")
                    when (it.data) {
                        is FrameContent.Image -> _state.update { state ->
                            state.copy(
                                uiFile = UIFile(
                                    type = Type.PNG,
                                    bytes = it.data.value.toByteArray(),
                                    content = it.data
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    internal fun clearCache() {
        cacheManager.clearCache()
    }

    private fun socketSend(data: ByteArray) {
        viewModelScope.launch {
            socketRepository.send(data)
        }

    }

    private fun setServerConnected(state: Boolean) {
        _state.update { it.copy(isServerConnected = state) }
    }

    fun onAction(action: OnScreenAction) {
        when (action) {
            is OnScreenAction.OnDocumentSelected -> {
                _state.update { it.copy(isLoadingResource = false) }
                viewModelScope.launch {
                    socketSend(
                        encryptUseCase(action.uri)
                    )
                }

            }

            OnScreenAction.OnCloseDocumentLoading -> _state.update {
                it.copy(
                    isLoadingResource = false, showDocumentDialog = true
                )
            }

            OnScreenAction.OnTriggerSaveDocumentButton -> viewModelScope.launch(Dispatchers.IO) {
                if (_state.value.uiFile != null) {
                    cacheManager.createCachedFile(_state.value.uiFile)

                }
            }

            OnScreenAction.OnTriggerViewSaveButton -> {
                viewModelScope.launch(Dispatchers.IO) {
                    val persistedFiles =
                        cacheManager.getCachedFiles().map { it.name ?: "no name: ${it.type}" }
                    withContext(Dispatchers.Main) {
                        _state.update {
                            it.copy(
                                persistedResourceNames = persistedFiles
                            )

                        }
                    }

                }

            }

            OnScreenAction.OnTriggerDeleteDocumentButton -> viewModelScope.launch(Dispatchers.IO) {
                if (_state.value.uiFile != null) {
                    cacheManager.deleteCachedFile(_state.value.uiFile)
                }
            }

            OnScreenAction.OnCloseDocumentVisualization -> _state.update {
                it.copy(
                    uiFile = null, showDocumentDialog = false
                )
            }

            is OnScreenAction.OnCachedFileClicked -> viewModelScope.launch(Dispatchers.IO) {
                val cachedFile =
                    cacheManager.getCachedFiles().firstOrNull { it.name == action.name }
                if (cachedFile != null) {
                    withContext(Dispatchers.Main) {
                        _state.update {
                            it.copy(
                                uiFile = cachedFile,
                                showDocumentDialog = true,
                                isViewingCachedFile = true
                            )
                        }
                    }
                }
            }
        }
    }

}

fun Bitmap.toByteArray(): ByteArray {
    val stream = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}

sealed interface OnScreenAction {
    data class OnDocumentSelected(val uri: Uri) : OnScreenAction
    object OnCloseDocumentLoading : OnScreenAction
    object OnCloseDocumentVisualization : OnScreenAction
    object OnTriggerSaveDocumentButton : OnScreenAction
    object OnTriggerViewSaveButton : OnScreenAction
    object OnTriggerDeleteDocumentButton : OnScreenAction
    data class OnCachedFileClicked(val name: String) : OnScreenAction
}