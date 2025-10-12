package com.wolf2.reader.reader

import android.os.ParcelFileDescriptor
import androidx.compose.ui.util.fastForEachIndexed
import com.anggrayudi.storage.file.DocumentFileCompat
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.mode.entity.book.Chapter
import com.wolf2.reader.mode.entity.book.CoverImage
import com.wolf2.reader.mode.entity.book.ExtraInfo
import com.wolf2.reader.mode.entity.book.Metadata
import com.wolf2.reader.mode.entity.book.PageContent
import com.wolf2.reader.cache.ImageCacheUtil
import com.wolf2.reader.util.closeQuietly
import com.wolf2.reader.util.contains
import com.wolf2.reader.util.globalContext
import com.wolf2.reader.util.traceMillis
import org.jsoup.Jsoup
import timber.log.Timber
import java.io.Closeable

class MobiFileReader(private val book: Book) : Closeable {

    companion object {
        init {
            System.loadLibrary("reader_jni")
        }
    }

    private var isInitSuccess = false
    private var pfd: ParcelFileDescriptor? = null

    private fun checkCondition(): Boolean {
        return isInitSuccess
    }

    fun readMobi(updateMetadata: Boolean, updatePageContent: Boolean) {

        traceMillis {
            val documentFile = DocumentFileCompat.fromUri(globalContext, book.uri)
            if (documentFile == null) {
                Timber.e("book file is null")
                return@traceMillis
            }
            Timber.d("book uri : ${book.uri} ==> ${documentFile.name}")

            val fileDescriptor = runCatching {
                globalContext.contentResolver.openFileDescriptor(book.uri, "r")
            }.onFailure { it.printStackTrace() }.getOrNull()

            if (fileDescriptor == null) {
                Timber.e("book fileDescriptor is null")
                return@traceMillis
            }
            pfd = fileDescriptor

            val initStatus = nativeInitByFd(fileDescriptor.detachFd())
            Timber.d("initStatus: ret = $initStatus")
            isInitSuccess = initStatus == 0

            if (!isInitSuccess) {
                Timber.e("Native Init fail")
                return@traceMillis
            }

            if (updateMetadata) {
                parseCover()?.let {
                    book.cover = it
                    cacheCoverImage()
                }
                parseMetadata()?.let {
                    book.title = it.title
                    book.author = it.author
                }
            }

            if (updatePageContent) {
                val extraInfo = ExtraInfo()
                parseChapters().let {
                    book.chapters = it
                    extraInfo.chapterCount = it.size
                }
                parseContent().let {
                    book.pageContents = it
                    extraInfo.pageCount = it.size
                }
                book.extraInfo = extraInfo

                parseChaptersRange(book.chapters, book.pageContents.size).let {
                    book.chapters = it
                }
            }
        }
    }

    private fun parseCover(): CoverImage? {
        if (!checkCondition()) return null
        val data = nativeGetCoverImage()
        return CoverImage(href = "", data = data)
    }

    private fun cacheCoverImage() {
        val result = ImageCacheUtil.cacheCoverImage(book.uri.toString(), book.cover)
        if (result) {
            book.cover.diskPath =
                ImageCacheUtil.getCoverImageDiskFile(book.uri.toString())
        }
    }

    private fun parseMetadata(): Metadata? {
        if (!checkCondition()) return null
        val title = nativeGetTitle() ?: ""
        val authors = nativeGetAuthor() ?: ""
        return Metadata(
            title = title,
            author = authors
        )
    }

    private fun parseChapters(): List<Chapter> {
        if (!checkCondition()) return emptyList()
        return (nativeGetChapter() ?: emptyList()).also {
            Timber.d("Chapter Size: ${it.size}")
        }
    }

    private fun parseContent(): List<PageContent> {
        if (!checkCondition()) return emptyList()
        return (nativeGetPageContents() ?: emptyList()).also {
            Timber.d("PageContent Size: ${it.size}")
        }
    }

    private fun parseChaptersRange(chapters: List<Chapter>, pageCount: Int): List<Chapter> {
        // 只有1个或者没有目录，当作没有目录
        if (chapters.size <= 1) return chapters
        val parent = chapters.filter { it.level == 0 }
        val child = chapters.filter { it.level == 1 }
        // 只有一级目录
        if (child.isEmpty()) {
            return parent.updateParentChapter(pageCount)
        }
        parent.updateParentChapter(pageCount)
        child.updateChildChapter(parent, pageCount)
        val merge = mergeChapter(parent, child)
        return merge
    }

    private fun List<Chapter>.updateParentChapter(pageCount: Int): List<Chapter> {
        this.fastForEachIndexed { i, c ->
            val cur = c
            if (i == this.size - 1) {
                cur.pageIndexRange = IntRange(cur.posfid, pageCount - 1)
            } else {
                val next = this[i + 1]
                cur.pageIndexRange = IntRange(cur.posfid, next.posfid - 1)
            }
        }
        return this
    }

    private fun List<Chapter>.updateChildChapter(
        parent: List<Chapter>,
        pageCount: Int
    ): List<Chapter> {
        this.fastForEachIndexed { i, c ->
            val cur = c
            if (i == this.size - 1) {
                cur.pageIndexRange = IntRange(cur.posfid, pageCount - 1)
            } else {
                val next = this[i + 1]
                cur.pageIndexRange = IntRange(cur.posfid, next.posfid - 1)
            }
            cur.parent = parent.find { it.pageIndexRange.contains(cur.pageIndexRange) }
        }
        return this
    }

    private fun mergeChapter(parent: List<Chapter>, child: List<Chapter>): List<Chapter> {
        val merge = mutableListOf<Chapter>()
        parent.fastForEachIndexed { i, p ->
            val range = IntRange(p.pageIndexRange.first, p.pageIndexRange.last)
            // 一级目录更新成1页
            p.pageIndexRange = IntRange(range.first, range.first)
            merge.add(p)
            merge.addAll(child.filter { range.contains(it.pageIndexRange) })
        }
        return merge
    }

    fun markupUid2resourceUid(markupUid: Int): Int? {
        if (!checkCondition()) return null
        val data = nativeGetMarkupData(markupUid) ?: return null
        val body = Jsoup.parse(String(data)).body()
        val imgHrefs = mutableListOf<Int>()
        // 解析图片
        body.select("img").forEach {
            imgHrefs.add(matchImgResourceId(it.attr("src")))
        }
        return imgHrefs.firstOrNull()
    }

    // ../images/01220.jpeg -->  1220
    private fun matchImgResourceId(href: String, defValue: Int = 0): Int {
        return runCatching {
            "(\\d+)".toRegex().find(href)?.value?.toInt()
        }.onFailure { it.printStackTrace() }.getOrNull() ?: defValue
    }

    fun getImage(resourceUid: Int): ByteArray? {
        if (!checkCondition()) return null
        return nativeGetResourceData(resourceUid)
    }

    override fun close() {
        if (!checkCondition()) return
        isInitSuccess = false
        pfd?.closeQuietly()
        nativeDestroy()
    }

    private var nativeMOBIDataPtr: Long = 0L

    private var nativeMOBIRawmlPtr: Long = 0L

    private external fun nativeInitByPath(path: String): Int

    private external fun nativeInitByFd(fd: Int): Int

    private external fun nativeDestroy()

    private external fun nativeGetTitle(): String?

    private external fun nativeGetAuthor(): String?

    private external fun nativeGetCoverImage(): ByteArray?

    private external fun nativeGetPageContents(): List<PageContent>?

    private external fun nativeGetChapter(): List<Chapter>?

    private external fun nativeGetResourceData(uid: Int): ByteArray?

    private external fun nativeGetMarkupData(uid: Int): ByteArray?

}