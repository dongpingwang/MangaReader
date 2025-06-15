package com.wolf2.reader.reader

import android.os.Environment
import androidx.documentfile.provider.DocumentFile
import com.wolf2.reader.util.globalContext
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.mode.entity.book.PageContent
import timber.log.Timber

class LocalFileReader(private val book: Book) {

    private var format = 0
    private var epubFileReader: EpubFileReader? = null
    private var mobiFileReader: MobiFileReader? = null

    fun readBook(): Boolean {
        val doc = DocumentFile.fromSingleUri(globalContext, book.uri)
        if (doc == null || !doc.exists() || !doc.canRead() || !Environment.isExternalStorageManager()) {
            Timber.e("DocumentFile is not exists")
            return false
        }
        when (doc.type) {
            "application/epub+zip" -> {
                format = 0
                epubFileReader = EpubFileReader(book).apply { readEpub() }
            }

            "application/x-mobipocket-ebook", "application/vnd.amazon.mobi8-ebook" -> {
                format = 1
                mobiFileReader = MobiFileReader(book).apply { readMobi() }
            }

            else -> {}
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