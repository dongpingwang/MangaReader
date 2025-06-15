package com.wolf2.reader.config

import com.dylanc.mmkv.MMKVOwner

object AppConfig : MMKVOwner(mmapID = "app_settings") {

    val pagerSwitchEffectLD by mmkvInt().asStateFlow()

    val darkModeLD by mmkvBool().asStateFlow()

    val shelfLayoutModeLD by mmkvInt().asStateFlow()

    val shelfLayoutColumnLD by mmkvInt(default = 2).asStateFlow()

    val themeMode by mmkvInt().asStateFlow()

    val amoled by mmkvBool().asStateFlow()

    val appColor by mmkvInt().asStateFlow()
}
