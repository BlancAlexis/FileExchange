package fr.ablanc.fileexchangeandroid.domain

enum class WebDataType(val type: String) {
    KEY("KEY:"), DATA("DATA");

    companion object {
        fun fromType(type: String): WebDataType? =
            WebDataType.entries.firstOrNull { it.type == type }
    }

}