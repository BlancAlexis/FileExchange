package fr.ablanc.fileexchangeandroid.domain

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class DecryptImageUseCase {
    operator fun invoke(encryptedData: ByteArray, key: SecretKey): ByteArray {
        val iv = encryptedData.sliceArray(0 until 12)
        val cipherText = encryptedData.sliceArray(12 until encryptedData.size)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val gcmSpec = GCMParameterSpec(128, iv)

        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)
        return cipher.doFinal(cipherText)
    }

}

