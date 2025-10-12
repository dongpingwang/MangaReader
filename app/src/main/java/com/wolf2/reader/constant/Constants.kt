package com.wolf2.reader.constant

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

enum class NavLabelShow(private val value: Int) {
    AlwaysShow(0),
    SelectedShow(1),
    Hidden(2);

    companion object {
        fun fromInt(value: Int) =
            NavLabelShow.entries.firstOrNull { it.value == value } ?: Hidden

        fun NavLabelShow.toInt() = value
    }
}

enum class ChapterDisplay(private val value: Int) {
    Chapter(0),
    Total(1);

    companion object {
        fun fromInt(value: Int) =
            ChapterDisplay.entries.firstOrNull { it.value == value } ?: Chapter

        fun ChapterDisplay.toInt() = value
    }
}

enum class ShelfFilter(private val value: Int) {
    ALL(0),
    Reading(1),
    UnRead(2),
    Favorite(3);

    companion object {
        fun fromInt(value: Int) =
            ShelfFilter.entries.firstOrNull { it.value == value } ?: ALL

        fun ShelfFilter.toInt() = value
    }
}

enum class ShelfSort(private val value: Int) {
    LastReadTime(0),
    Title(1),
    Author(2);

    companion object {
        fun fromInt(value: Int) =
            ShelfSort.entries.firstOrNull { it.value == value } ?: LastReadTime

        fun ShelfSort.toInt() = value
    }
}