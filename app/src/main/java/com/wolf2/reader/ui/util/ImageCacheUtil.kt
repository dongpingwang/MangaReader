package com.wolf2.reader.ui.util

import com.linxiao.framework.common.globalContext
import com.wolf2.reader.config.Constants
import com.wolf2.reader.convert.toImageBuffer
import com.wolf2.reader.mode.entity.book.CoverImage
import com.wolf2.reader.mode.entity.book.PageContent
import com.wolf2.reader.util.MD5Util
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

object ImageCacheUtil {

    val cacheImageDir = "/sdcard/Download/${Constants.APP_SHORTNAME}"

    fun cacheCoverImage(uriString: String, cover: CoverImage): Boolean {
        if (File(getCoverDiskPath(uriString)).exists()) {
            return true
        }
        if (cover.data == null) {
            return false
        }
        return runCatching {
            val fileName = MD5Util.getMD5String16(uriString, null)
            val dir = globalContext.externalCacheDir?.absolutePath + "/coverImages"
            val dirFile = File(dir)
            if (!dirFile.exists()) {
                dirFile.mkdirs()
            }
            val file = File(dir, fileName)
            val fos = FileOutputStream(file)
            fos.write(cover.data)
            fos.close()
        }.onFailure { it.printStackTrace() }
            .isSuccess
    }

    fun getCoverDiskPath(uriString: String): String {
        val fileName = MD5Util.getMD5String16(uriString, null)
        val dir = globalContext.externalCacheDir?.absolutePath + "/coverImages"
        return "$dir/$fileName"
    }

    fun cacheImage(pageContent: PageContent): Boolean {
        val imageData = pageContent.toImageBuffer().also {
            Timber.d("image buffer is $it")
        } ?: return false
        return runCatching {
            val fileName = MD5Util.getMD5String16(pageContent.pageHref, null) + ".jpg"
            val dir = cacheImageDir
            val dirFile = File(dir)
            if (!dirFile.exists()) {
                dirFile.mkdirs()
            }
            val file = File(dir, fileName)
            val fos = FileOutputStream(file)
            fos.write(imageData)
            fos.close()
        }.onFailure { it.printStackTrace() }
            .isSuccess
    }
}