package com.wolf2.reader.config

import android.content.Context
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import com.drake.serialize.serialize.serialLiveData
import com.linxiao.framework.common.globalContext
import com.wolf2.reader.R
import timber.log.Timber
import com.wolf2.reader.config.PageSwitchEffect.*
import com.wolf2.reader.config.ImageQuality.*
import com.wolf2.reader.config.ImageScale.*


enum class ImageQuality(val display: String) {
    None(globalContext.getString(R.string.image_quality_none)),
    Low(globalContext.getString(R.string.image_quality_low)),
    Medium(globalContext.getString(R.string.image_quality_medium)),
    High(globalContext.getString(R.string.image_quality_high))
}

enum class ImageScale(val display: String) {
    Crop(globalContext.getString(R.string.image_scale_crop)),
    Fit(globalContext.getString(R.string.image_scale_fit)),
    FillHeight(globalContext.getString(R.string.image_scale_fill_height)),
    FillWidth(globalContext.getString(R.string.image_scale_fill_width)),
    Inside(globalContext.getString(R.string.image_scale_inside)),
    None(globalContext.getString(R.string.image_scale_none))
}

enum class PageSwitchEffect(val display: String) {
    VerticalPage(globalContext.getString(R.string.page_switch_effect_vertical)),
    HorizontalPage(globalContext.getString(R.string.page_switch_effect_horizon)),
    CurlPage(globalContext.getString(R.string.page_switch_effect_curl)),
}

fun ImageQuality.toFilterQuality(): FilterQuality {
    return when (this) {
        ImageQuality.None -> FilterQuality.None
        ImageQuality.Low -> FilterQuality.Low
        ImageQuality.Medium -> FilterQuality.Medium
        ImageQuality.High -> FilterQuality.High
    }
}

fun ImageScale.toContentScale(): ContentScale {
    return when (this) {
        ImageScale.Crop -> ContentScale.Crop
        ImageScale.Fit -> ContentScale.Fit
        ImageScale.FillHeight -> ContentScale.FillHeight
        ImageScale.FillWidth -> ContentScale.FillWidth
        ImageScale.Inside -> ContentScale.Inside
        ImageScale.None -> ContentScale.None
    }
}


object AppConfig {

    private const val KEY_IMAGE_QUALITY = "image_quality"
    private const val KEY_IMAGE_SCALE = "image_scale"
    private const val KEY_PAGE_SWITCH_EFFECT = "page_switch_effect"
    private const val KEY_DARK_MODE = "dark_mode"
    private const val KEY_BOOK_FOLDERS = "book_folders"

    val pageSwitchEffects = listOf(VerticalPage, HorizontalPage, CurlPage)
    val imageQualities = listOf(ImageQuality.None, Low, Medium, High)
    val imageScales = listOf(ImageScale.None, Crop, Fit, FillHeight, FillWidth, Inside)

    fun init(context: Context) {
        Timber.d("init AppConfig")
    }

    val imageQualityLD by serialLiveData(
        default = ImageQuality.None,
        name = KEY_IMAGE_QUALITY
    )

    val imageScaleLD by serialLiveData(
        default = Fit,
        name = KEY_IMAGE_SCALE
    )

    val pagerSwitchEffectLD by serialLiveData(
        default = VerticalPage,
        name = KEY_PAGE_SWITCH_EFFECT
    )

    val darkModeLD by serialLiveData(default = false, name = KEY_DARK_MODE)

    val bookFolders by serialLiveData(default = emptyList<String>(), name = KEY_BOOK_FOLDERS)
}