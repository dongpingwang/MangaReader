package com.wolf2.reader.ui.util

import com.wolf2.reader.mode.entity.ReadRecord


fun ReadRecord?.getReadProgress(defValue: String = ""): String {
    this ?: return defValue
    val percent =
        (((this.curPage + 1).toFloat() / this.pageCount) * 100 + 0.5F).toInt().coerceIn(0, 100)
    return "${this.curPage + 1}/${this.pageCount}, ${percent}%" +
            ""
}

fun ReadRecord?.getReadStatus(): String {
    this ?: return "未读"
    return if (this.curPage < 0) {
        "未读"
    } else if ((this.curPage + 1) < this.pageCount) {
        "在读"
    } else {
        "已读完"
    }
}