package com.wolf2.reader.config

val supportFileExtends = listOf(".epub", ".mobi", ".azw3")

val ebookMimeTypes by lazy {
    mutableListOf<String>().apply {
        supportFileExtends.forEach {
            when (it) {
                ".epub" -> add("application/epub+zip")
                ".mobi" -> add("application/x-mobipocket-ebook")
                ".azw3" -> {
                    add("application/vnd.amazon.mobi8-ebook")
                    add("application/octet-stream")
                }
            }
        }
    }
}
