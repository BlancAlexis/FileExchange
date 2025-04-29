package fr.ablanc.fileexchangeandroid.presentation

import fr.ablanc.fileexchangeandroid.domain.UIFile

data class BaseState(
    val isServerConnected: Boolean = false,
    val isLoadingResource: Boolean = false,
    val uiFile: UIFile? = null,
    val persistedResourceNames: List<String>? = null,
    val showDocumentDialog: Boolean = false,
    val isViewingCachedFile: Boolean = false,
    val showCachedFilesDialog: Boolean = false,
    val toastMessage: String? = null,
    val isResourceLoaded: Boolean = false,
)