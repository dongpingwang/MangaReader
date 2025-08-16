package com.wolf2.reader.reader

import androidx.documentfile.provider.DocumentFile
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.mode.entity.book.Chapter

fun DocumentFile.toBook(): Book {
    return Book(
        uri = this.uri,
        title = this.name ?: "",
        mimeType = this.type ?: ""
    )
}

fun List<Chapter>.getPageRange(chapterIndex: Int, pageCount: Int): IntRange {
    if (chapterIndex == this.size - 1) {
        val start = matchPageId(this[chapterIndex].pageHref)
        val end = pageCount - 1
        return IntRange(start, end)
    } else {
        val start = matchPageId(this[chapterIndex].pageHref)
        val end = matchPageId(this[chapterIndex + 1].pageHref) - 1
        return IntRange(start, end)
    }
}

// Text/part0000.xhtml --> 0
private fun matchPageId(href: String, defValue: Int = 0): Int {
    return runCatching {
        "(\\d+)".toRegex().find(href)?.value?.toInt()
    }.onFailure { it.printStackTrace() }.getOrNull() ?: defValue
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