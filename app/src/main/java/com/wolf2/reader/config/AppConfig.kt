package com.wolf2.reader.config

import com.dylanc.mmkv.MMKVOwner

object AppConfig : MMKVOwner(mmapID = "app_settings") {

    val pagerSwitchEffectLD by mmkvInt().asLiveData()

    val darkModeLD by mmkvBool().asLiveData()

    val shelfLayoutModeLD by mmkvInt().asLiveData()

    val shelfLayoutColumnLD by mmkvInt(default = 2).asLiveData()
}
