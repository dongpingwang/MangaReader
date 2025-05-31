package com.wolf2.reader.mode.entity.book


data class PageContent(
    var pageHref: String = "", // epub每一页href
    var markupUid: Int = 0  // mobi每一页markupUid
)
