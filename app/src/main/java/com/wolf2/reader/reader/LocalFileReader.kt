package com.wolf2.reader.reader

import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.mode.entity.book.PageContent
import com.wolf2.reader.util.isExternalStorageManager
import com.wolf2.reader.util.storagePath
import timber.log.Timber

class LocalFileReader(private val book: Book) {

    private var format = 0
    private var epubFileReader: EpubFileReader? = null
    private var mobiFileReader: MobiFileReader? = null

    fun readBook(onlyMetadata: Boolean = false): Boolean {
        if (!isExternalStorageManager()) {
            Timber.e("no access all files permission")
            return false
        }
        val path = book.uri.storagePath()
        if (path == null) {
            Timber.e("book file is not exists")
            return false
        }
        if (path.endsWith(".epub")) {
            format = 0
            epubFileReader = EpubFileReader(book).apply { readEpub(onlyMetadata) }
        } else if (path.endsWith(".mobi") || path.endsWith(".azw") || path.endsWith(".azw3")) {
            format = 1
            mobiFileReader = MobiFileReader(book).apply { readMobi(onlyMetadata) }
        }
        return true
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
}