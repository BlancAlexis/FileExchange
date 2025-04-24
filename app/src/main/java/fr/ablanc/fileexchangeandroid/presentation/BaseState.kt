package fr.ablanc.fileexchangeandroid.presentation

import android.graphics.Bitmap

data class BaseState(
    val serverConnected : Boolean = false,
    val image : Bitmap? = null
)