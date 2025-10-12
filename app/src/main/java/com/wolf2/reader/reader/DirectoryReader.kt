package com.wolf2.reader.reader

import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import com.anggrayudi.storage.extension.toDocumentFile
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.mode.entity.book.Chapter
import com.wolf2.reader.mode.entity.book.PageContent
import com.wolf2.reader.util.globalContext
import com.wolf2.reader.util.isDirectoryEarly
import com.wolf2.reader.util.listFilesUri
import com.wolf2.reader.util.traceMillis
import timber.log.Timber

class DirectoryReader(private val book: Book) : BaseReader(book) {

    private fun readDirectory() {
        traceMillis {
            if (book.extraInfo.subDirectoryAsChapter) {
                // 只有一级子目录，每个子目录当作一个章节
                val documentFile = book.uri.toDocumentFile(globalContext) ?: return@traceMillis
                val filesUri = documentFile.listFilesUri().sortedBy { it.path }

                val subDirectories = filesUri.filter { it.isDirectoryEarly() }
                // 实际最多只有一个cover
                val covers = filesUri.filter { !it.isDirectoryEarly() }

                val pages = mutableListOf<PageContent>()
                val chapters = mutableListOf<Chapter>()

                covers.fastForEachIndexed { i, it ->
                    pages.add(PageContent(imageUri = it))
                    chapters.add(
                        Chapter(
                            title = it.toDocumentFile(globalContext)?.name ?: "",
                            pageIndexRange = IntRange(i, i)
                        )
                    )
                }

                var startIndex = covers.size
                subDirectories.fastForEach { dir ->
                    val doc = dir.toDocumentFile(globalContext) ?: return@traceMillis
                    val files = doc.listFilesUri().sortedBy { it.path }
                    Timber.d("filessss: ${files.size}")
                    files.fastForEach {
                        pages.add(PageContent(imageUri = it))
                        Timber.d("addd pc --- $it")
                    }
                    val endIndex = startIndex + files.size
                    chapters.add(
                        Chapter(
                            title = doc.name ?: "",
                            pageIndexRange = IntRange(startIndex, endIndex)
                        )
                    )
                    startIndex = endIndex
                }

                book.pageContents = pages
                book.chapters = chapters

            } else {
                // 没有子目录，没有章节，只需构建页面
                val documentFile = book.uri.toDocumentFile(globalContext) ?: return@traceMillis
                val filesUri = documentFile.listFilesUri().sortedBy { it.path }
                val pages = mutableListOf<PageContent>()
                filesUri.fastForEach {
                    pages.add(PageContent(imageUri = it))
                }
                book.pageContents = pages
            }
        }
    }

    override fun read(updateMetadata: Boolean, updatePageContent: Boolean) {
        readDirectory()
    }

    override fun getImageBuffer(page: PageContent): ByteArray? {
        return kotlin.runCatching {
            globalContext.contentResolver.openInputStream(page.imageUri)?.readBytes()
        }.onFailure {
            it.printStackTrace()
        }.getOrNull()
    }

    override fun close() {
    }

}