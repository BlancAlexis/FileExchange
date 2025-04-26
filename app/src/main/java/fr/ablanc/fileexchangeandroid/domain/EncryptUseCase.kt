package fr.ablanc.fileexchangeandroid.domain

import android.net.Uri
import javax.crypto.Cipher

class EncryptUseCase(
    private val fileReader: FileReader,
    private val cryptoManager: CryptoManager
) {

    operator fun invoke(uri: Uri): ByteArray {
        val imageBytes = fileReader.readBytes(uri)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, cryptoManager.key)

        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(imageBytes)

        return iv + encryptedBytes
    }

}
