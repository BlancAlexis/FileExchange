package fr.ablanc.fileexchangeandroid.presentation

import android.util.Log.d
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.ablanc.fileexchangeandroid.domain.SocketRepository
import fr.ablanc.fileexchangeandroid.domain.util.Ressource
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BaseViewModel(
    private val repository: SocketRepository
) : ViewModel() {

    private val job : Job = socketConnection()

    private val _state = MutableStateFlow(BaseState())
    val state = _state.onStart {
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), BaseState()
    )

    private fun setServerConnected(state: Boolean){
        _state.update { it.copy(serverConnected = state) }
    }

    private suspend fun socketConnect(){
        if (this.job.isActive == true) {
            socketDisconnection()
        } else {
            job.start()
        }
    }

    private fun socketConnection() : Job {
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
    private suspend fun socketDisconnection(){
        repository.disconnect()
            .onSuccess {
                job.cancel()
            }
            .onFailure {

            }
    }
    private suspend fun socketListen(){
        repository.listen().collect{
            when(it){
                is Ressource.Error<*> -> println() // Gestion erreur
                is Ressource.Loading<*> -> println()
                is Ressource.Success<*> -> println(it.data)
            }
        }
    }
    private fun socketSend(){

    }

}