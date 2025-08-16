package com.wolf2.reader.reader

import androidx.documentfile.provider.DocumentFile
import com.wolf2.reader.mode.entity.book.Book

fun DocumentFile.toBook(): Book {
    return Book(
        uri = this.uri,
        title = this.name ?: "",
        mimeType = this.type ?: ""
    )
}

fun copyBook(from: Book, to: Book) {
    if (from.uuid != to.uuid) return
    if (from == to) return
    to.apply {
        uuid = from.uuid
        uri = from.uri
        title = from.title
        author = from.author
        mimeType = from.mimeType
        cover = from.cover
        chapters = from.chapters
        pageContents = from.pageContents
        lastAddedTimeMillis = from.lastAddedTimeMillis
        extraInfo = from.extraInfo
    }
}