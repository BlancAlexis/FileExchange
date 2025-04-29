package fr.ablanc.fileexchangeandroid.domain

import android.content.Context
import java.io.File

class CacheManager(
    private val context: Context,
    private val convertIntoFileUseCase: ConvertIntoFileUseCase
) {

    internal fun createCachedFile(uiFile: UIFile?) {
        if (uiFile == null) return
        val fileName = "cached_${uiFile.type.name}_${System.currentTimeMillis()}"
        val tempFile = File(context.cacheDir, fileName)
        tempFile.writeBytes(uiFile.bytes)
    }

    internal fun getCachedFiles(): List<UIFile> {
        return context.cacheDir.listFiles()?.map { file ->
            val bytes = file.readBytes()
            val type = Type.valueOf(file.name.split("_")[1])
            val content = convertIntoFileUseCase(bytes)
            UIFile(file.name,type, bytes, content)
        } ?: emptyList()
    }

    internal fun clearCache() {
        context.cacheDir.listFiles()?.forEach { it.delete() }
    }
    internal fun deleteCachedFile(UiFile: UIFile?) : Boolean {
        if (UiFile == null) return false
        val file = File(context.cacheDir, UiFile.name)
        if (file.exists()) {
            return file.delete()
        }
        return false
    }
}

data class UIFile(
    val name : String? = "",
    val type: Type,
    val bytes: ByteArray,
    val content: FrameContent
)