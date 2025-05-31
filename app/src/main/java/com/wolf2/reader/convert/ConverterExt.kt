package com.wolf2.reader.convert

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

fun List<Chapter>.getPageRange(chapterIndex: Int, pageCount: Int): Pair<Int, Int> {
    if (chapterIndex == this.size - 1) {
        val start = matchPageId(this[chapterIndex].pageHref)
        val end = pageCount - 1
        return start.to(end)
    } else {
        val start = matchPageId(this[chapterIndex].pageHref)
        val end = matchPageId(this[chapterIndex + 1].pageHref)
        return start.to(end)
    }
}

// Text/part0000.xhtml --> 0
private fun matchPageId(href: String, defValue: Int = 0): Int {
    return runCatching {
        "(\\d+)".toRegex().find(href)?.value?.toInt()
    }.onFailure { it.printStackTrace() }.getOrNull() ?: defValue
}