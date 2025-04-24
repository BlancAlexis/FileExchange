package fr.ablanc.fileexchangeandroid.domain

import android.content.Context
import android.net.Uri
import kotlinx.io.IOException

class FileReader(private val context: Context) {
    fun readBytes(uri: Uri): ByteArray {
        return context.contentResolver.openInputStream(uri)?.readBytes()
            ?: throw IOException("Impossible de lire le fichier")
    }
}