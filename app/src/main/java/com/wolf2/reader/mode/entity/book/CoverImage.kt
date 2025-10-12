package com.wolf2.reader.mode.entity.book

import android.graphics.Bitmap
import androidx.core.net.toUri
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CoverImage(
    @SerialName("href")
    var href: String = "", // epub封面对应的href
    @kotlinx.serialization.Transient
    var data: ByteArray? = null, // 书籍封面数据，从Epub/Mobi文件获取
    @kotlinx.serialization.Transient
    var bitmap: Bitmap? = null, // 书籍封面数据，从Pdf文件获取
    @SerialName("diskPath")
    var diskPath: String = "", // 保存到磁盘的路径
    @SerialName("fileUri")
    var fileUri: String = "", // 文件uri
) {

    fun getImageSource(): Any? {
        if (data != null) return data
        if (bitmap != null) return bitmap
        if (diskPath.isNotEmpty()) return diskPath
        if (fileUri.isNotEmpty()) return fileUri.toUri()
        return null
    }

    override fun toString(): String {
        return "[CoverImage:href=${href},data.size=${data?.size},diskPath=$diskPath,fileUri=$fileUri]"
    }
}