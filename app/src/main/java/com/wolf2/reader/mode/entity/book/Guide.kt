package com.wolf2.reader.mode.entity.book


data class Guide(
    var cover: CoverImage? = CoverImage(), // 前言封面
    var startPage: Int = 0,// 起始页码
    var endPage: Int = 0 // 结束页码
)