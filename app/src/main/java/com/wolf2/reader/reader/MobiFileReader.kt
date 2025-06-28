package com.wolf2.reader.reader

import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.mode.entity.book.Chapter
import com.wolf2.reader.mode.entity.book.CoverImage
import com.wolf2.reader.mode.entity.book.Metadata
import com.wolf2.reader.mode.entity.book.PageContent
import com.wolf2.reader.ui.util.ImageCacheUtil
import com.wolf2.reader.util.storagePath
import com.wolf2.reader.util.traceMillis
import org.jsoup.Jsoup
import timber.log.Timber
import java.io.Closeable

class MobiFileReader(private val book: Book) : Closeable {

    private var nativeMOBIDataPtr: Long = 0L
    private var nativeMOBIRawmlPtr: Long = 0L

    companion object {
        init {
            System.loadLibrary("reader_jni")
        }
    }

    private val isFileExists by lazy {
        book.uri.storagePath() != null
    }

    private var isInitSuccess = false

    private fun checkCondition(): Boolean {
        return isFileExists && isInitSuccess
    }

    fun readMobi(updateMetadata: Boolean, updatePageContent: Boolean) {
        if (!isFileExists) return
        traceMillis {
            val path = book.uri.storagePath()
            if (path == null) {
                Timber.e("BOOK Path is NULL")
                return@traceMillis
            }
            Timber.d("book uri : ${book.uri} ==> $path")

            val initStatus = nativeInit(path)
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
//                parseChapters().let { book.chapters = it }
                parseContent().let { book.pageContents = it }
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
            book.cover.diskPath = ImageCacheUtil.getCoverImageDiskPath(book.uri.toString())
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
        nativeDestroy()
    }

    private external fun nativeInit(path: String): Int

    private external fun nativeDestroy()

    private external fun nativeGetTitle(): String?

    private external fun nativeGetAuthor(): String?

    private external fun nativeGetCoverImage(): ByteArray?

    private external fun nativeGetPageContents(): List<PageContent>?

    private external fun nativeGetChapter(): List<Chapter>?

    private external fun nativeGetResourceData(uid: Int): ByteArray?

    private external fun nativeGetMarkupData(uid: Int): ByteArray?

}