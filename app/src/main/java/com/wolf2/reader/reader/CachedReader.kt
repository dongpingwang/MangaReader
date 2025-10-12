package com.wolf2.reader.reader

import com.anggrayudi.storage.file.DocumentFileCompat
import com.anggrayudi.storage.file.mimeType
import com.wolf2.reader.config.EbookUtil
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.mode.entity.book.PageContent
import com.wolf2.reader.util.checkUriPermissions
import com.wolf2.reader.util.globalContext
import timber.log.Timber

class CachedReader private constructor(private val source: Book) {

    private var closed = false
    private var reader: BaseReader? = null

    companion object {
        private var reader: CachedReader? = null

        fun withLocalFileReader(book: Book): CachedReader {
            reader?.close()
            return CachedReader(book).also { reader = it }
        }

        fun obtainLocalFileReader(book: Book): CachedReader {
            if (reader != null && reader!!.source.uuid == book.uuid && !reader!!.ifClosed()) {
                return reader!!
            }
            return CachedReader(book).also { reader = it }
        }

        fun newLocalFileReader(book: Book): CachedReader {
            return CachedReader(book)
        }
    }

    fun readBook(updateMetadata: Boolean = true, updatePageContent: Boolean = true): Boolean {
        val documentFile = DocumentFileCompat.fromUri(globalContext, source.uri)
        if (documentFile == null) {
            Timber.e("book file is null")
            return false
        }
        if (!documentFile.uri.checkUriPermissions()) {
            Timber.e("book uri hasn't permissions")
            return false
        }

        if (source.extraInfo.directoryAsBook) {
            reader = DirectoryReader(source)
            reader?.read(updateMetadata, updatePageContent)
            return true
        }

        val mimeType = documentFile.mimeType
        if (EbookUtil.isEpubMimeType(mimeType)) {
            reader = EpubFileReader(source)
        } else if (EbookUtil.isMobiMimeType(mimeType) || EbookUtil.isAzw3MimeType(mimeType)) {
            reader = MobiFileReader(source)
        }
        reader?.read(updateMetadata, updatePageContent)
        return true
    }

    fun copyOrRead(
        to: Book,
        updateMetadata: Boolean = true,
        updatePageContent: Boolean = true
    ): Boolean {
        val hasCache = source.pageContents.isNotEmpty()
        if (hasCache) {
            copyBook(from = source, to = to)
            return true
        }
        return readBook(updateMetadata, updatePageContent)
    }

    fun getImageBuffer(page: PageContent): ByteArray? {
        return reader?.getImageBuffer(page)
    }

    fun close() {
        reader?.close()
        reader = null
        closed = true
        // 主动gc一下，回收大量bitmap内存
        System.gc()
    }

    fun ifClosed(): Boolean {
        return closed
    }
}