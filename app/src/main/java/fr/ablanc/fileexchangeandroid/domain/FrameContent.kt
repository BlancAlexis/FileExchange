package fr.ablanc.fileexchangeandroid.domain

import android.graphics.Bitmap

sealed class FrameContent {
    data class Image(val value: Bitmap) : FrameContent()
    data class PDF(val value: ByteArray) : FrameContent()
}