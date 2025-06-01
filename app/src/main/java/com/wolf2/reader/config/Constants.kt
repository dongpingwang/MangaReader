package com.wolf2.reader.config

import android.os.Environment

object Constants {
    val appShortName = "MReader"
    val dirRoot = "${Environment.getExternalStorageDirectory().path}/$appShortName"
    val dirCoverImage = "$dirRoot/coverImages"
    val dirCacheImage = "$dirRoot/cacheImages"
}