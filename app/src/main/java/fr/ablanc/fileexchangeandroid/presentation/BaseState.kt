package fr.ablanc.fileexchangeandroid.presentation

import fr.ablanc.fileexchangeandroid.domain.UIFile

data class BaseState(
    val isServerConnected: Boolean = false,
    val isLoadingResource: Boolean = false,
    val uiFile: UIFile? = null,
    val persistedResourceNames: List<String>? = null
)