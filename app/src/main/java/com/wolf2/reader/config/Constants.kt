package com.wolf2.reader.config

import android.os.Environment

data object Constants {
    // 缓存目录
    val appShortName = "MReader"
    val dirRoot = "${Environment.getExternalStorageDirectory().path}/$appShortName"
    val dirCoverImage = "$dirRoot/coverImages"
    val dirCacheImage = "$dirRoot/cacheImages"
}

enum class AppTheme(private val value: Int) {
    SYSTEM(0),
    LIGHT(1),
    DARK(2);

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: SYSTEM
        fun AppTheme.toInt() = value
    }
}

enum class AppColor(private val value: Int) {
    DYNAMIC(0),
    PURPLE(1),
    BLUE(2),
    GREEN(3),
    ORANGE(4),
    RED(5);

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull { it.value == value } ?: DYNAMIC
        fun AppColor.toInt() = value
    }
}

enum class ShelfLayout(private val value: Int) {
    LIST(0),
    GIRD(1);

    companion object {
        fun fromInt(value: Int) = ShelfLayout.entries.firstOrNull { it.value == value } ?: LIST
        fun ShelfLayout.toInt() = value
    }
}

enum class PagerSwitchEffect(private val value: Int) {
    VerticalPage(0),
    HorizontalPage(1),
    CurlPage(2),
    VerticalList(3),
    SwipeTinder(4);

    companion object {
        fun fromInt(value: Int) =
            PagerSwitchEffect.entries.firstOrNull { it.value == value } ?: VerticalPage

        fun PagerSwitchEffect.toInt() = value
    }
}