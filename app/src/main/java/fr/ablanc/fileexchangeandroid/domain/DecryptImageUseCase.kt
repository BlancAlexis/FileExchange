package fr.ablanc.fileexchangeandroid.domain

import java.io.ByteArrayInputStream
import java.net.URLConnection
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class DecryptImageUseCase {
        operator fun invoke(encryptedData: ByteArray, key: String): ByteArray {
            val iv = encryptedData.sliceArray(0 until 12)
            val cipherText = encryptedData.sliceArray(12 until encryptedData.size)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val gcmSpec = GCMParameterSpec(128, iv)

            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key.toByteArray(), "AES"), gcmSpec)
            return cipher.doFinal(cipherText)
        }

    }

class ConvertIntoFileUseCase() {

    operator fun invoke(byteArray: ByteArray, fileName: String) {
        when(val name = Type.valueOf(detectMimeType(byteArray) ?: "")){
            Type.JPG -> TODO()//FrameContent.Image
            Type.PNG -> TODO()
            Type.PDF -> TODO()
            else -> {}
        }

    }


    fun detectMimeType(data: ByteArray): String? {
        return ByteArrayInputStream(data).use { stream ->
            URLConnection.guessContentTypeFromStream(stream)
        }
    }


}

enum class Type(val type: String) {
    JPG("image/jpeg"), PNG("image/png"), PDF("application/pdf")
}

