package fr.ablanc.fileexchangeandroid.domain

import android.content.Context
import java.io.File

class CacheManager(
    private val context: Context,
    private val convertIntoFileUseCase: ConvertIntoFileUseCase
) {

    fun createCachedFile(uiFile: UIFile?) {
        if (uiFile == null) return
        val fileName = "cached_${uiFile.type.name}_${System.currentTimeMillis()}"
        val tempFile = File(context.cacheDir, fileName)
        tempFile.writeBytes(uiFile.bytes)
    }

    fun getCachedFiles(): List<UIFile> {
        return context.cacheDir.listFiles()?.map { file ->
            val bytes = file.readBytes()
            val type = Type.valueOf(file.name.split("_")[1])
            val content = convertIntoFileUseCase(bytes)
            UIFile(type, bytes, content)
        } ?: emptyList()
    }

    fun clearCache() {
        context.cacheDir.listFiles()?.forEach { it.delete() }
    }
}

data class UIFile(
    val type: Type,
    val bytes: ByteArray,
    val content: FrameContent
)