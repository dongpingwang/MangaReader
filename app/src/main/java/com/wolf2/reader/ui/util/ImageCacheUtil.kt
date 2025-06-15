package com.wolf2.reader.ui.util

import com.wolf2.reader.util.MD5Util
import com.wolf2.reader.config.Constants
import com.wolf2.reader.mode.entity.book.CoverImage
import java.io.File

object ImageCacheUtil {

    fun cacheCoverImage(uriString: String, cover: CoverImage): Boolean {
        val imgFile = File(getCoverImageDiskPath(uriString))
        if (imgFile.exists()) {
            return true
        }
        if (cover.data == null) {
            return false
        }
        return runCatching {
            File(Constants.dirRoot).apply { if (!exists()) mkdirs() }
            File(Constants.dirCoverImage).apply { if (!exists()) mkdirs() }
            imgFile.writeBytes(cover.data!!)
        }.onFailure { it.printStackTrace() }.isSuccess
    }

    fun getCoverImageDiskPath(uriString: String): String {
        val fileName = MD5Util.getMD5String16(uriString, null)
        return "${Constants.dirCoverImage}/$fileName"
    }

    fun cacheImage(buffer: ByteArray, displayName: String): Boolean {
        return runCatching {
            File(Constants.dirRoot).apply { if (!exists()) mkdirs() }
            File(Constants.dirCacheImage).apply { if (!exists()) mkdirs() }
            File(getCacheImageDiskPath(displayName)).writeBytes(buffer)
        }.onFailure { it.printStackTrace() }.isSuccess
    }

    fun getCacheImageDiskPath(displayName: String): String {
        return Constants.dirCacheImage + "/" + displayName + ".jpg"
    }

    fun deleteCacheDir(): Boolean {
        val file = File(Constants.dirRoot)
        if (!file.exists()) return false
        return runCatching {
            file.deleteRecursively()
        }.onFailure { it.printStackTrace() }.isSuccess
    }
}