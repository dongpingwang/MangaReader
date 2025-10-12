package com.wolf2.reader.reader

import com.anggrayudi.storage.file.DocumentFileCompat
import com.anggrayudi.storage.file.mimeType
import com.wolf2.reader.config.EbookUtil
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.mode.entity.book.PageContent
import com.wolf2.reader.util.checkUriPermissions
import com.wolf2.reader.util.globalContext
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
        val documentFile = DocumentFileCompat.fromUri(globalContext, from.uri)
        if (documentFile == null) {
            Timber.e("book file is null")
            return false
        }
        if (!documentFile.uri.checkUriPermissions()) {
            Timber.e("book uri hasn't permissions")
            return false
        }
        val mimeType = documentFile.mimeType
        if (EbookUtil.isEpubMimeType(mimeType)) {
            format = 0
            epubFileReader = EpubFileReader(from).apply {
                readEpub(updateMetadata, updatePageContent)
            }
        } else if (EbookUtil.isMobiMimeType(mimeType) || EbookUtil.isAzw3MimeType(mimeType)) {
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
        // 主动gc一下，回收大量bitmap内存
        System.gc()
    }

    fun ifClosed(): Boolean {
        return closed
    }
}