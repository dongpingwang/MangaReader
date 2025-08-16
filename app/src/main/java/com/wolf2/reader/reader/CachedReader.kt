package com.wolf2.reader.reader

import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.mode.entity.book.PageContent
import com.wolf2.reader.util.isExternalStorageManager
import com.wolf2.reader.util.storagePath
import timber.log.Timber

class CachedReader private constructor(private val from: Book) {

    private var format = 0
    private var epubFileReader: EpubFileReader? = null
    private var mobiFileReader: MobiFileReader? = null
    private var closed = false

    companion object {
        private var reader: CachedReader? = null

        fun withLocalFileReader(book: Book): CachedReader {
            reader?.close()
            return CachedReader(book).also { reader = it }
        }

        fun obtainLocalFileReader(book: Book): CachedReader {
            if (reader != null && reader!!.from.uuid == book.uuid && !reader!!.ifClosed()) {
                return reader!!
            }
            return CachedReader(book).also { reader = it }
        }

        fun newLocalFileReader(book: Book): CachedReader {
            return CachedReader(book)
        }
    }

    fun readBook(updateMetadata: Boolean = true, updatePageContent: Boolean = true): Boolean {
        if (!isExternalStorageManager()) {
            Timber.e("no access all files permission")
            return false
        }
        val path = from.uri.storagePath()
        if (path == null) {
            Timber.e("book file is not exists")
            return false
        }
        if (path.endsWith(".epub")) {
            format = 0
            epubFileReader = EpubFileReader(from).apply {
                readEpub(updateMetadata, updatePageContent)
            }
        } else if (path.endsWith(".mobi") || path.endsWith(".azw") || path.endsWith(".azw3")) {
            format = 1
            mobiFileReader = MobiFileReader(from).apply {
                readMobi(updateMetadata, updatePageContent)
            }
        }
        return true
    }

    fun copyOrRead(
        to: Book,
        updateMetadata: Boolean = true,
        updatePageContent: Boolean = true
    ): Boolean {
        val hasCache = from.pageContents.isNotEmpty()
        if (hasCache) {
            copyBook(from = from, to = to)
            return true
        }
        return readBook(updateMetadata, updatePageContent)
    }

    fun getImageBuffer(page: PageContent): ByteArray? {
        when (format) {
            0 -> {
                val imageHref = epubFileReader?.pageHref2ImageHref(page.pageHref)
                if (imageHref == null) return null
                return epubFileReader?.getImage(imageHref)
            }

            1 -> {
                val imgResourceUid = mobiFileReader?.markupUid2resourceUid(page.markupUid)
                if (imgResourceUid == null) return null
                return mobiFileReader?.getImage(imgResourceUid)
            }

            else -> return null
        }
    }

    fun close() {
        epubFileReader?.close()
        mobiFileReader?.close()
        epubFileReader = null
        mobiFileReader = null
        closed = true
    }

    fun ifClosed(): Boolean {
        return closed
    }
}