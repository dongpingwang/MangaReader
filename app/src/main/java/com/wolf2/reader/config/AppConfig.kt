package com.wolf2.reader.config

import com.dylanc.mmkv.MMKVOwner
import com.wolf2.reader.config.NavLabelShow.Companion.toInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

fun <V> MutableStateFlow<V>.mmkvEmit(value: V) {
    CoroutineScope(Dispatchers.IO).launch {
        this@mmkvEmit.value = value
    }
}

object AppConfig : MMKVOwner(mmapID = "app_settings") {

    val pagerSwitchEffect by mmkvInt().asStateFlow()

    val darkMode by mmkvBool().asStateFlow()

    val shelfLayoutMode by mmkvInt().asStateFlow()

    val shelfLayoutColumn by mmkvInt(default = 2).asStateFlow()

    val themeMode by mmkvInt().asStateFlow()

    val amoled by mmkvBool().asStateFlow()

    val appColor by mmkvInt().asStateFlow()

    val navLabelShow by mmkvInt(default = NavLabelShow.Hidden.toInt()).asStateFlow()

    val readerFullScreen by mmkvBool(false).asStateFlow()

    val chapterDisplay by mmkvInt().asStateFlow()
}
