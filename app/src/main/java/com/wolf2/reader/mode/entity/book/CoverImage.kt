package com.wolf2.reader.mode.entity.book

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CoverImage(
    @SerialName("href")
    var href: String = "", // 封面对应的href
    @kotlinx.serialization.Transient
    var data: ByteArray? = null, // 书籍封面书籍
    @SerialName("diskPath")
    var diskPath: String = "" // 保存到磁盘的路径
) {

    fun getImageSource(): Any? {
        if (data == null) {
            return diskPath
        }
        return data
    }

    override fun toString(): String {
        return "[CoverImage:href=${href},data.size=${data?.size},diskPath=$diskPath]"
    }
}