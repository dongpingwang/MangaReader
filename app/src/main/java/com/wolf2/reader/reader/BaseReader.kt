package com.wolf2.reader.reader

import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.mode.entity.book.PageContent
import java.io.Closeable

abstract class BaseReader(val source: Book) : Closeable {

    abstract fun read(updateMetadata: Boolean, updatePageContent: Boolean)

    abstract override fun close()

    abstract fun getImageBuffer(page: PageContent): ByteArray?
}