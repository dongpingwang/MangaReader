package com.wolf2.reader.cache

import android.content.Context
import com.wolf2.reader.util.MD5Util
import com.wolf2.reader.mode.entity.book.CoverImage
import com.wolf2.reader.util.globalContext
import java.io.File

object ImageCacheUtil {

    fun cacheCoverImage(uriString: String, cover: CoverImage): Boolean {
        val imgFile = File(getCoverImageDiskFile(uriString = uriString, mkdir = true))
        if (imgFile.exists()) {
            return true
        }
        if (cover.data == null) {
            return false
        }
        return runCatching {
            imgFile.writeBytes(cover.data!!)
        }.onFailure { it.printStackTrace() }.isSuccess
    }

    fun getCoverImageDiskFile(uriString: String, mkdir: Boolean = false): String {
        val fileName = MD5Util.getMD5String16(uriString, null)
        val app_images = globalContext.getDir(
            "images",
            Context.MODE_PRIVATE
        )
        if (!app_images.exists() && mkdir) {
            app_images.mkdirs()
        }
        val coverImages = File(app_images, "coverImages")
        if (!coverImages.exists() && mkdir) {
            coverImages.mkdirs()
        }
        return File(coverImages, fileName).absolutePath
    }
}