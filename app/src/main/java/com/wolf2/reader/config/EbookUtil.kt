package com.wolf2.reader.config

object EbookUtil {

    private val epubFileMimeTypes = listOf("application/epub+zip")
    private val mobiFileMimeTypes = listOf("application/x-mobipocket-ebook")
    private val azw3FileMimeTypes =
        listOf("application/vnd.amazon.mobi8-ebook", "application/octet-stream")

    val allFileFormats = listOf(
        "epub",
        "mobi",
        "azw",
        "pdf",
        "cbz",
        "cbr",
        "zip",
        "rar",
        "7z"
    )

    fun isEbookMimeType(mimeType: String?): Boolean {
        return isEpubMimeType(mimeType) || isMobiMimeType(mimeType) || isAzw3MimeType(mimeType)
    }

    fun isEpubMimeType(mimeType: String?): Boolean {
        return epubFileMimeTypes.contains(mimeType)
    }

    fun isMobiMimeType(mimeType: String?): Boolean {
        return mobiFileMimeTypes.contains(mimeType)
    }

    fun isAzw3MimeType(mimeType: String?): Boolean {
        return azw3FileMimeTypes.contains(mimeType)
    }
}