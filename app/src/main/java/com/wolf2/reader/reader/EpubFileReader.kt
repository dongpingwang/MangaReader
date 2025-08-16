package com.wolf2.reader.reader

import androidx.compose.ui.util.fastForEachIndexed
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.mode.entity.book.Chapter
import com.wolf2.reader.mode.entity.book.CoverImage
import com.wolf2.reader.mode.entity.book.ExtraInfo
import com.wolf2.reader.mode.entity.book.Metadata
import com.wolf2.reader.mode.entity.book.PageContent
import com.wolf2.reader.ui.util.ImageCacheUtil
import com.wolf2.reader.util.storagePath
import com.wolf2.reader.util.traceMillis
import org.jsoup.Jsoup
import timber.log.Timber
import java.io.Closeable

class EpubFileReader(private val book: Book) : Closeable {

    companion object {
        init {
            System.loadLibrary("reader_jni")
        }
    }

    private var isInitSuccess = false

    private fun checkCondition(): Boolean {
        return isInitSuccess
    }

    fun readEpub(updateMetadata: Boolean, updatePageContent: Boolean) {
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

        book.chapters.forEach{
            Timber.d("chapter:  $it ")
        }
    }

    private fun parseCover(): CoverImage? {
        if (!checkCondition()) return null
        val href = nativeGetCoverImageHref() ?: ""
        val data = nativeGetCoverImage()
        Timber.d("nativeGetCoverImageHref:$href")
        return CoverImage(href = href, data = data)
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
        val chapters = nativeGetChapter()
        Timber.d("chapter size: ${chapters?.size}")
        if (chapters == null) return emptyList()
        return chapters
    }

    private fun parseContent(): List<PageContent> {
        if (!checkCondition()) return emptyList()
        return (nativeGetPageContents() ?: emptyList()).also {
            Timber.d("PageContent Size: ${it.size}")
        }
    }

    private fun parseChaptersRange(chapters: List<Chapter>, pageCount: Int): List<Chapter> {
        chapters.fastForEachIndexed { i, c ->
            c.pageIndexRange = chapters.getPageRange(i, pageCount)
        }
        return chapters
    }

    fun pageHref2ImageHref(pageHref: String): String? {
        if (!checkCondition()) return null
        val data = nativeGetResourceData(pageHref) ?: return null
        if (isCoverImage(pageHref)) return nativeGetCoverImageHref()
        val body = Jsoup.parse(String(data)).body()
        val imgHrefs = mutableListOf<String>()
        // 解析图片
        body.select("img").forEach {
            imgHrefs.add(getImgHref(it.attr("src")))
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

    fun getImage(imgHref: String): ByteArray? {
        if (!checkCondition()) return null
        return nativeGetResourceData(imgHref)
    }

    override fun close() {
        if (!checkCondition()) return
        isInitSuccess = false
        nativeDestroy()
    }

    private var nativeEPUB3RefPtr: Long = 0L

    private external fun nativeInit(path: String): Int

    private external fun nativeDestroy()

    private external fun nativeGetTitle(): String?

    private external fun nativeGetAuthor(): String?

    private external fun nativeGetCoverImage(): ByteArray?

    private external fun nativeGetCoverImageHref(): String?

    private external fun nativeGetChapter(): List<Chapter>?

    private external fun nativeGetPageContents(): List<PageContent>?

    private external fun nativeGetResourceData(href: String): ByteArray?

}