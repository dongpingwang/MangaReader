package com.wolf2.reader.config

import android.preference.PreferenceManager
import com.dylanc.mmkv.MMKVOwner
import com.linxiao.framework.common.globalContext

object AppConfig : MMKVOwner(mmapID = "app_settings") {

    val pagerSwitchEffectLD by mmkvInt().asLiveData()

    val darkModeLD by mmkvBool().asLiveData()

    val shelfLayoutModeLD by mmkvInt().asLiveData()

    val shelfLayoutColumnLD by mmkvInt(default = 2).asLiveData()

    // 从me.zhanghai.compose.preference库中获取
    val coverImageExtension: Boolean
        get() {
            return PreferenceManager.getDefaultSharedPreferences(globalContext)
                .getBoolean("cover_image_extension", false)
        }
}
