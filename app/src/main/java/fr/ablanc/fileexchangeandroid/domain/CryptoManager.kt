package fr.ablanc.fileexchangeandroid.domain

import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class CryptoManager {
    val key: SecretKey by lazy {
        generateAESKey()
    }

    private fun generateAESKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        return keyGen.generateKey()
    }
}