package com.wolf2.reader.mode.entity.book

data class Chapter(
    var title: String, // 章节标题
    var pageHref: String, // 对应pageHref
    var pageIndexRange: IntRange, // 页码范围，从0开始
    var parent: String, // 父目录，**目前不支持嵌套目录**
) {
    constructor(title: String, pageHref: String, parent: String) : this(
        title,
        pageHref,
        IntRange(0, 1),
        parent
    )
}
