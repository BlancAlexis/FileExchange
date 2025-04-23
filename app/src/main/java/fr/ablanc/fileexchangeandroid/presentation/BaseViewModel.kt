package fr.ablanc.fileexchangeandroid.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.ablanc.fileexchangeandroid.domain.SocketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class BaseViewModel(
    private val repository: SocketRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BaseState())
    val state = _state.onStart {

    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), BaseState()
    )
}