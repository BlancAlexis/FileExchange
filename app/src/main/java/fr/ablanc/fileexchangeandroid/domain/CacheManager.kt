package fr.ablanc.fileexchangeandroid.domain

import android.content.Context
import java.io.File

class CacheManager(
    private val context: Context,
    private val convertIntoFileUseCase: ConvertIntoFileUseCase
) {

    internal fun createCachedFile(uiFile: UIFile?): Result<Unit> {
        return runCatching {
            if (uiFile == null) throw IllegalArgumentException("UIFile is null")

            val fileName = "cached_${uiFile.type.name}_${System.currentTimeMillis()}"
            val tempFile = File(context.cacheDir, fileName)
            tempFile.writeBytes(uiFile.bytes)
        }
    }

    internal fun getCachedFiles(): Result<List<UIFile>> {
        return runCatching {
            context.cacheDir.listFiles()?.mapNotNull { file ->
                try {
                    val bytes = file.readBytes()
                    val type = Type.valueOf(file.name.split("_")[1])
                    val content = convertIntoFileUseCase(bytes)
                    UIFile(file.name, type, bytes, content)
                } catch (e: Exception) {
                    null
                }
            } ?: emptyList()
        }
    }

    internal fun clearCache(): Result<Unit> {
        return runCatching {
            context.cacheDir.listFiles()?.forEach { it.delete() }
        }
    }

    internal fun deleteCachedFile(uiFile: UIFile?): Result<Boolean> {
        return runCatching {
            if (uiFile == null) return@runCatching false

            val file = File(context.cacheDir, uiFile.name)
            file.exists() && file.delete()
        }
    }
}

data class UIFile(
    val name: String? = "",
    val type: Type,
    val bytes: ByteArray,
    val content: FrameContent
)