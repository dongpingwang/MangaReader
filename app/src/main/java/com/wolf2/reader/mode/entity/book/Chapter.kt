package com.wolf2.reader.mode.entity.book

data class Chapter(
    var title: String, // 章节标题
    var pageIndexRange: IntRange, // 页码范围，从0开始，区间[start, end)
    var parent: Chapter? = null, // 父目录，**目前不支持嵌套目录**

    var pageHref: String, // epub对应pageHref，如Text/part0000.xhtml

    var level: Int, // mobi对应level，代表目录层级
    var posfid: Int,// mobi对应起始位置

) {
    // for epub
    constructor(title: String, pageHref: String, parent: String) : this(
        title = title,
        pageIndexRange = IntRange(0, 0),
        parent = null,
        pageHref = pageHref,
        level = 1,
        posfid = 0,
    )

    // for mobi
    constructor(title: String, level: Int, posfid: Int) : this(
        title = title,
        pageIndexRange = IntRange(0, 0),
        parent = null,
        pageHref = "",
        level = level,
        posfid = posfid,
    )
}
