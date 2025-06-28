package com.wolf2.reader.mode.entity.book

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExtraInfo(
    @SerialName("pageCount")
    var pageCount: Int =0, // 总页数
    @SerialName("chapterCount")
    var chapterCount: Int =0 // 总章节数
)
