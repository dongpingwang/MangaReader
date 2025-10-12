package com.wolf2.reader.reader

import android.os.ParcelFileDescriptor
import androidx.compose.ui.util.fastForEachIndexed
import com.anggrayudi.storage.file.DocumentFileCompat
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.mode.entity.book.Chapter
import com.wolf2.reader.mode.entity.book.CoverImage
import com.wolf2.reader.mode.entity.book.Metadata
import com.wolf2.reader.mode.entity.book.PageContent
import com.wolf2.reader.cache.ImageCacheUtil
import com.wolf2.reader.util.closeQuietly
import com.wolf2.reader.util.globalContext
import com.wolf2.reader.util.traceMillis
import org.jsoup.Jsoup
import timber.log.Timber

class EpubFileReader(private val book: Book) : BaseReader(book) {

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

    fun readEpub(updateMetadata: Boolean, updatePageContent: Boolean) {
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

                parseChapters().let {
                    book.chapters = it
                    book.extraInfo.chapterCount = it.size
                }
                parseContent().let {
                    book.pageContents = it
                    book.extraInfo.pageCount = it.size
                }

                parseChaptersRange(book.chapters, book.pageContents.size).let {
                    book.chapters = it
                }
            }
        }
    }

    private fun parseCover(): CoverImage? {
        if (!checkCondition()) return null
        val href = nativeGetCoverImageHref() ?: ""
        val data = nativeGetCoverImage()
        return CoverImage(href = href, data = data)
    }

    private fun cacheCoverImage() {
        val result = ImageCacheUtil.cacheCoverImage(book.uri.toString(), book.cover)
        if (result) {
            book.cover.diskPath = ImageCacheUtil.getCoverImageDiskFile(book.uri.toString())
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
        return chapters.updateParentChapter(pageCount)
    }

    private fun List<Chapter>.updateParentChapter(pageCount: Int): List<Chapter> {
        this.fastForEachIndexed { i, c ->
            val cur = c
            if (i == this.size - 1) {
                val start = matchPageId(this[i].pageHref)
                val end = pageCount - 1
                cur.pageIndexRange = IntRange(start, end)
            } else {
                val start = matchPageId(this[i].pageHref)
                val end = matchPageId(this[i + 1].pageHref) - 1
                cur.pageIndexRange = IntRange(start, end)
            }
        }
        return this
    }

    // Text/part0000.xhtml --> 0
    private fun matchPageId(href: String, defValue: Int = 0): Int {
        return runCatching {
            "(\\d+)".toRegex().find(href)?.value?.toInt()
        }.onFailure { it.printStackTrace() }.getOrNull() ?: defValue
    }

    private fun pageHref2ImageHref(pageHref: String): String? {
        if (!checkCondition()) return null
        val data = nativeGetResourceData(pageHref) ?: return null
        if (isCoverImage(pageHref)) return nativeGetCoverImageHref()
        val body = Jsoup.parse(String(data)).body()
        val imgHrefs = mutableListOf<String>()
        // 解析图片
        body.select("img").forEach {
            imgHrefs.add(getImgHref(it.attr("src")))
        }
        body.select("image").forEach {
            imgHrefs.add(getImgHref(it.attr("xlink:href")))
        }
        return imgHrefs.firstOrNull()
    }

    // ../Images/image00170.jpeg -->  Images/image00172.jpeg
    private fun getImgHref(src: String): String {
        return src.replace("../", "")
    }

    private fun isCoverImage(imgHref: String): Boolean {
        return imgHref.contains("cover_page")
    }

    private fun getImage(imgHref: String): ByteArray? {
        if (!checkCondition()) return null
        return nativeGetResourceData(imgHref)
    }

    override fun read(updateMetadata: Boolean, updatePageContent: Boolean) {
        readEpub(updateMetadata, updatePageContent)
    }

    override fun getImageBuffer(page: PageContent): ByteArray? {
        val imageHref = pageHref2ImageHref(page.pageHref) ?: return null
        return getImage(imageHref)
    }

    override fun close() {
        if (!checkCondition()) return
        pfd?.closeQuietly()
        isInitSuccess = false
        nativeDestroy()
    }

    private var nativeEPUB3RefPtr: Long = 0L

    private external fun nativeInitByFd(fd: Int): Int

    private external fun nativeInitByPath(path: String): Int

    private external fun nativeDestroy()

    private external fun nativeGetTitle(): String?

    private external fun nativeGetAuthor(): String?

    private external fun nativeGetCoverImage(): ByteArray?

    private external fun nativeGetCoverImageHref(): String?

    private external fun nativeGetChapter(): List<Chapter>?

    private external fun nativeGetPageContents(): List<PageContent>?

    private external fun nativeGetResourceData(href: String): ByteArray?

}