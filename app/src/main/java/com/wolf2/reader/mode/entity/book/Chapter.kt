package com.wolf2.reader.mode.entity.book


data class Chapter(
    var title: String = "", // 章节标题
    var pageHref: String, // 对应pageHref
    var parent: String, // 父目录
)
