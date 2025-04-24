package fr.ablanc.fileexchangeandroid.domain

import android.net.Uri
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class EncryptImageUseCase(
    private val fileReader: FileReader
) {

    operator fun invoke(uri: Uri, key: SecretKey): ByteArray {
        //val simageBytes = fileReader.readBytes(uri)
        val imageBytes = "eee".encodeToByteArray()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)

        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(imageBytes)

        return iv + encryptedBytes
    }

    companion object {
        fun generateAESKey(): SecretKey {
            val keyGen = KeyGenerator.getInstance("AES")
            keyGen.init(256)
            return keyGen.generateKey()
        }
    }
}
