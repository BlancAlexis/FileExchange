package fr.ablanc.fileexchangeandroid.domain.util

sealed class Ressource<T>(
    val data: T? = null, val error: Exception? = null, val message: String? = null
) {
    class Success<T>(data: T?) : Ressource<T>(data)
    class Loading<T> : Ressource<T>()
    class Error<T>(exception: Exception? = null, message: String? = null) :
        Ressource<T>(error = exception, message = message)
}