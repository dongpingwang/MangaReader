package com.wolf2.reader.reader

import androidx.compose.ui.util.fastForEach
import androidx.documentfile.provider.DocumentFile
import com.wolf2.reader.mode.entity.ReadRecord
import com.wolf2.reader.mode.entity.ReadTime
import com.wolf2.reader.mode.entity.book.Book
import kotlin.math.roundToInt

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

fun ReadRecord?.percent(): String {
    return when {
        this == null -> 0
        else -> {
            this.curPage.toFloat().div(this.pageCount - 1).times(100)
                .roundToInt()
                .coerceIn(0, 100)
        }
    }.toString() + "%"
}

fun ReadRecord?.numbers(): String {
    return when {
        this == null -> ""
        else -> "${this.curPage + 1}/${this.pageCount}"
    }
}

fun IntRange.percent(progress: Int): String {
    val total = this.last - this.first + 1
    val cur = progress - this.first + 1
    return cur.toFloat().div(total).times(100)
        .roundToInt()
        .coerceIn(0, 100)
        .toString() + "%"
}

fun IntRange.numbers(progress: Int): String {
    val total = this.last - this.first + 1
    val cur = progress - this.first + 1
    return "${cur}/${total}"
}

fun ReadTime.duration(): Long {
    return this.endReadTimeMillis - this.startReadTimeMillis
}

fun List<ReadTime>.durations(): Long {
    var duration = 0L
    this.fastForEach {
        duration += it.duration()
    }
    return duration
}