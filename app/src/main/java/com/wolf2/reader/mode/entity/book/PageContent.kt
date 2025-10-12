package com.wolf2.reader.mode.entity.book

import android.net.Uri

data class PageContent(
    var pageHref: String = "", // epub每一页href
    var markupUid: Int = 0,  // mobi每一页markupUid
    var imageUri: Uri = Uri.EMPTY, // 文件夹图片Uri
) {
    constructor(pageHref: String) : this(pageHref = pageHref, markupUid = 0, imageUri = Uri.EMPTY)

    constructor(markupUid: Int) : this(pageHref = "", markupUid = markupUid, imageUri = Uri.EMPTY)
}
