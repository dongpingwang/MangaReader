package com.wolf2.reader.config

import android.os.Environment

data object Constants {

    val appShortName = "MReader"
    val dirRoot = "${Environment.getExternalStorageDirectory().path}/$appShortName"
    val dirCoverImage = "$dirRoot/coverImages"
    val dirCacheImage = "$dirRoot/cacheImages"

    // 书架布局模式
    val List = 0
    val Grid = 1

    // 翻页动画
    val VerticalPage = 0
    val HorizontalPage = 1
    val CurlPage = 2
    val VerticalList = 3
    val SwipeTinder = 4
}