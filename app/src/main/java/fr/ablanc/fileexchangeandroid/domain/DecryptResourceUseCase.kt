package fr.ablanc.fileexchangeandroid.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class DecryptResourceUseCase {
    suspend operator fun invoke(encryptedData: ByteArray, key: SecretKey): ByteArray =
        withContext(Dispatchers.Default) {
            val iv = encryptedData.sliceArray(0 until 12)
            val cipherText = encryptedData.sliceArray(12 until encryptedData.size)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val gcmSpec = GCMParameterSpec(128, iv)

            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)
            return@withContext cipher.doFinal(cipherText)
        }

}

