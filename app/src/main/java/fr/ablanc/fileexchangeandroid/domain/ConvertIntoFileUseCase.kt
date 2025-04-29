package fr.ablanc.fileexchangeandroid.domain

import android.graphics.BitmapFactory
import java.io.ByteArrayInputStream
import java.net.URLConnection

class ConvertIntoFileUseCase() {

    operator fun invoke(byteArray: ByteArray): FrameContent {
        return when (val name = Type.entries.find { it.type == detectMimeType(byteArray) } ?: "") {
            Type.JPG -> FrameContent.Image(
                BitmapFactory.decodeByteArray(
                    byteArray, 0, byteArray.size
                )
            )

            Type.PNG -> FrameContent.Image(
                BitmapFactory.decodeByteArray(
                    byteArray, 0, byteArray.size
                )
            )

            Type.PDF -> FrameContent.PDF(
                byteArray
            )

            else -> {
                throw IllegalArgumentException("Unknown type $name")
            }
        }

    }


    private fun detectMimeType(data: ByteArray): String? {
        return ByteArrayInputStream(data).use { stream ->
            URLConnection.guessContentTypeFromStream(stream)
        }
    }


}

enum class Type(val type: String) {
    JPG("image/jpeg"), PNG("image/png"), PDF("application/pdf")
}