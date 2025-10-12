package com.wolf2.reader.reader

import android.graphics.Bitmap
import android.os.ParcelFileDescriptor
import com.anggrayudi.storage.file.DocumentFileCompat
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore
import com.wolf2.reader.cache.ImageCacheUtil
import com.wolf2.reader.mode.entity.book.Book
import com.wolf2.reader.mode.entity.book.CoverImage
import com.wolf2.reader.mode.entity.book.Metadata
import com.wolf2.reader.mode.entity.book.PageContent
import com.wolf2.reader.util.closeQuietly
import com.wolf2.reader.util.globalContext
import com.wolf2.reader.util.traceMillis
import timber.log.Timber
import java.io.ByteArrayOutputStream


class PdfFileReader(private val book: Book) : BaseReader(book) {

    private var isInitSuccess = false
    private var pfd: ParcelFileDescriptor? = null
    private var pdfiumCore: PdfiumCore? = null
    private var pdfDocument: PdfDocument? = null

    private fun checkCondition(): Boolean {
        return isInitSuccess
    }

    private fun readPdf(updateMetadata: Boolean, updatePageContent: Boolean) {
        traceMillis {
            val documentFile = DocumentFileCompat.fromUri(globalContext, book.uri)
            if (documentFile == null) {
                Timber.e("book file is null")
                return@traceMillis
            }
            Timber.d("book uri : ${book.uri} ==> ${documentFile.name}")

            val fileDescriptor = runCatching {
                globalContext.contentResolver.openFileDescriptor(book.uri, "r")
            }.onFailure { it.printStackTrace() }.getOrNull()

            if (fileDescriptor == null) {
                Timber.e("book fileDescriptor is null")
                return@traceMillis
            }
            pfd = fileDescriptor

            pdfDocument = runCatching {
                pdfiumCore = PdfiumCore(globalContext)
                pdfiumCore?.newDocument(pfd)
            }.onFailure { it.printStackTrace() }
                .onSuccess { isInitSuccess = true }
                .getOrNull()

            if (!isInitSuccess) {
                Timber.e("PdfiumCore Init fail")
                return@traceMillis
            }

            if (updateMetadata) {
                parseCover()?.let {
                    book.cover = it
                    cacheCoverImage(book.cover)
                }
                parseMetadata()?.let {
                    book.title = it.title
                    book.author = it.author
                }
            }
            if (updatePageContent) {
                // TODO 解析目录
                parseContent().let {
                    book.pageContents = it
                    book.extraInfo.pageCount = it.size
                }
            }
        }
    }

    private fun parseMetadata(): Metadata? {
        if (!checkCondition()) return null
        val meta = pdfiumCore?.getDocumentMeta(pdfDocument)
        return meta?.toBookMetadata()
    }

    private fun parseCover(): CoverImage? {
        if (!checkCondition()) return null
        val coverImage = CoverImage(bitmap = getPageBitmap(0))
        return coverImage
    }

    fun getPageBitmap(pageIndex: Int): Bitmap? {
        if (!checkCondition()) return null
        val ptr = pdfiumCore?.openPage(pdfDocument, pageIndex)
        val pageWidth = pdfiumCore?.getPageWidth(pdfDocument, pageIndex)
        val pageHeight = pdfiumCore?.getPageHeight(pdfDocument, pageIndex)
        if (pageWidth == null || pageHeight == null) return null
        val bitmap = Bitmap.createBitmap(pageWidth, pageHeight, Bitmap.Config.ARGB_8888)
        pdfiumCore?.renderPageBitmap(pdfDocument, bitmap, pageIndex, 0, 0, pageWidth, pageHeight)
        return bitmap
    }

    private fun cacheCoverImage(coverImage: CoverImage) {
        val outputStream = ByteArrayOutputStream()
        coverImage.bitmap?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val bytes = outputStream.toByteArray()
        coverImage.data = bytes
        outputStream.closeQuietly()
        val result = ImageCacheUtil.cacheCoverImage(book.uri.toString(), book.cover)
        if (result) {
            book.cover.diskPath = ImageCacheUtil.getCoverImageDiskFile(book.uri.toString())
        }
    }

    private fun parseContent(): List<PageContent> {
        if (!checkCondition()) return emptyList()
        val pageCount = pdfiumCore?.getPageCount(pdfDocument) ?: return emptyList()
        val result = mutableListOf<PageContent>()
        for (i in 0 until pageCount) {
            result.add(PageContent(pageIndex = i))
        }
        Timber.d("PageContent Size: ${result.size}")
        return result
    }

    override fun read(updateMetadata: Boolean, updatePageContent: Boolean) {
        readPdf(updateMetadata, updatePageContent)
    }

    override fun close() {
        if (!checkCondition()) return
        isInitSuccess = false
        if (pdfiumCore != null && pdfDocument != null) {
            pdfiumCore?.closeDocument(pdfDocument)
        }
        pfd?.closeQuietly()
    }

    override fun getImageBuffer(page: PageContent): ByteArray? {
        val bitmap = getPageBitmap(page.pageIndex) ?: return null
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        val result = out.toByteArray()
        out.closeQuietly()
        return result
    }
}