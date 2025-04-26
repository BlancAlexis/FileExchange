package fr.ablanc.fileexchangeandroid.domain.util

sealed class Resources<T>(
    val data: T? = null, val error: Exception? = null, val message: String? = null
) {
    class Success<T>(data: T?) : Resources<T>(data)
    class Loading<T> : Resources<T>()
    class Error<T>(exception: Exception? = null, message: String? = null) :
        Resources<T>(error = exception, message = message)
}