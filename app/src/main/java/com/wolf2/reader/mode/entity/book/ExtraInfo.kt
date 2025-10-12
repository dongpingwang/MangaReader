package com.wolf2.reader.mode.entity.book

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExtraInfo(
    @SerialName("pageCount")
    var pageCount: Int = 0, // 总页数
    @SerialName("chapterCount")
    var chapterCount: Int = 0, // 总章节数
    @SerialName("directoryAsBook")
    var directoryAsBook: Boolean = false, // 整个目录作为一本书
    @SerialName("subDirectoryAsChapter")
    var subDirectoryAsChapter: Boolean = false,// 子目录作为章节
)
