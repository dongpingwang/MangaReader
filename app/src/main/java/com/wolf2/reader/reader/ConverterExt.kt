package com.wolf2.reader.reader

import android.net.Uri
import androidx.compose.ui.util.fastForEach
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.extension.toDocumentFile
import com.wolf2.reader.mode.entity.ReadRecord
import com.wolf2.reader.mode.entity.ReadTime
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.mode.entity.book.CoverImage
import com.wolf2.reader.util.globalContext
import com.wolf2.reader.util.isDirectoryEarly
import com.wolf2.reader.util.listFilesCount
import com.wolf2.reader.util.listFilesUri
import kotlin.math.roundToInt

// epub等单个文件
fun DocumentFile.toBook(): Book {
    return Book(
        uri = this.uri,
        title = this.name ?: "",
        mimeType = this.type ?: ""
    )
}

// 找到封面图片
fun List<Uri>.coverOrFirst(): Uri? {
    return this.find { it.toString().contains("cover") } ?: firstOrNull()
}

fun List<Uri>.cover(): Uri? {
    return this.find { it.toString().contains("cover") }
}

// 图片数量
fun List<Uri>.filesCount(): Int {
    var count = 0
    this.fastForEach {
        count += it.toDocumentFile(globalContext)?.listFilesCount() ?: 0
    }
    return count
}

// 目录下只有图片
fun DocumentFile.toBookNoSubDirectory(filesOfTree: List<Uri> = this.listFilesUri()): Book {
    val book = this.toBook()
    book.extraInfo.directoryAsBook = true
    book.extraInfo.pageCount = filesOfTree.size
    // cover 或者 第一张图片
    val coverFile = filesOfTree.coverOrFirst()
    val coverImage = CoverImage(fileUri = coverFile.toString())
    book.cover = coverImage
    return book
}

// 子目录作为章节
fun DocumentFile.toBookSubDirectoryAsChapter(filesOfTree: List<Uri>): Book {
    val book = this.toBook()
    book.extraInfo.directoryAsBook = true
    book.extraInfo.subDirectoryAsChapter = true
    book.extraInfo.pageCount = filesOfTree.filesCount()
    // 简单的把文件夹数量作为章节数量
    book.extraInfo.chapterCount = filesOfTree.filter { it.isDirectoryEarly() }.size
    // cover
    val coverOfBook = filesOfTree.cover()
    // 第一章节cover/第一张图片
    val coverOfFirstChapter =
        filesOfTree.firstOrNull()?.toDocumentFile(globalContext)?.listFilesUri()
            ?.sortedBy { it.path }?.coverOrFirst()
    val coverFile = coverOfBook ?: coverOfFirstChapter
    val coverImage = CoverImage(fileUri = coverFile.toString())
    book.cover = coverImage
    return book
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