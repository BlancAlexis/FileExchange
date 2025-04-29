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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        socketJob = viewModelScope.launch(Dispatchers.IO) {
            socketRepository.connect().collect { resource ->
                when (resource) {
                    is Resources.Error<*> -> {
                        setServerConnected(false)
                        setToastMessage("Erreur lors de la connexion au serveur ${resource.message}")
                     }

                    is Resources.Loading<*> -> {}
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
                }

                is Resources.Loading<*> -> _state.update { state ->
                    state.copy(
                        isLoadingResource = true
                    )
                }

                is Resources.Success<*> -> {
                    when (it.data) {
                        is FrameContent.Image -> _state.update { state ->
                            state.copy(
                                isResourceLoaded = true,
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

    private suspend fun socketSend(data: ByteArray) {
        socketRepository.send(data)

    }

    private fun setServerConnected(state: Boolean) {
        _state.update { it.copy(isServerConnected = state) }
    }

    private fun setToastMessage(message: String?) {
        _state.update { it.copy(toastMessage = message) }
    }

    fun onAction(action: OnScreenAction) {
        when (action) {
            is OnScreenAction.OnDocumentSelected -> {
                _state.update { it.copy(isLoadingResource = false) }
                viewModelScope.launch(Dispatchers.IO) {
                    socketSend(
                        encryptUseCase(action.uri)
                    )
                }

            }

            OnScreenAction.OnCloseDocumentLoading -> _state.update {
                it.copy(
                    isLoadingResource = false, showDocumentDialog = true, isResourceLoaded = false
                )
            }

            OnScreenAction.OnTriggerSaveDocumentButton -> {
                val fileToSave = _state.value.uiFile
                viewModelScope.launch(Dispatchers.IO) {
                    if (fileToSave != null) {
                        cacheManager.createCachedFile(fileToSave).onSuccess {
                            setToastMessage("Document sauvegardé")
                        }.onFailure {
                            setToastMessage("Erreur lors de la sauvegarde du document ${it.message}")
                        }

                    }
                }
            }

            OnScreenAction.OnTriggerViewSaveButton -> {
                viewModelScope.launch(Dispatchers.IO) {
                    cacheManager.getCachedFiles().onSuccess {
                        val persistedFiles = it.map { it.name ?: "no name: ${it.type}" }
                        withContext(Dispatchers.Main) {
                            _state.update {
                                it.copy(
                                    showCachedFilesDialog = true,
                                    persistedResourceNames = persistedFiles
                                )

                            }
                        }
                    }.onFailure {
                        setToastMessage("Erreur lors de la récupération des documents ${it.message}")
                    }


                }

            }

            OnScreenAction.OnTriggerDeleteDocumentButton -> viewModelScope.launch(Dispatchers.IO) {
                val fileToDelete = _state.value.uiFile
                if (fileToDelete != null) {
                    cacheManager.deleteCachedFile(fileToDelete).onSuccess {
                        setToastMessage("Document supprimé")
                    }.onFailure {
                        setToastMessage("Erreur lors de la suppression du document")
                    }
                }
            }

            OnScreenAction.OnCloseDocumentVisualization -> _state.update {
                it.copy(
                    uiFile = null,
                    showDocumentDialog = false,
                    persistedResourceNames = emptyList(),
                    showCachedFilesDialog = false
                )
            }

            is OnScreenAction.OnCachedFileClicked -> viewModelScope.launch(Dispatchers.IO) {
                cacheManager.getCachedFiles().onSuccess {
                    if (it.isNotEmpty()) {
                        val cachedFile = it.firstOrNull { it.name == action.name }
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
                }.onFailure {
                    setToastMessage("Erreur lors de la récupération des documents ${it.message}")
                }
            }

            OnScreenAction.OnCloseCachedFilesDialog -> _state.update {
                it.copy(
                    persistedResourceNames = emptyList(), showCachedFilesDialog = false
                )

            }

            OnScreenAction.ClearToastMessage -> setToastMessage(null)
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
    object ClearToastMessage : OnScreenAction
    object OnTriggerDeleteDocumentButton : OnScreenAction
    object OnCloseCachedFilesDialog : OnScreenAction
    data class OnCachedFileClicked(val name: String) : OnScreenAction
}