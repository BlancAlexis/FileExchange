package fr.ablanc.fileexchangeandroid.domain

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.crypto.Cipher

class EncryptUseCase(
    private val fileReader: FileReader,
    private val cryptoManager: CryptoManager
) {

    suspend operator fun invoke(uri: Uri): ByteArray = withContext(Dispatchers.Default) {
        val fileBytes = withContext(Dispatchers.IO) {
            fileReader.readBytes(uri)
        }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, cryptoManager.key)

        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(fileBytes)

        return@withContext iv + encryptedBytes
    }
}