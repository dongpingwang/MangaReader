package com.wolf2.reader.config

val supportFileFormats = listOf(".epub", ".mobi", ".azw3")

val ebookMimeTypes by lazy {
    mutableListOf<String>().apply {
        supportFileFormats.forEach {
            when (it) {
                ".epub" -> add("application/epub+zip")
                ".mobi" -> add("application/x-mobipocket-ebook")
                ".azw3" -> add("application/vnd.amazon.mobi8-ebook")
            }
        }
    }
}
