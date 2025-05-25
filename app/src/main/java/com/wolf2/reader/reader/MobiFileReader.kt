package com.wolf2.reader.reader

import com.wolf2.reader.mode.entity.book.Chapter
import com.wolf2.reader.mode.entity.book.PageContent


class MobiFileReader {

    private var nativeMOBIDataPtr: Long = 0L

    companion object {
        init {
            System.loadLibrary("reader_jni")
        }
    }

    external fun nativeInit(path: String): Int

    external fun nativeDestroy()

    external fun getTitle(): String?

    external fun getAuthor(): String?

    external fun getCoverImage(): ByteArray?

    external fun nativeGetPageContents(): List<PageContent>

    external fun nativeGetChapter(): List<Chapter>

    fun readMobi() {

    }


    //fun parse()
}